package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.clef.ClefType;          // ← corect
import com.alin.musicnotation.model.KeySignature;
import com.alin.musicnotation.model.TimeSignature;
import com.alin.musicnotation.musicxml.common.MusicXmlClefMapper;
import com.alin.musicnotation.musicxml.common.MusicXmlConstants;
import com.alin.musicnotation.musicxml.common.MusicXmlDurationMapper;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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

        // Determinăm valorile corecte de scris
        ClefType clefToWrite = currentClef;
        KeySignature keyToWrite = currentKey;
        TimeSignature timeToWrite = currentTime;

        boolean hasClefChange = measure.clef() != null && !measure.clef().equals(currentClef);
        boolean hasKeyChange = measure.keySignature() != null && !measure.keySignature().equals(currentKey);
        boolean hasTimeChange = measure.timeSignature() != null && !measure.timeSignature().equals(currentTime);

        if (hasClefChange) {
            clefToWrite = measure.clef();
        }
        if (hasKeyChange) {
            keyToWrite = measure.keySignature();
        }
        if (hasTimeChange) {
            timeToWrite = measure.timeSignature();
        }

        // Scriem attributes dacă:
        // - este prima măsură
        // - sau există o schimbare
        if (isFirstMeasure || hasClefChange || hasKeyChange || hasTimeChange) {
            writer.writeStartElement("attributes");

            // Divisions
            writer.writeStartElement("divisions");
            writer.writeCharacters(String.valueOf(MusicXmlConstants.DIVISIONS));
            writer.writeEndElement();

            // Clef – se scrie întotdeauna în prima măsură sau la schimbare
            if (isFirstMeasure || hasClefChange) {
                writeClef(clefToWrite);
            }

            // Key – se scrie întotdeauna în prima măsură sau la schimbare
            if (isFirstMeasure || hasKeyChange) {
                writeKey(keyToWrite);
            }

            // Time – se scrie întotdeauna în prima măsură sau la schimbare
            if (isFirstMeasure || hasTimeChange) {
                writeTime(timeToWrite);
            }

            writer.writeEndElement(); // attributes
        }

        // Note
        for (var note : measure.notes()) {
            noteWriter.writeNote(note);
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