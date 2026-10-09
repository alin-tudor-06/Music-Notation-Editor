package com.alin.musicnotation.musicxml.imports;

import com.alin.musicnotation.model.dynamic.DynamicPlacement;
import com.alin.musicnotation.model.dynamic.DynamicType;
import com.alin.musicnotation.model.tempo.TempoPlacement;
import com.alin.musicnotation.model.text.TextPlacement;

import javax.xml.stream.XMLStreamConstants;

/**
 * Citește un element <direction> și extrage:
 * - dinamica (dacă există <dynamics>)
 * - tempo (dacă există <metronome>)
 * - text expression (dacă există <words> fără <metronome>)
 */
public class MusicXmlDirectionReader {

    /**
     * Citire <direction>. Returnează un DirectionInfo sau null dacă nimic relevant.
     */
    public DirectionInfo readDirection(MusicXmlReader xml) throws Exception {
        String placementAttr = xml.getAttribute("placement");
        boolean above = !"below".equals(placementAttr);

        DynamicType dynamicType = null;
        String words = null;
        Integer perMinute = null;
        String beatUnit = null;

        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                if ("direction-type".equals(name)) {
                    DirectionTypeContent content = readDirectionType(xml);
                    if (content.dynamicType() != null) dynamicType = content.dynamicType();
                    if (content.words() != null) words = content.words();
                    if (content.perMinute() != null) perMinute = content.perMinute();
                    if (content.beatUnit() != null) beatUnit = content.beatUnit();
                } else {
                    xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("direction".equals(xml.getLocalName())) break;
            }
        }

        // 1. Dinamică
        if (dynamicType != null) {
            DynamicPlacement dp = above ? DynamicPlacement.ABOVE : DynamicPlacement.BELOW;
            return new DirectionInfo(
                    new DynamicInfo(dynamicType, dp),
                    null,
                    null
            );
        }

        // 2. Tempo (are metronome)
        if (perMinute != null) {
            TempoPlacement tp = above ? TempoPlacement.ABOVE : TempoPlacement.BELOW;
            return new DirectionInfo(
                    null,
                    new TempoInfo(words, perMinute, tp),
                    null
            );
        }

        // 3. Text expression (doar words, fără metronome)
        if (words != null) {
            TextPlacement tpl = above ? TextPlacement.ABOVE : TextPlacement.BELOW;
            return new DirectionInfo(
                    null,
                    null,
                    new TextInfo(words, tpl)
            );
        }

        return null;
    }

    /**
     * Citește <direction-type>.
     */
    private DirectionTypeContent readDirectionType(MusicXmlReader xml) throws Exception {
        DynamicType dynamicType = null;
        String words = null;
        Integer perMinute = null;
        String beatUnit = null;

        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                switch (name) {
                    case "dynamics" -> dynamicType = readDynamics(xml);
                    case "words" -> words = xml.getElementText().trim();
                    case "metronome" -> {
                        MetronomeContent mc = readMetronome(xml);
                        perMinute = mc.perMinute();
                        beatUnit = mc.beatUnit();
                    }
                    default -> xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("direction-type".equals(xml.getLocalName())) break;
            }
        }

        return new DirectionTypeContent(dynamicType, words, perMinute, beatUnit);
    }

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

    private MetronomeContent readMetronome(MusicXmlReader xml) throws Exception {
        String beatUnit = null;
        Integer perMinute = null;

        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                switch (name) {
                    case "beat-unit" -> beatUnit = xml.getElementText().trim();
                    case "per-minute" -> perMinute = Integer.parseInt(xml.getElementText().trim());
                    default -> xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("metronome".equals(xml.getLocalName())) break;
            }
        }

        return new MetronomeContent(beatUnit, perMinute);
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

    private record DirectionTypeContent(
            DynamicType dynamicType,
            String words,
            Integer perMinute,
            String beatUnit
    ) {}

    private record MetronomeContent(String beatUnit, Integer perMinute) {}

    public record DynamicInfo(DynamicType type, DynamicPlacement placement) {}

    public record TempoInfo(String text, Integer bpm, TempoPlacement placement) {}

    public record TextInfo(String text, TextPlacement placement) {}

    public record DirectionInfo(DynamicInfo dynamic, TempoInfo tempo, TextInfo text) {}
}