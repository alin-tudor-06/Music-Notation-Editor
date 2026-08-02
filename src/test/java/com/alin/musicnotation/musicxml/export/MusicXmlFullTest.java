package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.clef.ClefType;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MusicXmlFullTest {

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    @Test
    void testFullScoreExport() throws Exception {
        Score score = new Score(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);

        // Măsura 1: 4/4 - Do Re Mi Fa
        Measure m1 = new Measure();
        m1.addNote(new Note(Pitch.of("C4"), Duration.QUARTER));
        m1.addNote(new Note(Pitch.of("D4"), Duration.QUARTER));
        m1.addNote(new Note(Pitch.of("E4"), Duration.QUARTER));
        m1.addNote(new Note(Pitch.of("F4"), Duration.QUARTER));
        score.addMeasure(m1);

        // Măsura 2: 4/4 - tie + note
        Measure m2 = new Measure();
        m2.addNote(new Note(Pitch.of("G4"), Duration.QUARTER, true, false));
        m2.addNote(new Note(Pitch.of("G4"), Duration.QUARTER, false, true));
        m2.addNote(new Note(Pitch.of("A4"), Duration.QUARTER));
        m2.addNote(new Note(Pitch.of("B4"), Duration.QUARTER));
        score.addMeasure(m2);

        // Măsura 3: 3/4 - G major + timp 3/4
        Measure m3 = new Measure();
        m3.addKeySignature(KeySignature.G_MAJOR);
        m3.addTimeSignature(TimeSignature.THREE_FOUR);
        m3.addNote(new Note(Pitch.of("C5"), Duration.QUARTER));
        m3.addNote(new Note(Pitch.of("D5"), Duration.QUARTER));
        m3.addNote(new Note(Pitch.of("E5"), Duration.QUARTER));
        score.addMeasure(m3);

        // Măsura 4: 4/4 - Bass clef
        Measure m4 = new Measure();
        m4.addClef(ClefType.BASS);
        m4.addTimeSignature(TimeSignature.COMMON_TIME);
        m4.addNote(new Note(Pitch.of("C3"), Duration.QUARTER));
        m4.addNote(new Note(Pitch.of("D3"), Duration.QUARTER));
        m4.addNote(new Note(Pitch.of("E3"), Duration.QUARTER));
        m4.addNote(new Note(Pitch.of("F3"), Duration.QUARTER));
        score.addMeasure(m4);

        // Măsura 5: 4/4 - resturi
        Measure m5 = new Measure();
        m5.addNote(new Note(Duration.HALF));
        m5.addNote(new Note(Duration.QUARTER));
        m5.addNote(new Note(Duration.QUARTER));
        score.addMeasure(m5);

        // Măsura 6: 4/4 - accidentale
        Measure m6 = new Measure();
        m6.addNote(new Note(Pitch.of("C#4"), Duration.QUARTER));
        m6.addNote(new Note(Pitch.of("Db4"), Duration.QUARTER));
        m6.addNote(new Note(Pitch.of("Dn4"), Duration.QUARTER));
        m6.addNote(new Note(Pitch.of("E4"), Duration.QUARTER));
        score.addMeasure(m6);

        // Măsura 7: 4/4 - notă întreagă
        Measure m7 = new Measure();
        m7.addNote(new Note(Pitch.of("C4"), Duration.WHOLE));
        score.addMeasure(m7);

        // Măsura 8: 4/4 - optimi + pătrimi
        Measure m8 = new Measure();
        m8.addNote(new Note(Pitch.of("C4"), Duration.EIGHTH));
        m8.addNote(new Note(Pitch.of("D4"), Duration.EIGHTH));
        m8.addNote(new Note(Pitch.of("E4"), Duration.EIGHTH));
        m8.addNote(new Note(Pitch.of("F4"), Duration.EIGHTH));
        m8.addNote(new Note(Pitch.of("G4"), Duration.QUARTER));
        m8.addNote(new Note(Pitch.of("A4"), Duration.QUARTER));
        score.addMeasure(m8);

        score.validate();

        MusicXmlExporter exporter = new MusicXmlExporter();
        String xml = exporter.exportToString(score);
        Document doc = parseXml(xml);

        // ============================================================
        // 1. Elementul rădăcină și versiunea
        // ============================================================
        assertEquals("score-partwise", doc.getDocumentElement().getNodeName());
        assertEquals("4.0", doc.getDocumentElement().getAttribute("version"));

        // ============================================================
        // 2. Part și part-list
        // ============================================================
        NodeList partNodes = doc.getElementsByTagName("part");
        assertEquals(1, partNodes.getLength());
        assertEquals("P1", partNodes.item(0).getAttributes().getNamedItem("id").getNodeValue());

        // ============================================================
        // 3. Numărul de măsuri (8)
        // ============================================================
        NodeList measureNodes = doc.getElementsByTagName("measure");
        assertEquals(8, measureNodes.getLength());
        for (int i = 1; i <= 8; i++) {
            assertEquals(String.valueOf(i), measureNodes.item(i - 1).getAttributes().getNamedItem("number").getNodeValue());
        }

        // ============================================================
        // 4. Divisions – verificăm că există cel puțin un element
        //    și că toate au valoarea 16.
        // ============================================================
        NodeList divisionsNodes = doc.getElementsByTagName("divisions");
        assertTrue(divisionsNodes.getLength() >= 1, "Should have at least one divisions element");
        for (int i = 0; i < divisionsNodes.getLength(); i++) {
            assertEquals("16", divisionsNodes.item(i).getTextContent(), "All divisions should be 16");
        }

        // ============================================================
        // 5. Numărul total de note (29)
        // ============================================================
        NodeList noteNodes = doc.getElementsByTagName("note");
        assertEquals(29, noteNodes.getLength());

        // ============================================================
        // 6. Pitch-urile primelor 4 note (C4, D4, E4, F4)
        // ============================================================
        String[] expectedSteps = {"C", "D", "E", "F"};
        for (int i = 0; i < 4; i++) {
            Node noteNode = noteNodes.item(i);
            // Găsim elementul <pitch> copil al <note>
            NodeList noteChildren = noteNode.getChildNodes();
            Node pitchNode = null;
            for (int j = 0; j < noteChildren.getLength(); j++) {
                if ("pitch".equals(noteChildren.item(j).getNodeName())) {
                    pitchNode = noteChildren.item(j);
                    break;
                }
            }
            assertNotNull(pitchNode, "No pitch element for note " + (i + 1));
            // Căutăm <step> în interiorul <pitch>
            NodeList pitchChildren = pitchNode.getChildNodes();
            boolean foundStep = false;
            for (int j = 0; j < pitchChildren.getLength(); j++) {
                if ("step".equals(pitchChildren.item(j).getNodeName())) {
                    assertEquals(expectedSteps[i], pitchChildren.item(j).getTextContent());
                    foundStep = true;
                    break;
                }
            }
            assertTrue(foundStep, "Missing step for note " + (i + 1));
        }

        // ============================================================
        // 7. Tie (măsura 2 - notele 5 și 6)
        // ============================================================
        NodeList tieNodes = doc.getElementsByTagName("tie");
        assertEquals(2, tieNodes.getLength());
        String[] tieTypes = {"start", "stop"};
        for (int i = 0; i < 2; i++) {
            String type = tieNodes.item(i).getAttributes().getNamedItem("type").getNodeValue();
            assertEquals(tieTypes[i], type);
        }

        NodeList tiedNodes = doc.getElementsByTagName("tied");
        assertEquals(2, tiedNodes.getLength());
        for (int i = 0; i < 2; i++) {
            String type = tiedNodes.item(i).getAttributes().getNamedItem("type").getNodeValue();
            assertEquals(tieTypes[i], type);
        }

        // ============================================================
        // 8. Schimbarea de armură (măsura 3 - G major: fifths=1)
        // ============================================================
        NodeList keyNodes = doc.getElementsByTagName("key");
        assertEquals(2, keyNodes.getLength(), "Should have 2 key elements (measure 1 and 3)");

        NodeList fifthsNodes = doc.getElementsByTagName("fifths");
        assertEquals(2, fifthsNodes.getLength());

        List<String> fifthsValues = new ArrayList<>();
        for (int i = 0; i < fifthsNodes.getLength(); i++) {
            fifthsValues.add(fifthsNodes.item(i).getTextContent());
        }

        assertTrue(fifthsValues.contains("0"), "Expected fifths=0 for C major");
        assertTrue(fifthsValues.contains("1"), "Expected fifths=1 for G major");

        // ============================================================
        // 9. Schimbarea de timp (măsura 3 - 3/4)
        // ============================================================
        NodeList timeNodes = doc.getElementsByTagName("time");
        assertEquals(3, timeNodes.getLength()); // m1 4/4 și m3 3/4

        NodeList beatsNodes = doc.getElementsByTagName("beats");
        assertEquals(3, beatsNodes.getLength());
        assertEquals("4", beatsNodes.item(0).getTextContent());
        assertEquals("3", beatsNodes.item(1).getTextContent());

        NodeList beatTypeNodes = doc.getElementsByTagName("beat-type");
        assertEquals(3, beatTypeNodes.getLength());
        assertEquals("4", beatTypeNodes.item(0).getTextContent());
        assertEquals("4", beatTypeNodes.item(1).getTextContent());

        // ============================================================
        // 10. Schimbarea de cheie (măsura 4 - Bass)
        // ============================================================
        NodeList clefNodes = doc.getElementsByTagName("clef");
        assertEquals(2, clefNodes.getLength()); // m1 și m4

        assertEquals("G", clefNodes.item(0).getFirstChild().getTextContent());
        NodeList lineNodes = doc.getElementsByTagName("line");
        assertEquals(2, lineNodes.getLength());
        assertEquals("2", lineNodes.item(0).getTextContent());

        assertEquals("F", clefNodes.item(1).getFirstChild().getTextContent());
        assertEquals("4", lineNodes.item(1).getTextContent());

        // ============================================================
        // 11. Resturi (măsura 5 are 3 resturi: half, quarter, quarter)
        // ============================================================
        NodeList restNodes = doc.getElementsByTagName("rest");
        assertEquals(3, restNodes.getLength());
    }
}