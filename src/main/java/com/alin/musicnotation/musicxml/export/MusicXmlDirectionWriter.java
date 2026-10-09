package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.dynamic.Dynamic;
import com.alin.musicnotation.model.dynamic.DynamicPlacement;
import com.alin.musicnotation.model.tempo.Tempo;
import com.alin.musicnotation.model.tempo.TempoPlacement;
import com.alin.musicnotation.model.hairpin.Hairpin;
import com.alin.musicnotation.model.hairpin.HairpinPlacement;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Scrie elemente <direction> pentru MusicXML.
 * Folosit pentru dinamici, tempo, hairpin, text expressions.
 */
public class MusicXmlDirectionWriter {

    private final XMLStreamWriter writer;

    public MusicXmlDirectionWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    /**
     * Scrie o dinamică (p, mf, ff, etc.) ca element <direction>.
     */
    public void writeDynamic(Dynamic dyn) throws XMLStreamException {
        writer.writeStartElement("direction");

        DynamicPlacement placement = dyn.placement();
        if (placement == DynamicPlacement.AUTO) {
            placement = DynamicPlacement.BELOW;
        }
        String placementValue = (placement == DynamicPlacement.ABOVE) ? "above" : "below";
        writer.writeAttribute("placement", placementValue);

        writer.writeStartElement("direction-type");
        writer.writeStartElement("dynamics");

        String dynamicName = switch (dyn.type()) {
            case PPP -> "ppp";
            case PP -> "pp";
            case P -> "p";
            case MP -> "mp";
            case MF -> "mf";
            case F -> "f";
            case FF -> "ff";
            case FFF -> "fff";
        };

        writer.writeEmptyElement(dynamicName);

        writer.writeEndElement(); // dynamics
        writer.writeEndElement(); // direction-type
        writer.writeEndElement(); // direction
    }

    /**
     * Scrie un tempo ca element <direction>.
     * Suportă:
     * - text descriptiv (ex: "Allegro") -> <words>
     * - tempo numeric (ex: 120 BPM) -> <metronome>
     * - ambele combinate
     */
    public void writeTempo(Tempo tempo) throws XMLStreamException {
        if (tempo == null) {
            return;
        }

        writer.writeStartElement("direction");

        // Placement implicit: ABOVE
        TempoPlacement placement = tempo.placement();
        if (placement == TempoPlacement.BELOW) {
            writer.writeAttribute("placement", "below");
        } else {
            writer.writeAttribute("placement", "above");
        }

        // <direction-type> pentru text (dacă există)
        if (tempo.hasText()) {
            writer.writeStartElement("direction-type");
            writer.writeStartElement("words");
            writer.writeCharacters(tempo.text());
            writer.writeEndElement(); // words
            writer.writeEndElement(); // direction-type
        }

        // <direction-type> pentru metronom (dacă există BPM)
        if (tempo.hasBpm()) {
            writer.writeStartElement("direction-type");
            writer.writeStartElement("metronome");

            writer.writeStartElement("beat-unit");
            writer.writeCharacters("quarter");
            writer.writeEndElement(); // beat-unit

            writer.writeStartElement("per-minute");
            writer.writeCharacters(String.valueOf(tempo.bpm()));
            writer.writeEndElement(); // per-minute

            writer.writeEndElement(); // metronome
            writer.writeEndElement(); // direction-type
        }

        // <sound> pentru playback (opțional, dar recomandat)
        if (tempo.hasBpm()) {
            writer.writeStartElement("sound");
            writer.writeAttribute("tempo", String.valueOf(tempo.bpm()));
            writer.writeEndElement(); // sound
        }

        writer.writeEndElement(); // direction
    }

    public void writeHairpinStart(Hairpin hp) throws XMLStreamException {
        writer.writeStartElement("direction");

        // Placement
        HairpinPlacement placement = hp.placement();
        String placementValue = (placement == HairpinPlacement.ABOVE) ? "above" : "below";
        writer.writeAttribute("placement", placementValue);

        writer.writeStartElement("direction-type");
        writer.writeEmptyElement("wedge");
        // Crescendo -> "crescendo", Decrescendo -> "diminuendo"
        String wedgeType = switch (hp.type()) {
            case CRESCENDO -> "crescendo";
            case DECRESCENDO -> "diminuendo";
        };
        writer.writeAttribute("type", wedgeType);
        writer.writeEndElement(); // direction-type

        writer.writeEndElement(); // direction
    }

    /**
     * Scrie sfârșitul unui hairpin (stop).
     */
    public void writeHairpinStop(Hairpin hp) throws XMLStreamException {
        writer.writeStartElement("direction");

        HairpinPlacement placement = hp.placement();
        String placementValue = (placement == HairpinPlacement.ABOVE) ? "above" : "below";
        writer.writeAttribute("placement", placementValue);

        writer.writeStartElement("direction-type");
        writer.writeEmptyElement("wedge");
        writer.writeAttribute("type", "stop");
        writer.writeEndElement(); // direction-type

        writer.writeEndElement(); // direction
    }
}