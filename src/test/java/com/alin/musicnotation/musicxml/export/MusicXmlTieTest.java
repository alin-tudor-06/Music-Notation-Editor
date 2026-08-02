package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.clef.ClefType;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class MusicXmlTieTest {

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        // Dezactivează încărcarea DTD-ului de pe internet (previne erorile de rețea)
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    @Test
    void testTieExport() throws Exception {
        Score score = new Score(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);

        Measure m = new Measure();
        m.addNote(new Note(Pitch.of("C4"), Duration.QUARTER, true, false));
        m.addNote(new Note(Pitch.of("C4"), Duration.QUARTER, false, true));
        score.addMeasure(m);
        score.validate();

        MusicXmlExporter exporter = new MusicXmlExporter();
        String xml = exporter.exportToString(score);
        Document doc = parseXml(xml);

        NodeList tieNodes = doc.getElementsByTagName("tie");
        assertEquals(2, tieNodes.getLength(), "Should have 2 tie elements");

        boolean hasStart = false;
        boolean hasStop = false;
        for (int i = 0; i < tieNodes.getLength(); i++) {
            String type = tieNodes.item(i).getAttributes().getNamedItem("type").getNodeValue();
            if ("start".equals(type)) hasStart = true;
            if ("stop".equals(type)) hasStop = true;
        }
        assertTrue(hasStart, "Missing tie start");
        assertTrue(hasStop, "Missing tie stop");

        NodeList tiedNodes = doc.getElementsByTagName("tied");
        assertEquals(2, tiedNodes.getLength(), "Should have 2 tied elements");

        boolean hasTiedStart = false;
        boolean hasTiedStop = false;
        for (int i = 0; i < tiedNodes.getLength(); i++) {
            String type = tiedNodes.item(i).getAttributes().getNamedItem("type").getNodeValue();
            if ("start".equals(type)) hasTiedStart = true;
            if ("stop".equals(type)) hasTiedStop = true;
        }
        assertTrue(hasTiedStart, "Missing tied start");
        assertTrue(hasTiedStop, "Missing tied stop");

        NodeList notationsNodes = doc.getElementsByTagName("notations");
        assertEquals(2, notationsNodes.getLength(), "Should have 2 notations elements (one per note)");
    }

    @Test
    void testNoTie() throws Exception {
        Score score = new Score(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);

        Measure m = new Measure();
        m.addNote(new Note(Pitch.of("C4"), Duration.QUARTER));
        m.addNote(new Note(Pitch.of("D4"), Duration.QUARTER));
        score.addMeasure(m);
        score.validate();

        MusicXmlExporter exporter = new MusicXmlExporter();
        String xml = exporter.exportToString(score);
        Document doc = parseXml(xml);

        NodeList tieNodes = doc.getElementsByTagName("tie");
        assertEquals(0, tieNodes.getLength(), "Should have no tie elements");

        NodeList tiedNodes = doc.getElementsByTagName("tied");
        assertEquals(0, tiedNodes.getLength(), "Should have no tied elements");

        NodeList notationsNodes = doc.getElementsByTagName("notations");
        assertEquals(0, notationsNodes.getLength(), "Should have no notations elements");
    }
}