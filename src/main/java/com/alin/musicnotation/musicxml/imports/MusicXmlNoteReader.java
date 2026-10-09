package com.alin.musicnotation.musicxml.imports;

import com.alin.musicnotation.model.Duration;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.Pitch;
import com.alin.musicnotation.musicxml.common.MusicXmlPitchMapper;

import javax.xml.stream.XMLStreamConstants;
import java.util.ArrayList;
import java.util.List;

/**
 * Citește un element <note> din MusicXML.
 * Suportă: pitch, rest, duration, type, tie, slur.
 * TODO: articulations, tuplet.
 */
public class MusicXmlNoteReader {

    public NoteReadResult readNote(MusicXmlReader xml, int divisions) throws Exception {
        boolean isRest = false;
        String step = null;
        Integer alter = null;
        Integer octave = null;
        String typeName = null;
        boolean tieStart = false;
        boolean tieEnd = false;
        List<NoteReadResult.SlurInfo> slurs = new ArrayList<>();

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
                        String type = xml.getAttribute("type");
                        if ("start".equals(type)) tieStart = true;
                        if ("stop".equals(type)) tieEnd = true;
                        xml.skipElement();
                    }
                    case "notations" -> slurs.addAll(readNotations(xml));
                    default -> xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("note".equals(xml.getLocalName())) {
                    break;
                }
            }
        }

        Duration duration = mapType(typeName);
        Note note;

        if (isRest || step == null || octave == null) {
            note = new Note(duration);
        } else {
            Pitch pitch = MusicXmlPitchMapper.toPitch(step, alter, octave);
            note = new Note(pitch, duration, tieStart, tieEnd);
        }

        return new NoteReadResult(note, slurs);
    }

    /**
     * Citește <notations> și extrage slur start/stop.
     */
    private List<NoteReadResult.SlurInfo> readNotations(MusicXmlReader xml) throws Exception {
        List<NoteReadResult.SlurInfo> slurs = new ArrayList<>();

        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                if ("slur".equals(name)) {
                    String type = xml.getAttribute("type");
                    String numberStr = xml.getAttribute("number");
                    int number = 1;
                    if (numberStr != null) {
                        try { number = Integer.parseInt(numberStr); } catch (NumberFormatException e) { number = 1; }
                    }
                    boolean start = "start".equals(type);
                    boolean stop = "stop".equals(type);
                    slurs.add(new NoteReadResult.SlurInfo(number, start, stop));
                    xml.skipElement();
                } else {
                    xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("notations".equals(xml.getLocalName())) break;
            }
        }

        return slurs;
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
                if ("pitch".equals(xml.getLocalName())) break;
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