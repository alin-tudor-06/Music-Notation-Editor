package com.alin.musicnotation.musicxml.imports;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.Slur;
import com.alin.musicnotation.model.tempo.Tempo;
import com.alin.musicnotation.model.articulation.Articulation;
import com.alin.musicnotation.model.dynamic.Dynamic;
import com.alin.musicnotation.model.text.TextExpression;
import com.alin.musicnotation.model.clef.ClefType;
import com.alin.musicnotation.model.KeySignature;
import com.alin.musicnotation.model.TimeSignature;
import com.alin.musicnotation.musicxml.common.MusicXmlClefMapper;

import javax.xml.stream.XMLStreamConstants;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Citește un element <measure>.
 * Suportă: attributes, note, slur, articulations, dynamics, tempo, text.
 * TODO: barline (repeat), hairpin.
 */
public class MusicXmlMeasureReader {

    private final MusicXmlNoteReader noteReader = new MusicXmlNoteReader();
    private final MusicXmlDirectionReader directionReader = new MusicXmlDirectionReader();

    public MeasureReadResult readMeasure(MusicXmlReader xml, int currentDivisions) throws Exception {
        Measure measure = new Measure();

        ClefType parsedClef = null;
        KeySignature parsedKey = null;
        TimeSignature parsedTime = null;
        Integer parsedDivisions = null;
        int effectiveDivisions = currentDivisions;

        List<Note> addedNotes = new ArrayList<>();
        Map<Integer, Integer> slurStartIdx = new LinkedHashMap<>();
        Map<Integer, Integer> slurStopIdx = new LinkedHashMap<>();
        Map<Note, List<NoteReadResult.ArticulationInfo>> articulationsByNote = new LinkedHashMap<>();

        List<MusicXmlDirectionReader.DynamicInfo> pendingDynamics = new ArrayList<>();
        Map<Note, List<MusicXmlDirectionReader.DynamicInfo>> dynamicsByNote = new LinkedHashMap<>();

        // Tempo (poate fi doar unul pe măsură)
        MusicXmlDirectionReader.TempoInfo parsedTempo = null;
        // Text expressions (pot fi mai multe)
        List<MusicXmlDirectionReader.TextInfo> parsedTexts = new ArrayList<>();

        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                switch (name) {
                    case "attributes" -> {
                        AttributesResult attrs = readAttributes(xml);
                        if (attrs.divisions() != null) {
                            effectiveDivisions = attrs.divisions();
                            parsedDivisions = attrs.divisions();
                        }
                        if (attrs.clef() != null) parsedClef = attrs.clef();
                        if (attrs.key() != null) parsedKey = attrs.key();
                        if (attrs.time() != null) parsedTime = attrs.time();
                    }
                    case "direction" -> {
                        MusicXmlDirectionReader.DirectionInfo di = directionReader.readDirection(xml);
                        if (di != null) {
                            if (di.dynamic() != null) pendingDynamics.add(di.dynamic());
                            if (di.tempo() != null && parsedTempo == null) parsedTempo = di.tempo();
                            if (di.text() != null) parsedTexts.add(di.text());
                        }
                    }
                    case "note" -> {
                        NoteReadResult result = noteReader.readNote(xml, effectiveDivisions);
                        Note note = result.note();
                        measure.addNote(note);
                        int idx = addedNotes.size();
                        addedNotes.add(note);

                        for (NoteReadResult.SlurInfo si : result.slurs()) {
                            if (si.start()) slurStartIdx.put(si.number(), idx);
                            if (si.stop()) slurStopIdx.put(si.number(), idx);
                        }

                        if (!result.articulations().isEmpty()) {
                            articulationsByNote.put(note, result.articulations());
                        }

                        if (!pendingDynamics.isEmpty()) {
                            dynamicsByNote.put(note, new ArrayList<>(pendingDynamics));
                            pendingDynamics.clear();
                        }
                    }
                    default -> xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("measure".equals(xml.getLocalName())) {
                    break;
                }
            }
        }

        // Construim slurs
        for (Integer num : slurStartIdx.keySet()) {
            Integer start = slurStartIdx.get(num);
            Integer stop = slurStopIdx.get(num);
            if (start != null && stop != null && start < stop) {
                List<Note> slurNotes = new ArrayList<>(addedNotes.subList(start, stop + 1));
                measure.addSlur(new Slur(slurNotes));
            }
        }

        // Articulații
        for (Map.Entry<Note, List<NoteReadResult.ArticulationInfo>> entry : articulationsByNote.entrySet()) {
            Note note = entry.getKey();
            for (NoteReadResult.ArticulationInfo ai : entry.getValue()) {
                measure.addArticulation(new Articulation(note, ai.type(), ai.placement()));
            }
        }

