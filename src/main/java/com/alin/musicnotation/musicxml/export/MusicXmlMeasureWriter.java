package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.clef.ClefType;
import com.alin.musicnotation.musicxml.common.MusicXmlClefMapper;
import com.alin.musicnotation.musicxml.common.MusicXmlConstants;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicXmlMeasureWriter {

    private final XMLStreamWriter writer;
    private final MusicXmlNoteWriter noteWriter;

    public MusicXmlMeasureWriter(XMLStreamWriter writer) {
        this.writer = writer;
        this.noteWriter = new MusicXmlNoteWriter(writer);
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

        // ---- Note: pregătire pentru slurs ----
        List<Slur> slurs = measure.slurs();
        Map<Note, Integer> noteSlurNumber = new HashMap<>();
        Map<Note, Boolean> noteSlurStart = new HashMap<>();
        Map<Note, Boolean> noteSlurStop = new HashMap<>();

        // Pentru fiecare slur, identificăm prima și ultima notă
        for (int i = 0; i < slurs.size(); i++) {
            Slur slur = slurs.get(i);
            List<Note> notes = slur.notes();
            if (notes.size() >= 2) {
                Note first = notes.get(0);
                Note last = notes.get(notes.size() - 1);
                int slurNumber = i + 1; // numerotare de la 1
                noteSlurNumber.put(first, slurNumber);
                noteSlurNumber.put(last, slurNumber);
                noteSlurStart.put(first, true);
                noteSlurStop.put(last, true);
            }
        }

        // ---- Scriem notele ----
        List<Note> notes = measure.notes();
        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            int slurNumber = noteSlurNumber.getOrDefault(note, 0);
            boolean slurStart = noteSlurStart.getOrDefault(note, false);
            boolean slurStop = noteSlurStop.getOrDefault(note, false);

            noteWriter.writeNote(note, slurNumber, slurStart, slurStop);
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