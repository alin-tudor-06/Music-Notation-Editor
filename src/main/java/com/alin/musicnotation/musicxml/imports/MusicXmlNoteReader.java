package com.alin.musicnotation.musicxml.imports;

import com.alin.musicnotation.model.Accidental;
import com.alin.musicnotation.model.Duration;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.Pitch;
import com.alin.musicnotation.model.articulation.ArticulationPlacement;
import com.alin.musicnotation.model.articulation.ArticulationType;

import javax.xml.stream.XMLStreamConstants;
import java.util.ArrayList;
import java.util.List;

public class MusicXmlNoteReader {

    public NoteReadResult readNote(MusicXmlReader xml, int divisions) throws Exception {
        boolean isRest = false;
        String step = null;
        Integer alter = null;
        Integer octave = null;
        String typeName = null;
        boolean tieStart = false;
        boolean tieEnd = false;
        String accidentalHint = null;
        List<NoteReadResult.SlurInfo> slurs = new ArrayList<>();
        List<NoteReadResult.ArticulationInfo> articulations = new ArrayList<>();

        // Tuplet state
        Integer actualNotes = null;
        Integer normalNotes = null;
        Integer tupletStartNumber = null;
        Integer tupletStopNumber = null;

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
                    case "accidental" -> accidentalHint = xml.getElementText().trim();
                    case "tie" -> {
                        String type = xml.getAttribute("type");
                        if ("start".equals(type)) tieStart = true;
                        if ("stop".equals(type)) tieEnd = true;
                        xml.skipElement();
                    }
                    case "time-modification" -> {
                        TimeModResult tm = readTimeModification(xml);
                        actualNotes = tm.actual();
                        normalNotes = tm.normal();
                    }
                    case "notations" -> {
                        NotationsResult nr = readNotations(xml);
                        slurs.addAll(nr.slurs());
                        articulations.addAll(nr.articulations());
                        if (nr.tupletStart() != null) tupletStartNumber = nr.tupletStart();
                        if (nr.tupletStop() != null) tupletStopNumber = nr.tupletStop();
                    }
                    default -> xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("note".equals(xml.getLocalName())) break;
            }
        }

        Duration duration = mapType(typeName);
        Note note;

        if (isRest || step == null || octave == null) {
            note = new Note(duration);
        } else {
            Accidental accidental = determineAccidental(alter, accidentalHint);
            Pitch pitch = new Pitch(Pitch.Step.valueOf(step), accidental, octave);
            note = new Note(pitch, duration, tieStart, tieEnd);
        }

        // Construim tuplet info dacă există
        NoteReadResult.TupletInfo tupletInfo = null;
        if (actualNotes != null && normalNotes != null) {
            int number = 1;
            boolean isStart = false;
            boolean isStop = false;
            if (tupletStartNumber != null) { number = tupletStartNumber; isStart = true; }
            if (tupletStopNumber != null) { number = tupletStopNumber; isStop = true; }
            tupletInfo = new NoteReadResult.TupletInfo(actualNotes, normalNotes, number, isStart, isStop);
        }

        return new NoteReadResult(note, slurs, articulations, tupletInfo);
    }

    /**
     * Determină accidentalul corect al pitch-ului.
     * - Dacă MusicXML are elementul <accidental>, îl folosim ca hint pentru afișare.
     * - Altfel, deducem din <alter>:
     *   null = fără accidental, 0 = natural, >0 = sharp, <0 = flat.
     */
    private Accidental determineAccidental(Integer alter, String accidentalHint) {
        if (accidentalHint != null) {
            return switch (accidentalHint) {
                case "sharp" -> Accidental.SHARP;
                case "flat" -> Accidental.FLAT;
                case "natural" -> Accidental.NATURAL;
                default -> Accidental.NONE;
            };
        }
        if (alter == null) return Accidental.NONE;
        if (alter == 0) return Accidental.NATURAL;
        if (alter > 0) return Accidental.SHARP;
        return Accidental.FLAT;
    }

    private TimeModResult readTimeModification(MusicXmlReader xml) throws Exception {
        Integer actual = null;
        Integer normal = null;
        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                switch (name) {
                    case "actual-notes" -> actual = Integer.parseInt(xml.getElementText().trim());
                    case "normal-notes" -> normal = Integer.parseInt(xml.getElementText().trim());
                    default -> xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("time-modification".equals(xml.getLocalName())) break;
            }
        }
        return new TimeModResult(actual, normal);
    }

    private NotationsResult readNotations(MusicXmlReader xml) throws Exception {
        List<NoteReadResult.SlurInfo> slurs = new ArrayList<>();
        List<NoteReadResult.ArticulationInfo> articulations = new ArrayList<>();
        Integer tupletStart = null;
        Integer tupletStop = null;

        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                switch (name) {
                    case "slur" -> {
                        String type = xml.getAttribute("type");
                        String numberStr = xml.getAttribute("number");
                        int number = 1;
                        if (numberStr != null) {
                            try { number = Integer.parseInt(numberStr); } catch (NumberFormatException e) { number = 1; }
                        }
                        slurs.add(new NoteReadResult.SlurInfo(number, "start".equals(type), "stop".equals(type)));
                        xml.skipElement();
                    }
                    case "tuplet" -> {
                        String type = xml.getAttribute("type");
                        String numberStr = xml.getAttribute("number");
                        int number = 1;
                        if (numberStr != null) {
                            try { number = Integer.parseInt(numberStr); } catch (NumberFormatException e) { number = 1; }
                        }
                        if ("start".equals(type)) tupletStart = number;
                        else if ("stop".equals(type)) tupletStop = number;
                        xml.skipElement();
                    }
                    case "articulations" -> readArticulations(xml, articulations);
                    default -> xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("notations".equals(xml.getLocalName())) break;
            }
        }

        return new NotationsResult(slurs, articulations, tupletStart, tupletStop);
    }

    private void readArticulations(MusicXmlReader xml,
                                   List<NoteReadResult.ArticulationInfo> articulations) throws Exception {
        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                ArticulationType type = mapArticulationName(name);
                if (type != null) {
                    ArticulationPlacement placement = parsePlacement(xml.getAttribute("placement"));
                    articulations.add(new NoteReadResult.ArticulationInfo(type, placement));
                }
                xml.skipElement();
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("articulations".equals(xml.getLocalName())) break;
            }
        }
    }

    private ArticulationType mapArticulationName(String name) {
        return switch (name) {
            case "staccato" -> ArticulationType.STACCATO;
            case "staccatissimo" -> ArticulationType.STACCATISSIMO;
            case "accent" -> ArticulationType.ACCENT;
            case "strong-accent", "marcato" -> ArticulationType.MARCATO;
            case "tenuto" -> ArticulationType.TENUTO;
            case "fermata" -> ArticulationType.FERMATA;
            default -> null;
        };
    }

    private ArticulationPlacement parsePlacement(String placementStr) {
        if (placementStr != null) {
            if ("above".equals(placementStr)) return ArticulationPlacement.ABOVE;
            if ("below".equals(placementStr)) return ArticulationPlacement.BELOW;
        }
        return ArticulationPlacement.AUTO;
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
    private record TimeModResult(Integer actual, Integer normal) {}
    private record NotationsResult(
            List<NoteReadResult.SlurInfo> slurs,
            List<NoteReadResult.ArticulationInfo> articulations,
            Integer tupletStart,
            Integer tupletStop
    ) {}
}