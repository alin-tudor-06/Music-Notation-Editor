package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.Note;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Scrie elementul <notations> pentru o notă.
 * Conține: tied, slur, articulations, tuplet.
 * Un singur <notations> pe notă, conform specificației MusicXML.
 */
public class MusicXmlNotationWriter {

    private final XMLStreamWriter writer;

    public MusicXmlNotationWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    /**
     * Scrie <notations> pentru nota dată.
     * Include doar elementele care sunt prezente (tie, slur, articulații, etc.).
     */
    public void writeNotations(Note note) throws XMLStreamException {
        boolean hasTie = note.tieStart() || note.tieEnd();

        if (!hasTie) {
            return; // nu scriem <notations> dacă nu e nimic
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

        // ---- Pe viitor: Slur, Articulations, Tuplet ----

        writer.writeEndElement(); // notations
    }
}