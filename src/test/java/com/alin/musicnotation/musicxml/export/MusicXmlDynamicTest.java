package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.clef.ClefType;
import com.alin.musicnotation.model.dynamic.*;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class MusicXmlDynamicTest {

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    @Test
    void testDynamicExport() throws Exception {
        Score score = new Score(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);

        Measure m = new Measure();
        Note n1 = new Note(Pitch.of("C4"), Duration.QUARTER);
        Note n2 = new Note(Pitch.of("D4"), Duration.QUARTER);
        Note n3 = new Note(Pitch.of("E4"), Duration.QUARTER);
        Note n4 = new Note(Pitch.of("F4"), Duration.QUARTER);
        m.addNote(n1);
        m.addNote(n2);
        m.addNote(n3);
        m.addNote(n4);
        m.addDynamic(new Dynamic(n1, DynamicType.P));
        m.addDynamic(new Dynamic(n2, DynamicType.MF));
        m.addDynamic(new Dynamic(n3, DynamicType.FF));
        m.addDynamic(new Dynamic(n4, DynamicType.PPP));
        score.addMeasure(m);
        score.validate();

        MusicXmlExporter exporter = new MusicXmlExporter();
        String xml = exporter.exportToString(score);
        Document doc = parseXml(xml);

        // 4 dinamici
        NodeList directions = doc.getElementsByTagName("direction");
        assertEquals(4, directions.getLength());

        // Verifică fiecare
        String[] expectedDynamics = {"p", "mf", "ff", "ppp"};
        for (int i = 0; i < 4; i++) {
            Element direction = (Element) directions.item(i);
            Element dynamics = (Element) direction.getElementsByTagName("dynamics").item(0);
            NodeList dynChildren = dynamics.getChildNodes();
            boolean found = false;
            for (int j = 0; j < dynChildren.getLength(); j++) {
                if (expectedDynamics[i].equals(dynChildren.item(j).getNodeName())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Missing dynamic: " + expectedDynamics[i]);
        }
    }

    @Test
    void testDynamicPlacement() throws Exception {
        Score score = new Score(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);

        Measure m = new Measure();
        Note n1 = new Note(Pitch.of("C4"), Duration.QUARTER);
        Note n2 = new Note(Pitch.of("D4"), Duration.QUARTER);
        m.addNote(n1);
        m.addNote(n2);
        m.addDynamic(new Dynamic(n1, DynamicType.F, DynamicPlacement.ABOVE));
        m.addDynamic(new Dynamic(n2, DynamicType.P, DynamicPlacement.BELOW));
        score.addMeasure(m);
        score.validate();

        MusicXmlExporter exporter = new MusicXmlExporter();
        String xml = exporter.exportToString(score);
        Document doc = parseXml(xml);

        NodeList directions = doc.getElementsByTagName("direction");
        assertEquals(2, directions.getLength());

        Element dir1 = (Element) directions.item(0);
        assertEquals("above", dir1.getAttribute("placement"));

        Element dir2 = (Element) directions.item(1);
        assertEquals("below", dir2.getAttribute("placement"));
    }
}