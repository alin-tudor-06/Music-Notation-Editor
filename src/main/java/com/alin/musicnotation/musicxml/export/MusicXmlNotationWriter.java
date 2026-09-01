package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.articulation.Articulation;
import com.alin.musicnotation.model.articulation.ArticulationPlacement;
import com.alin.musicnotation.model.articulation.ArticulationType;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;

public class MusicXmlNotationWriter {

    private final XMLStreamWriter writer;

    public MusicXmlNotationWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    public void writeNotations(Note note, int slurNumber, boolean slurStart, boolean slurStop,
                               List<Articulation> articulations) throws XMLStreamException {
        boolean hasTie = note.tieStart() || note.tieEnd();
        boolean hasSlur = slurStart || slurStop;
        boolean hasArticulations = articulations != null && !articulations.isEmpty();

        if (!hasTie && !hasSlur && !hasArticulations) {
            return;
        }

        writer.writeStartElement("notations");

        // ---- Tied ----
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

        // ---- Slur ----
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

        // ---- Articulations ----
        if (hasArticulations) {
            writer.writeStartElement("articulations");
            for (Articulation art : articulations) {
                writeArticulation(art);
            }
            writer.writeEndElement();
        }

        writer.writeEndElement(); // notations
    }

    private void writeArticulation(Articulation art) throws XMLStreamException {
        String elementName = switch (art.type()) {
            case STACCATO -> "staccato";
            case STACCATISSIMO -> "staccatissimo";
            case ACCENT -> "accent";
            case MARCATO -> "strong-accent";
            case TENUTO -> "tenuto";
            case FERMATA -> "fermata";
        };

        writer.writeStartElement(elementName);

        if (art.placement() != ArticulationPlacement.AUTO) {
            String placement = art.placement() == ArticulationPlacement.ABOVE ? "above" : "below";
            writer.writeAttribute("placement", placement);
        }

        if (art.type() == ArticulationType.FERMATA) {
            writer.writeAttribute("type", "upright");
        }

        writer.writeEndElement();
    }
}