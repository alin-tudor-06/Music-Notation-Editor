package com.alin.musicnotation.musicxml.imports;

import javax.xml.stream.XMLStreamConstants;

/**
 * Citește un element <barline> și extrage repeat signs.
 */
public class MusicXmlBarlineReader {

    /**
     * Citire <barline>. Returnează un BarlineInfo sau null dacă nu conține repeat.
     */
    public BarlineInfo readBarline(MusicXmlReader xml) throws Exception {
        String location = xml.getAttribute("location");
        String repeatDirection = null;

        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                if ("repeat".equals(name)) {
                    repeatDirection = xml.getAttribute("direction");
                    xml.skipElement();
                } else {
                    xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("barline".equals(xml.getLocalName())) break;
            }
        }

        if (repeatDirection == null) return null;
        return new BarlineInfo(location, repeatDirection);
    }

    /**
     * Info despre un barline: location ("left"/"right") + direction ("forward"/"backward").
     */
    public record BarlineInfo(String location, String direction) {}
}