        // Dinamici
        for (Map.Entry<Note, List<MusicXmlDirectionReader.DynamicInfo>> entry : dynamicsByNote.entrySet()) {
            Note note = entry.getKey();
            for (MusicXmlDirectionReader.DynamicInfo di : entry.getValue()) {
                measure.addDynamic(new Dynamic(note, di.type(), di.placement()));
            }
        }

        // Tempo
        if (parsedTempo != null) {
            Tempo tempo = new Tempo(parsedTempo.text(), parsedTempo.bpm(), parsedTempo.placement());
            measure.addTempo(tempo);
        }

        // Text expressions
        for (MusicXmlDirectionReader.TextInfo ti : parsedTexts) {
            measure.addTextExpression(new TextExpression(ti.text(), ti.placement()));
        }

        return new MeasureReadResult(measure, parsedClef, parsedKey, parsedTime, parsedDivisions);
    }

    private AttributesResult readAttributes(MusicXmlReader xml) throws Exception {
        Integer divisions = null;
        ClefType clef = null;
        KeySignature key = null;
        TimeSignature time = null;

        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                switch (name) {
                    case "divisions" -> divisions = Integer.parseInt(xml.getElementText().trim());
                    case "key" -> key = readKey(xml);
                    case "time" -> time = readTime(xml);
                    case "clef" -> clef = readClef(xml);
                    default -> xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("attributes".equals(xml.getLocalName())) break;
            }
        }

        return new AttributesResult(divisions, clef, key, time);
    }

    private KeySignature readKey(MusicXmlReader xml) throws Exception {
        Integer fifths = null;
        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("fifths".equals(xml.getLocalName())) {
                    fifths = Integer.parseInt(xml.getElementText().trim());
                } else {
                    xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("key".equals(xml.getLocalName())) break;
            }
        }
        return keySignatureFromFifths(fifths != null ? fifths : 0);
    }

    private TimeSignature readTime(MusicXmlReader xml) throws Exception {
        Integer beats = null;
        Integer beatType = null;
        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                switch (name) {
                    case "beats" -> beats = Integer.parseInt(xml.getElementText().trim());
                    case "beat-type" -> beatType = Integer.parseInt(xml.getElementText().trim());
                    default -> xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("time".equals(xml.getLocalName())) break;
            }
        }
        if (beats != null && beatType != null) {
            return new TimeSignature(beats, beatType);
        }
        return TimeSignature.COMMON_TIME;
    }

    private ClefType readClef(MusicXmlReader xml) throws Exception {
        String sign = null;
        Integer line = null;
        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                switch (name) {
                    case "sign" -> sign = xml.getElementText().trim();
                    case "line" -> line = Integer.parseInt(xml.getElementText().trim());
                    default -> xml.skipElement();
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if ("clef".equals(xml.getLocalName())) break;
            }
        }
        if (sign == null) return null;
        int effectiveLine = line != null ? line : defaultLineForSign(sign);
        try {
            return MusicXmlClefMapper.fromSignAndLine(sign, effectiveLine);
        } catch (IllegalArgumentException e) {
            return ClefType.TREBLE;
        }
    }

    private int defaultLineForSign(String sign) {
        return switch (sign) {
            case "G" -> 2;
            case "F" -> 4;
            case "C" -> 3;
            default -> 2;
        };
    }

    private KeySignature keySignatureFromFifths(int fifths) {
        return switch (fifths) {
            case 0 -> KeySignature.C_MAJOR;
            case 1 -> KeySignature.G_MAJOR;
            case 2 -> KeySignature.D_MAJOR;
            case 3 -> KeySignature.A_MAJOR;
            case 4 -> KeySignature.E_MAJOR;
            case 5 -> KeySignature.B_MAJOR;
            case 6 -> KeySignature.F_SHARP_MAJOR;
            case 7 -> KeySignature.C_SHARP_MAJOR;
            case -1 -> KeySignature.F_MAJOR;
            case -2 -> KeySignature.B_FLAT_MAJOR;
            case -3 -> KeySignature.E_FLAT_MAJOR;
            case -4 -> KeySignature.A_FLAT_MAJOR;
            case -5 -> KeySignature.D_FLAT_MAJOR;
            case -6 -> KeySignature.G_FLAT_MAJOR;
            case -7 -> KeySignature.C_FLAT_MAJOR;
            default -> KeySignature.C_MAJOR;
        };
    }

    private record AttributesResult(
            Integer divisions,
            ClefType clef,
            KeySignature key,
            TimeSignature time
    ) {}
}