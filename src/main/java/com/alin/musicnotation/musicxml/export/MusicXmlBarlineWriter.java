package com.alin.musicnotation.musicxml.export;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Scrie elemente <barline> pentru MusicXML.
 * Folosit pentru repeat signs (start/end repeat).
 */
public class MusicXmlBarlineWriter {

    private final XMLStreamWriter writer;

    public MusicXmlBarlineWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    /**
     * Scrie un <barline> cu <repeat direction="forward"/> (start repeat).
     * Plasat la începutul măsurii (location="left").
     */
    public void writeStartRepeat() throws XMLStreamException {
        writer.writeStartElement("barline");
        writer.writeAttribute("location", "left");

        writer.writeStartElement("barline-style");
        writer.writeCharacters("heavy-light");
        writer.writeEndElement();

        writer.writeStartElement("repeat");
        writer.writeAttribute("direction", "forward");
        writer.writeEndElement();

        writer.writeEndElement(); // barline
    }

    /**
     * Scrie un <barline> cu <repeat direction="backward"/> (end repeat).
     * Plasat la sfârșitul măsurii (location="right").
     */
    public void writeEndRepeat() throws XMLStreamException {
        writer.writeStartElement("barline");
        writer.writeAttribute("location", "right");

        writer.writeStartElement("barline-style");
        writer.writeCharacters("light-heavy");
        writer.writeEndElement();

        writer.writeStartElement("repeat");
        writer.writeAttribute("direction", "backward");
        writer.writeEndElement();

        writer.writeEndElement(); // barline
    }
}