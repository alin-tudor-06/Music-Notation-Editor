package com.alin.musicnotation.musicxml.imports;

import com.alin.musicnotation.model.Duration;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.Pitch;
import com.alin.musicnotation.musicxml.common.MusicXmlPitchMapper;

import javax.xml.stream.XMLStreamConstants;

/**
 * Citește un element <note> din MusicXML.
 * Suportă: pitch (step, alter, octave), rest, duration, type, tie.
 * TODO: slur, articulations, tuplet.
 */
public class MusicXmlNoteReader {

    public Note readNote(MusicXmlReader xml, int divisions) throws Exception {
        // xml este poziționat la <note> START_ELEMENT
        boolean isRest = false;
        String step = null;
        Integer alter = null;
        Integer octave = null;
        String typeName = null;
        boolean tieStart = false;
        boolean tieEnd = false;

        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                switch (name) {
                    case "rest" -> {
                        isRest = true;
                        xml.skipElement();
                    }
                    case "pitch" -> {
                        PitchData pd = readPitch(xml);
                        step = pd.step();
                        alter = pd.alter();
                        octave = pd.octave();
                    }
                    case "type" -> typeName = xml.getElementText().trim();
                    case "tie" -> {
                        // <tie type="start"/> sau <tie type="stop"/>
                        String type = xml.getAttribute("type");
                        if ("start".equals(type)) tieStart = true;
                        if ("stop".equals(type)) tieEnd = true;
                        xml.skipElement();
                    }
                    default -> xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("note".equals(xml.getLocalName())) {
                    break;
                }
            }
        }

        Duration duration = mapType(typeName);

        if (isRest || step == null || octave == null) {
            return new Note(duration);
        }

        Pitch pitch = MusicXmlPitchMapper.toPitch(step, alter, octave);
        return new Note(pitch, duration, tieStart, tieEnd);
    }

    private PitchData readPitch(MusicXmlReader xml) throws Exception {
        String step = null;
        Integer alter = null;
        Integer octave = null;

        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                switch (name) {
                    case "step" -> step = xml.getElementText().trim();
                    case "alter" -> alter = Integer.parseInt(xml.getElementText().trim());
                    case "octave" -> octave = Integer.parseInt(xml.getElementText().trim());
                    default -> xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("pitch".equals(xml.getLocalName())) {
                    break;
                }
            }
        }

        return new PitchData(step, alter, octave);
    }

    private Duration mapType(String typeName) {
        if (typeName == null) return Duration.QUARTER;
        return switch (typeName) {
            case "whole" -> Duration.WHOLE;
            case "half" -> Duration.HALF;
            case "quarter" -> Duration.QUARTER;
            case "eighth" -> Duration.EIGHTH;
            case "16th", "sixteenth" -> Duration.SIXTEENTH;
            default -> Duration.QUARTER;
        };
    }

    private record PitchData(String step, Integer alter, Integer octave) {}
}