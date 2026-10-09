package com.alin.musicnotation.playback;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
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
import java.util.List;

/**
 * Construiește o {@link Sequence} MIDI din Score.
 * Folosește TICKS_PER_QUARTER = 480, la fel ca DIVISIONS-ul MusicXML.
 */
public final class MidiSequenceBuilder {

    public static final int TICKS_PER_QUARTER = 480;
    public static final int DEFAULT_VELOCITY = 80;
    public static final int MIDI_CHANNEL = 0;
    public static final int PIANO_PROGRAM = 0;

    private MidiSequenceBuilder() {}

    public static Sequence build(Score score) throws InvalidMidiDataException {
        Sequence sequence = new Sequence(Sequence.PPQ, TICKS_PER_QUARTER);
        Track track = sequence.createTrack();

        // Program change: piano
        ShortMessage pc = new ShortMessage(ShortMessage.PROGRAM_CHANGE, MIDI_CHANNEL, PIANO_PROGRAM, 0);
        track.add(new MidiEvent(pc, 0));

        long currentTick = 0;

        for (Measure measure : score.measures()) {
            Tempo tempo = measure.tempo();
            if (tempo != null && tempo.hasBpm()) {
                addTempoEvent(track, currentTick, tempo.bpm());
            }

            for (Note note : measure.notes()) {
                long noteTicks = computeTicks(note);

                if (!note.isRest()) {
                    int midiNote = note.pitch().toMidiNumber();
                    ShortMessage on = new ShortMessage(ShortMessage.NOTE_ON, MIDI_CHANNEL, midiNote, DEFAULT_VELOCITY);
                    ShortMessage off = new ShortMessage(ShortMessage.NOTE_OFF, MIDI_CHANNEL, midiNote, 0);
                    track.add(new MidiEvent(on, currentTick));
                    track.add(new MidiEvent(off, currentTick + noteTicks));
                }

                currentTick += noteTicks;
            }
        }

        // End of track
        MetaMessage endMsg = new MetaMessage();
        endMsg.setMessage(0x2F, new byte[0], 0);
        track.add(new MidiEvent(endMsg, currentTick));

        return sequence;
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
     * Calculează poziția în tick-uri pentru fiecare notă.
     * Folosit pentru cursor vizual.
     */
    public static List<NoteTiming> computeTimings(Score score) {
        List<NoteTiming> timings = new ArrayList<>();
        long currentTick = 0;
        int measureIdx = 0;
        for (Measure measure : score.measures()) {
            for (Note note : measure.notes()) {
                long noteTicks = computeTicks(note);
                timings.add(new NoteTiming(note, measureIdx, currentTick, currentTick + noteTicks));
                currentTick += noteTicks;
            }
            measureIdx++;
        }
        return timings;
    }

    public record NoteTiming(Note note, int measureIndex, long startTick, long endTick) {}
}