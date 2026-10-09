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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MusicXmlImporter {

    public Score importFromFile(Path file) throws Exception {
        byte[] content = Files.readAllBytes(file);

        if (isZip(content)) {
            System.out.println("📦 Detectat arhivă ZIP (.mxl real)");
            return importFromZip(content);
        } else {
            System.out.println("📄 Detectat XML text");
            try (InputStream is = new ByteArrayInputStream(content)) {
                return importFromStream(is);
            }
        }
    }

    public Score importFromString(String xml) throws Exception {
        try (InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            return importFromStream(is);
        }
    }

    /**
     * Detectează dacă fișierul este ZIP (începe cu "PK" = 0x50 0x4B).
     */
    private boolean isZip(byte[] data) {
        return data.length >= 2 && data[0] == 0x50 && data[1] == 0x4B;
    }

    /**
     * Import dintr-o arhivă ZIP.
     * 1. Caută META-INF/container.xml → află calea rootfile-ului.
     * 2. Extrage rootfile-ul.
     * 3. Fallback: orice .xml care nu e în META-INF/.
     */
    private Score importFromZip(byte[] zipData) throws Exception {
        byte[] containerXml = null;
        String rootFilePath = null;

        // Pas 1: Citim container.xml
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                if ("META-INF/container.xml".equalsIgnoreCase(entry.getName())) {
                    containerXml = zis.readAllBytes();
                    break;
                }
            }
        }

        if (containerXml != null) {
            rootFilePath = parseContainerRootFilePath(containerXml);
            System.out.println("📦 container.xml → rootfile: " + rootFilePath);
        }

        // Pas 2: Extragem rootfile-ul
        if (rootFilePath != null) {
            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.isDirectory()) continue;
                    if (entry.getName().equals(rootFilePath)) {
                        byte[] data = zis.readAllBytes();
                        System.out.println("📦 Extras: " + entry.getName());
                        try (InputStream is = new ByteArrayInputStream(data)) {
                            return importFromStream(is);
                        }
                    }
                }
            }
        }

        // Pas 3: Fallback - orice .xml care nu e META-INF/
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName().toLowerCase();
                if (name.endsWith(".xml") && !name.startsWith("meta-inf/")) {
                    byte[] data = zis.readAllBytes();
                    System.out.println("📦 Fallback extragere: " + entry.getName());
                    try (InputStream is = new ByteArrayInputStream(data)) {
                        return importFromStream(is);
                    }
                }
            }
        }

        throw new IllegalArgumentException("Arhiva nu conține MusicXML valid.");
    }

    private String parseContainerRootFilePath(byte[] containerXml) {
        try {
            javax.xml.stream.XMLInputFactory factory = javax.xml.stream.XMLInputFactory.newInstance();
            factory.setProperty(javax.xml.stream.XMLInputFactory.SUPPORT_DTD, false);
            var reader = factory.createXMLStreamReader(new ByteArrayInputStream(containerXml));

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT
                        && "rootfile".equals(reader.getLocalName())) {
                    String path = reader.getAttributeValue(null, "full-path");
                    if (path != null) return path;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Eroare parsare container.xml: " + e.getMessage());
        }
        return null;
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