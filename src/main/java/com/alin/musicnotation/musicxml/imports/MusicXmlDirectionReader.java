package com.alin.musicnotation.musicxml.imports;

import com.alin.musicnotation.model.dynamic.DynamicPlacement;
import com.alin.musicnotation.model.dynamic.DynamicType;

import javax.xml.stream.XMLStreamConstants;

/**
 * Citește un element <direction> și extrage dinamica (dacă există).
 * Returnează null dacă direction-ul nu conține o dinamică.
 */
public class MusicXmlDirectionReader {

    /**
     * Citire <direction>. Returnează informațiile despre dinamică sau null.
     */
    public DynamicInfo readDirection(MusicXmlReader xml) throws Exception {
        DynamicType type = null;
        DynamicPlacement placement = DynamicPlacement.AUTO;

        String placementAttr = xml.getAttribute("placement");
        if ("above".equals(placementAttr)) placement = DynamicPlacement.ABOVE;
        else if ("below".equals(placementAttr)) placement = DynamicPlacement.BELOW;

        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                if ("direction-type".equals(name)) {
                    // Coborâm în direction-type pentru a căuta dynamics
                    DynamicType found = readDirectionType(xml);
                    if (found != null) type = found;
                } else {
                    xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("direction".equals(xml.getLocalName())) break;
            }
        }

        if (type == null) return null;
        return new DynamicInfo(type, placement);
    }

    /**
     * Citește <direction-type> și caută <dynamics>.
     */
    private DynamicType readDirectionType(MusicXmlReader xml) throws Exception {
        DynamicType result = null;

        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                if ("dynamics".equals(name)) {
                    result = readDynamics(xml);
                } else {
                    xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("direction-type".equals(xml.getLocalName())) break;
            }
        }

        return result;
    }

    /**
     * Citește <dynamics> și extrage tipul.
     */
    private DynamicType readDynamics(MusicXmlReader xml) throws Exception {
        DynamicType result = null;

        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                DynamicType t = mapDynamicName(name);
                if (t != null) result = t;
                xml.skipElement();
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("dynamics".equals(xml.getLocalName())) break;
            }
        }

        return result;
    }

    private DynamicType mapDynamicName(String name) {
        return switch (name) {
            case "ppp" -> DynamicType.PPP;
            case "pp" -> DynamicType.PP;
            case "p" -> DynamicType.P;
            case "mp" -> DynamicType.MP;
            case "mf" -> DynamicType.MF;
            case "f" -> DynamicType.F;
            case "ff" -> DynamicType.FF;
            case "fff" -> DynamicType.FFF;
            default -> null;
        };
    }

    /**
     * Info despre o dinamică: tip + placement.
     */
    public record DynamicInfo(DynamicType type, DynamicPlacement placement) {}
}