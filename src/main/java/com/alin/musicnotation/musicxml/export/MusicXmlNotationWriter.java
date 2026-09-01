package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.Note;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class MusicXmlNotationWriter {

    private final XMLStreamWriter writer;

    public MusicXmlNotationWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    public void writeNotations(Note note, int slurNumber, boolean slurStart, boolean slurStop) throws XMLStreamException {
        boolean hasTie = note.tieStart() || note.tieEnd();
        boolean hasSlur = slurStart || slurStop;

        if (!hasTie && !hasSlur) {
            return;
        }

        writer.writeStartElement("notations");

        // ---- Tied (legături de durată) ----
        if (note.tieStart()) {
            writer.writeStartElement("tied");
            writer.writeAttribute("type", "start");
            writer.writeEndElement();
        }
        if (note.tieEnd()) {
            writer.writeStartElement("tied");
            writer.writeAttribute("type", "stop");
            writer.writeEndElement();
        }

        // ---- Slur (legături de portament) ----
        if (slurStart) {
            writer.writeStartElement("slur");
            writer.writeAttribute("type", "start");
            writer.writeAttribute("number", String.valueOf(slurNumber));
            writer.writeEndElement();
        }
        if (slurStop) {
            writer.writeStartElement("slur");
            writer.writeAttribute("type", "stop");
            writer.writeAttribute("number", String.valueOf(slurNumber));
            writer.writeEndElement();
        }

        writer.writeEndElement(); // notations
    }
}