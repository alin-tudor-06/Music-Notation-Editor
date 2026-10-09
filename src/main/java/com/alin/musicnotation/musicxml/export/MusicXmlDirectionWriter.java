package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.dynamic.Dynamic;
import com.alin.musicnotation.model.dynamic.DynamicPlacement;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Scrie elemente <direction> pentru MusicXML.
 * Folosit pentru dinamici, hairpin, tempo, text expressions.
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

        // Placement (implicit below dacă este AUTO)
        DynamicPlacement placement = dyn.placement();
        if (placement == DynamicPlacement.AUTO) {
            placement = DynamicPlacement.BELOW;
        }
        String placementValue = (placement == DynamicPlacement.ABOVE) ? "above" : "below";
        writer.writeAttribute("placement", placementValue);

        // direction-type > dynamics > <p/> etc.
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
}