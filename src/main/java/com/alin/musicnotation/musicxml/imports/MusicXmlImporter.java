package com.alin.musicnotation.musicxml.imports;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Score;
import com.alin.musicnotation.model.clef.ClefType;
import com.alin.musicnotation.model.KeySignature;
import com.alin.musicnotation.model.TimeSignature;

import javax.xml.stream.XMLStreamConstants;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrator pentru importul MusicXML.
 * API public: importFromFile(Path), importFromString(String).
 */
public class MusicXmlImporter {

    public Score importFromFile(Path file) throws Exception {
        try (InputStream is = Files.newInputStream(file)) {
            return importFromStream(is);
        }
    }

    public Score importFromString(String xml) throws Exception {
        try (InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            return importFromStream(is);
        }
    }

    private Score importFromStream(InputStream is) throws Exception {
        MusicXmlReader xml = new MusicXmlReader(is);
        MusicXmlMeasureReader measureReader = new MusicXmlMeasureReader();

        ClefType initialClef = null;
        KeySignature initialKey = null;
        TimeSignature initialTime = null;

        ClefType currentClef = null;
        KeySignature currentKey = null;
        TimeSignature currentTime = null;
        int currentDivisions = 1;

        List<Measure> measures = new ArrayList<>();
        boolean isFirstMeasure = true;

        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                if ("measure".equals(name)) {
                    MeasureReadResult result = measureReader.readMeasure(xml, currentDivisions);

                    if (isFirstMeasure) {
                        initialClef = result.clef() != null ? result.clef() : ClefType.TREBLE;
                        initialKey = result.key() != null ? result.key() : KeySignature.C_MAJOR;
                        initialTime = result.time() != null ? result.time() : TimeSignature.COMMON_TIME;
                        currentClef = initialClef;
                        currentKey = initialKey;
                        currentTime = initialTime;
                        isFirstMeasure = false;
                    } else {
                        if (result.clef() != null && !result.clef().equals(currentClef)) {
                            result.measure().addClef(result.clef());
                            currentClef = result.clef();
                        }
                        if (result.key() != null && !result.key().equals(currentKey)) {
                            result.measure().addKeySignature(result.key());
                            currentKey = result.key();
                        }
                        if (result.time() != null && !result.time().equals(currentTime)) {
                            result.measure().addTimeSignature(result.time());
                            currentTime = result.time();
                        }
                    }

                    if (result.divisions() != null) {
                        currentDivisions = result.divisions();
                    }

                    measures.add(result.measure());
                }
            }
        }

        if (initialClef == null) initialClef = ClefType.TREBLE;
        if (initialKey == null) initialKey = KeySignature.C_MAJOR;
        if (initialTime == null) initialTime = TimeSignature.COMMON_TIME;

        Score score = new Score(initialKey, initialClef, initialTime);
        for (Measure m : measures) {
            score.addMeasure(m);
        }
        return score;
    }
}