package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.Slur;
import com.alin.musicnotation.model.articulation.Articulation;
import com.alin.musicnotation.model.dynamic.Dynamic;
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

    public MusicXmlMeasureWriter(XMLStreamWriter writer) {
        this.writer = writer;
        this.noteWriter = new MusicXmlNoteWriter(writer);
        this.directionWriter = new MusicXmlDirectionWriter(writer);
    }

    public void writeMeasure(Measure measure, int measureNumber,
                             ClefType currentClef, KeySignature currentKey, TimeSignature currentTime,
                             boolean isFirstMeasure) throws XMLStreamException {

        writer.writeStartElement("measure");
        writer.writeAttribute("number", String.valueOf(measureNumber));

        // ---- Attributes (clef, key, time) ----
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

        // ---- Pregătire pentru slurs ----
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

        // ---- Scriem notele ----
        for (Note note : measure.notes()) {

            // 1. Dinamici atașate acestei note (înainte de notă!)
            for (Dynamic dyn : measure.dynamics()) {
                if (dyn.note() == note) {
                    directionWriter.writeDynamic(dyn);
                }
            }

            // 2. Slur info
            int slurNumber = noteSlurNumber.getOrDefault(note, 0);
            boolean slurStart = noteSlurStart.getOrDefault(note, false);
            boolean slurStop = noteSlurStop.getOrDefault(note, false);

            // 3. Articulații atașate acestei note
            List<Articulation> articulations = measure.articulations().stream()
                    .filter(a -> a.note() == note)
                    .toList();

            // 4. Scriem nota
            noteWriter.writeNote(note, slurNumber, slurStart, slurStop, articulations);
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