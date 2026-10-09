package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.Slur;
import com.alin.musicnotation.model.articulation.Articulation;
import com.alin.musicnotation.model.dynamic.Dynamic;
import com.alin.musicnotation.model.hairpin.Hairpin;
import com.alin.musicnotation.model.clef.ClefType;
import com.alin.musicnotation.model.KeySignature;
import com.alin.musicnotation.model.TimeSignature;
import com.alin.musicnotation.musicxml.common.MusicXmlClefMapper;
import com.alin.musicnotation.musicxml.common.MusicXmlConstants;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;

public class MusicXmlMeasureWriter {

    private final XMLStreamWriter writer;
    private final MusicXmlNoteWriter noteWriter;
    private final MusicXmlDirectionWriter directionWriter;
    private final MusicXmlBarlineWriter barlineWriter;

    public MusicXmlMeasureWriter(XMLStreamWriter writer) {
        this.writer = writer;
        this.noteWriter = new MusicXmlNoteWriter(writer);
        this.directionWriter = new MusicXmlDirectionWriter(writer);
        this.barlineWriter = new MusicXmlBarlineWriter(writer);
    }

    public void writeMeasure(Measure measure, int measureNumber,
                             ClefType currentClef, KeySignature currentKey, TimeSignature currentTime,
                             boolean isFirstMeasure) throws XMLStreamException {

        writer.writeStartElement("measure");
        writer.writeAttribute("number", String.valueOf(measureNumber));

        // ---- Start Repeat ----
        if (measure.hasStartRepeat()) {
            barlineWriter.writeStartRepeat();
        }

        // ---- Attributes ----
        ClefType clefToWrite = currentClef;
        KeySignature keyToWrite = currentKey;
        TimeSignature timeToWrite = currentTime;

        boolean hasClefChange = measure.clef() != null && !measure.clef().equals(currentClef);
        boolean hasKeyChange = measure.keySignature() != null && !measure.keySignature().equals(currentKey);
        boolean hasTimeChange = measure.timeSignature() != null && !measure.timeSignature().equals(currentTime);

        if (hasClefChange) clefToWrite = measure.clef();
        if (hasKeyChange) keyToWrite = measure.keySignature();
        if (hasTimeChange) timeToWrite = measure.timeSignature();

        if (isFirstMeasure || hasClefChange || hasKeyChange || hasTimeChange) {
            writer.writeStartElement("attributes");
            writer.writeStartElement("divisions");
            writer.writeCharacters(String.valueOf(MusicXmlConstants.DIVISIONS));
            writer.writeEndElement();
            if (isFirstMeasure || hasClefChange) writeClef(clefToWrite);
            if (isFirstMeasure || hasKeyChange) writeKey(keyToWrite);
            if (isFirstMeasure || hasTimeChange) writeTime(timeToWrite);
            writer.writeEndElement();
        }

        // ---- Tempo ----
        if (measure.tempo() != null) {
            directionWriter.writeTempo(measure.tempo());
        }

        // ---- Pregătire slurs ----
        List<Slur> slurs = measure.slurs();
        Map<Note, Integer> noteSlurNumber = new HashMap<>();
        Map<Note, Boolean> noteSlurStart = new HashMap<>();
        Map<Note, Boolean> noteSlurStop = new HashMap<>();

        for (int i = 0; i < slurs.size(); i++) {
            Slur slur = slurs.get(i);
            List<Note> notes = slur.notes();
            if (notes.size() >= 2) {
                Note first = notes.get(0);
                Note last = notes.get(notes.size() - 1);
                int slurNumber = i + 1;
                noteSlurNumber.put(first, slurNumber);
                noteSlurNumber.put(last, slurNumber);
                noteSlurStart.put(first, true);
                noteSlurStop.put(last, true);
            }
        }

        // ---- Pregătire hairpins ----
        List<Note> allNotes = measure.notes();
        List<Hairpin> hairpins = measure.hairpins();

        // hairpinStartsAt: note -> hairpins care încep la această notă
        Map<Note, List<Hairpin>> hairpinStartsAt = new HashMap<>();
        // hairpinStopsBefore: note -> hairpins care se termină ÎNAINTEA acestei note
        Map<Note, List<Hairpin>> hairpinStopsBefore = new HashMap<>();
        // hairpinsEndingAtMeasureEnd: hairpins care se termină pe ultima notă din măsură
        List<Hairpin> hairpinsEndingAtMeasureEnd = new ArrayList<>();

        for (Hairpin hp : hairpins) {
            hairpinStartsAt.computeIfAbsent(hp.startNote(), k -> new ArrayList<>()).add(hp);

            int endIdx = allNotes.indexOf(hp.endNote());
            if (endIdx >= 0) {
                if (endIdx + 1 < allNotes.size()) {
                    Note afterEnd = allNotes.get(endIdx + 1);
                    hairpinStopsBefore.computeIfAbsent(afterEnd, k -> new ArrayList<>()).add(hp);
                } else {
                    hairpinsEndingAtMeasureEnd.add(hp);
                }
            }
        }

        // ---- Scriem notele ----
        for (Note note : allNotes) {

            // 1. Hairpin stops (înaintea notei curente, dacă hairpin s-a terminat pe nota precedentă)
            List<Hairpin> stops = hairpinStopsBefore.get(note);
            if (stops != null) {
                for (Hairpin hp : stops) {
                    directionWriter.writeHairpinStop(hp);
                }
            }

            // 2. Hairpin starts (înaintea notei curente)
            List<Hairpin> starts = hairpinStartsAt.get(note);
            if (starts != null) {
                for (Hairpin hp : starts) {
                    directionWriter.writeHairpinStart(hp);
                }
            }

            // 3. Dinamici
            for (Dynamic dyn : measure.dynamics()) {
                if (dyn.note() == note) {
                    directionWriter.writeDynamic(dyn);
                }
            }

            // 4. Slur info
            int slurNumber = noteSlurNumber.getOrDefault(note, 0);
            boolean slurStart = noteSlurStart.getOrDefault(note, false);
            boolean slurStop = noteSlurStop.getOrDefault(note, false);

            // 5. Articulații
            List<Articulation> articulations = measure.articulations().stream()
                    .filter(a -> a.note() == note)
                    .toList();

            // 6. Nota
            noteWriter.writeNote(note, slurNumber, slurStart, slurStop, articulations);
        }

        // ---- Hairpin stops pentru hairpins care se termină pe ultima notă ----
        for (Hairpin hp : hairpinsEndingAtMeasureEnd) {
            directionWriter.writeHairpinStop(hp);
        }

        // ---- End Repeat ----
        if (measure.hasEndRepeat()) {
            barlineWriter.writeEndRepeat();
        }

        writer.writeEndElement(); // measure
    }


    private void writeClef(ClefType clef) throws XMLStreamException {
        writer.writeStartElement("clef");
        writer.writeStartElement("sign");
        writer.writeCharacters(MusicXmlClefMapper.getSign(clef));
        writer.writeEndElement();
        writer.writeStartElement("line");
        writer.writeCharacters(String.valueOf(MusicXmlClefMapper.getLine(clef)));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void writeKey(KeySignature key) throws XMLStreamException {
        writer.writeStartElement("key");
        writer.writeStartElement("fifths");
        writer.writeCharacters(String.valueOf(key.sharps()));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void writeTime(TimeSignature time) throws XMLStreamException {
        writer.writeStartElement("time");
        writer.writeStartElement("beats");
        writer.writeCharacters(String.valueOf(time.numerator()));
        writer.writeEndElement();
        writer.writeStartElement("beat-type");
        writer.writeCharacters(String.valueOf(time.denominator()));
        writer.writeEndElement();
        writer.writeEndElement();
    }
}