package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.clef.ClefType;
import com.alin.musicnotation.model.articulation.*;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MusicXmlArticulationTest {

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    @Test
    void testArticulationsExport() throws Exception {
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
        m.addArticulation(new Articulation(n1, ArticulationType.STACCATO));
        m.addArticulation(new Articulation(n2, ArticulationType.ACCENT));
        m.addArticulation(new Articulation(n3, ArticulationType.TENUTO));
        m.addArticulation(new Articulation(n4, ArticulationType.MARCATO));
        score.addMeasure(m);
        score.validate();

        MusicXmlExporter exporter = new MusicXmlExporter();
        String xml = exporter.exportToString(score);
        Document doc = parseXml(xml);

        NodeList noteNodes = doc.getElementsByTagName("note");
        assertEquals(4, noteNodes.getLength());

        // Verifică fiecare articulație
        String[] expectedArtic = {"staccato", "accent", "tenuto", "marcato"};
        for (int i = 0; i < 4; i++) {
            Element note = (Element) noteNodes.item(i);
            NodeList notations = note.getElementsByTagName("notations");
            assertEquals(1, notations.getLength());
            Element notationsElem = (Element) notations.item(0);
            Element articulationsElem = (Element) notationsElem.getElementsByTagName("articulations").item(0);
            NodeList articNodes = articulationsElem.getElementsByTagName(expectedArtic[i]);
            assertEquals(1, articNodes.getLength());
        }
    }

    @Test
    void testArticulationPlacement() throws Exception {
        Score score = new Score(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);

        Measure m = new Measure();
        Note n1 = new Note(Pitch.of("C4"), Duration.QUARTER);
        Note n2 = new Note(Pitch.of("D4"), Duration.QUARTER);
        m.addNote(n1);
        m.addNote(n2);
        m.addArticulation(new Articulation(n1, ArticulationType.STACCATO, ArticulationPlacement.ABOVE));
        m.addArticulation(new Articulation(n2, ArticulationType.STACCATO, ArticulationPlacement.BELOW));
        score.addMeasure(m);
        score.validate();

        MusicXmlExporter exporter = new MusicXmlExporter();
        String xml = exporter.exportToString(score);
        Document doc = parseXml(xml);

        NodeList noteNodes = doc.getElementsByTagName("note");
        Element note1 = (Element) noteNodes.item(0);
        Element artic1 = (Element) ((Element) ((Element) note1.getElementsByTagName("notations").item(0))
                .getElementsByTagName("articulations").item(0))
                .getElementsByTagName("staccato").item(0);
        assertEquals("above", artic1.getAttribute("placement"));

        Element note2 = (Element) noteNodes.item(1);
        Element artic2 = (Element) ((Element) ((Element) note2.getElementsByTagName("notations").item(0))
                .getElementsByTagName("articulations").item(0))
                .getElementsByTagName("staccato").item(0);
        assertEquals("below", artic2.getAttribute("placement"));
    }

    @Test
    void testFermata() throws Exception {
        Score score = new Score(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);

        Measure m = new Measure();
        Note n = new Note(Pitch.of("C4"), Duration.WHOLE);
        m.addNote(n);
        m.addArticulation(new Articulation(n, ArticulationType.FERMATA));
        score.addMeasure(m);
        score.validate();

        MusicXmlExporter exporter = new MusicXmlExporter();
        String xml = exporter.exportToString(score);
        Document doc = parseXml(xml);

        NodeList noteNodes = doc.getElementsByTagName("note");
        Element note = (Element) noteNodes.item(0);
        Element artic = (Element) ((Element) ((Element) note.getElementsByTagName("notations").item(0))
                .getElementsByTagName("articulations").item(0))
                .getElementsByTagName("fermata").item(0);
        assertNotNull(artic);
    }
}