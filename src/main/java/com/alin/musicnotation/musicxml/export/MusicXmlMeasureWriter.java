package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.Slur;
import com.alin.musicnotation.model.Tuplet;
import com.alin.musicnotation.model.articulation.Articulation;
import com.alin.musicnotation.model.dynamic.Dynamic;
import com.alin.musicnotation.model.hairpin.Hairpin;
import com.alin.musicnotation.model.text.TextExpression;
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

        // ---- Text Expressions ----
        for (TextExpression expr : measure.textExpressions()) {
            directionWriter.writeTextExpression(expr);
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

        // ---- Pregătire tuplete ----
        List<Tuplet> tuplets = measure.tuplets();
        Map<Note, Integer> noteTupletNumber = new HashMap<>();
        Map<Note, Boolean> noteTupletStart = new HashMap<>();
        Map<Note, Boolean> noteTupletStop = new HashMap<>();

        for (int i = 0; i < tuplets.size(); i++) {
            Tuplet t = tuplets.get(i);
            List<Note> tNotes = t.notes();
            if (tNotes.size() >= 2) {
                Note first = tNotes.get(0);
                Note last = tNotes.get(tNotes.size() - 1);
                int tupletNumber = i + 1;
                noteTupletNumber.put(first, tupletNumber);
                noteTupletNumber.put(last, tupletNumber);
                noteTupletStart.put(first, true);
                noteTupletStop.put(last, true);
            }
        }

        // ---- Pregătire hairpins ----
        List<Note> allNotes = measure.notes();
        List<Hairpin> hairpins = measure.hairpins();

        Map<Note, List<Hairpin>> hairpinStartsAt = new HashMap<>();
        Map<Note, List<Hairpin>> hairpinStopsBefore = new HashMap<>();
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

            List<Hairpin> stops = hairpinStopsBefore.get(note);
            if (stops != null) {
                for (Hairpin hp : stops) {
                    directionWriter.writeHairpinStop(hp);
                }
            }

            List<Hairpin> starts = hairpinStartsAt.get(note);
            if (starts != null) {
                for (Hairpin hp : starts) {
                    directionWriter.writeHairpinStart(hp);
                }
            }

            for (Dynamic dyn : measure.dynamics()) {
                if (dyn.note() == note) {
                    directionWriter.writeDynamic(dyn);
                }
            }

            int slurNumber = noteSlurNumber.getOrDefault(note, 0);
            boolean slurStart = noteSlurStart.getOrDefault(note, false);
            boolean slurStop = noteSlurStop.getOrDefault(note, false);

            int tupletNumber = noteTupletNumber.getOrDefault(note, 0);
            boolean tupletStart = noteTupletStart.getOrDefault(note, false);
            boolean tupletStop = noteTupletStop.getOrDefault(note, false);

            List<Articulation> articulations = measure.articulations().stream()
                    .filter(a -> a.note() == note)
                    .toList();

            noteWriter.writeNote(note, slurNumber, slurStart, slurStop,
                    articulations, tupletStart, tupletStop, tupletNumber);
        }

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