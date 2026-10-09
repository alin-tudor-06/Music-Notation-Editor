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
                               List<Articulation> articulations,
                               boolean tupletStart, boolean tupletStop, int tupletNumber) throws XMLStreamException {
        boolean hasTie = note.tieStart() || note.tieEnd();
        boolean hasSlur = slurStart || slurStop;
        boolean hasArticulations = articulations != null && !articulations.isEmpty();
        boolean hasTuplet = tupletStart || tupletStop;

        if (!hasTie && !hasSlur && !hasArticulations && !hasTuplet) {
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

        // ---- Tuplet ----
        if (tupletStart) {
            writer.writeStartElement("tuplet");
            writer.writeAttribute("type", "start");
            writer.writeAttribute("number", String.valueOf(tupletNumber));
            writer.writeAttribute("bracket", "yes");
            writer.writeEndElement();
        }
        if (tupletStop) {
            writer.writeStartElement("tuplet");
            writer.writeAttribute("type", "stop");
            writer.writeAttribute("number", String.valueOf(tupletNumber));
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
        } else {
            if (art.type() == ArticulationType.MARCATO || art.type() == ArticulationType.FERMATA ||
                    art.type() == ArticulationType.ACCENT || art.type() == ArticulationType.TENUTO) {
                writer.writeAttribute("placement", "above");
            } else {
                writer.writeAttribute("placement", "below");
            }
        }

        if (art.type() == ArticulationType.FERMATA) {
            writer.writeAttribute("type",
                    art.placement() == ArticulationPlacement.BELOW ? "inverted" : "upright");
        }

        writer.writeEndElement();
    }
}