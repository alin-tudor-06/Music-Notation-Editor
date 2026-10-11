package com.alin.musicnotation.playback;

import com.alin.musicnotation.model.Accidental;
import com.alin.musicnotation.model.KeySignature;
import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.Pitch;
import com.alin.musicnotation.model.Score;
import com.alin.musicnotation.model.tempo.Tempo;
import com.alin.musicnotation.model.Tuplet;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MidiSequenceBuilder {

    public static final int TICKS_PER_QUARTER = 480;
    public static final int DEFAULT_VELOCITY = 80;
    public static final int MIDI_CHANNEL = 0;
    public static final int PIANO_PROGRAM = 0;

    private MidiSequenceBuilder() {}

    public static Sequence build(Score score) throws InvalidMidiDataException {
        Sequence sequence = new Sequence(Sequence.PPQ, TICKS_PER_QUARTER);
        Track track = sequence.createTrack();

        ShortMessage pc = new ShortMessage(ShortMessage.PROGRAM_CHANGE, MIDI_CHANNEL, PIANO_PROGRAM, 0);
        track.add(new MidiEvent(pc, 0));

        long currentTick = 0;
        KeySignature currentKey = score.keySignature();

        // ─── Ordinea de redare (cu repeat expandat) ───
        List<Integer> playOrder = computePlayOrder(score);

        for (int measureIdx : playOrder) {
            Measure measure = score.measures().get(measureIdx);

            if (measure.keySignature() != null) {
                currentKey = measure.keySignature();
            }

            Tempo tempo = measure.tempo();
            if (tempo != null && tempo.hasBpm()) {
                addTempoEvent(track, currentTick, tempo.bpm());
            }

            for (Note note : measure.notes()) {
                long noteTicks = computeTicks(note);

                if (!note.isRest()) {
                    int midiNote = midiNumberWithKey(note.pitch(), currentKey);
                    ShortMessage on = new ShortMessage(ShortMessage.NOTE_ON, MIDI_CHANNEL, midiNote, DEFAULT_VELOCITY);
                    ShortMessage off = new ShortMessage(ShortMessage.NOTE_OFF, MIDI_CHANNEL, midiNote, 0);
                    track.add(new MidiEvent(on, currentTick));
                    track.add(new MidiEvent(off, currentTick + noteTicks));
                }

                currentTick += noteTicks;
            }
        }

        MetaMessage endMsg = new MetaMessage();
        endMsg.setMessage(0x2F, new byte[0], 0);
        track.add(new MidiEvent(endMsg, currentTick));

        return sequence;
    }

    /**
     * Calculează ordinea de redare a măsurilor, expandând repeat signs.
     * Reguli:
     * - Fiecare end repeat declanșează o singură revenire.
     * - Când întâlnim un end repeat, căutăm înapoi ultimul start repeat.
     *   Dacă nu există, revenim la începutul partiturii.
     * - Repetițiile deja efectuate nu se mai repetă.
     */
    private static List<Integer> computePlayOrder(Score score) {
        List<Measure> measures = score.measures();
        List<Integer> order = new ArrayList<>();
        if (measures.isEmpty()) return order;

        Set<Integer> playedEndRepeats = new HashSet<>();
        int safety = 0;
        int maxIterations = measures.size() * 20;   // protecție anti-loop infinit

        int i = 0;
        while (i < measures.size() && safety++ < maxIterations) {
            Measure m = measures.get(i);
            order.add(i);

            if (m.hasEndRepeat() && !playedEndRepeats.contains(i)) {
                playedEndRepeats.add(i);

                // Căutăm înapoi ultimul start repeat
                int jumpTo = 0;
                for (int j = i; j >= 0; j--) {
                    if (measures.get(j).hasStartRepeat()) {
                        jumpTo = j;
                        break;
                    }
                }
                i = jumpTo;
            } else {
                i++;
            }
        }
        return order;
    }

    private static int midiNumberWithKey(Pitch pitch, KeySignature key) {
        int base = pitch.toMidiNumber();
        if (pitch.accidental() != Accidental.NONE) {
            return base;
        }
        if (key != null && key.altersStep(pitch.step())) {
            Accidental keyAcc = key.accidentalForStep(pitch.step());
            return base + keyAcc.semitoneOffset();
        }
        return base;
    }

    private static long computeTicks(Note note) {
        int baseTicks = note.duration().toMidiTicks(TICKS_PER_QUARTER);
        Tuplet tuplet = note.tuplet();
        if (tuplet != null) {
            baseTicks = (int) Math.round(baseTicks * tuplet.durationFactor());
        }
        return baseTicks;
    }

    private static void addTempoEvent(Track track, long tick, int bpm) throws InvalidMidiDataException {
        int mpq = 60_000_000 / bpm;
        MetaMessage tempoMsg = new MetaMessage();
        tempoMsg.setMessage(0x51, new byte[]{
                (byte) ((mpq >> 16) & 0xFF),
                (byte) ((mpq >> 8) & 0xFF),
                (byte) (mpq & 0xFF)
        }, 3);
        track.add(new MidiEvent(tempoMsg, tick));
    }

    /**
     * Poziții temporale pentru cursor – TREBUIE să folosească aceeași ordine de redare
     * ca build(), altfel cursorul sare greșit la repeat.
     */
    public static List<NoteTiming> computeTimings(Score score) {
        List<NoteTiming> timings = new ArrayList<>();
        List<Integer> playOrder = computePlayOrder(score);

        long currentTick = 0;
        for (int measureIdx : playOrder) {
            Measure measure = score.measures().get(measureIdx);
            for (Note note : measure.notes()) {
                long noteTicks = computeTicks(note);
                timings.add(new NoteTiming(note, measureIdx, currentTick, currentTick + noteTicks));
                currentTick += noteTicks;
            }
        }
        return timings;
    }

    public record NoteTiming(Note note, int measureIndex, long startTick, long endTick) {}
}