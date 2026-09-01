package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.clef.ClefType;
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

class MusicXmlSlurTest {

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    @Test
    void testSlurExport() throws Exception {
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
        m.addSlur(new Slur(List.of(n1, n2, n3, n4)));
        score.addMeasure(m);
        score.validate();

        MusicXmlExporter exporter = new MusicXmlExporter();
        String xml = exporter.exportToString(score);
        Document doc = parseXml(xml);

        // Verifică elementele <slur> în <note>
        NodeList slurNodes = doc.getElementsByTagName("slur");
        assertEquals(2, slurNodes.getLength(), "Should have 2 slur elements (start and stop)");

        boolean hasStart = false;
        boolean hasStop = false;
        for (int i = 0; i < slurNodes.getLength(); i++) {
            Element slur = (Element) slurNodes.item(i);
            String type = slur.getAttribute("type");
            String number = slur.getAttribute("number");
            assertEquals("1", number);
            if ("start".equals(type)) hasStart = true;
            if ("stop".equals(type)) hasStop = true;
        }
        assertTrue(hasStart, "Missing slur start");
        assertTrue(hasStop, "Missing slur stop");

        // Verifică <notations> pentru prima și ultima notă
        NodeList noteNodes = doc.getElementsByTagName("note");
        assertEquals(4, noteNodes.getLength());

        // Prima notă (index 0) trebuie să aibă slur start în notations
        Element firstNote = (Element) noteNodes.item(0);
        NodeList notationsFirst = firstNote.getElementsByTagName("notations");
        assertEquals(1, notationsFirst.getLength());
        Element notationsFirstElem = (Element) notationsFirst.item(0);
        NodeList slurInNotationsFirst = notationsFirstElem.getElementsByTagName("slur");
        assertEquals(1, slurInNotationsFirst.getLength());
        Element slurFirst = (Element) slurInNotationsFirst.item(0);
        assertEquals("start", slurFirst.getAttribute("type"));

        // Ultima notă (index 3) trebuie să aibă slur stop în notations
        Element lastNote = (Element) noteNodes.item(3);
        NodeList notationsLast = lastNote.getElementsByTagName("notations");
        assertEquals(1, notationsLast.getLength());
        Element notationsLastElem = (Element) notationsLast.item(0);
        NodeList slurInNotationsLast = notationsLastElem.getElementsByTagName("slur");
        assertEquals(1, slurInNotationsLast.getLength());
        Element slurLast = (Element) slurInNotationsLast.item(0);
        assertEquals("stop", slurLast.getAttribute("type"));
    }

    @Test
    void testMultipleSlurs() throws Exception {
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
        m.addSlur(new Slur(List.of(n1, n2)));      // slur 1
        m.addSlur(new Slur(List.of(n3, n4)));      // slur 2
        score.addMeasure(m);
        score.validate();

        MusicXmlExporter exporter = new MusicXmlExporter();
        String xml = exporter.exportToString(score);
        Document doc = parseXml(xml);

        NodeList slurNodes = doc.getElementsByTagName("slur");
        assertEquals(4, slurNodes.getLength(), "Should have 4 slur elements (2 starts + 2 stops)");

        // Verifică numerele
        for (int i = 0; i < slurNodes.getLength(); i++) {
            Element slur = (Element) slurNodes.item(i);
            String number = slur.getAttribute("number");
            assertTrue(number.equals("1") || number.equals("2"), "Slur number should be 1 or 2");
        }

        // Verifică că nota 1 are slur start cu number=1
        NodeList noteNodes = doc.getElementsByTagName("note");
        Element firstNote = (Element) noteNodes.item(0);
        NodeList notationsFirst = firstNote.getElementsByTagName("notations");
        assertEquals(1, notationsFirst.getLength());
        Element notationsFirstElem = (Element) notationsFirst.item(0);
        NodeList slurInFirst = notationsFirstElem.getElementsByTagName("slur");
        assertEquals(1, slurInFirst.getLength());
        Element slurFirst = (Element) slurInFirst.item(0);
        assertEquals("start", slurFirst.getAttribute("type"));
        assertEquals("1", slurFirst.getAttribute("number"));

        // Verifică că nota 2 are slur stop cu number=1
        Element secondNote = (Element) noteNodes.item(1);
        NodeList notationsSecond = secondNote.getElementsByTagName("notations");
        assertEquals(1, notationsSecond.getLength());
        Element notationsSecondElem = (Element) notationsSecond.item(0);
        NodeList slurInSecond = notationsSecondElem.getElementsByTagName("slur");
        assertEquals(1, slurInSecond.getLength());
        Element slurSecond = (Element) slurInSecond.item(0);
        assertEquals("stop", slurSecond.getAttribute("type"));
        assertEquals("1", slurSecond.getAttribute("number"));

        // Verifică că nota 3 are slur start cu number=2
        Element thirdNote = (Element) noteNodes.item(2);
        NodeList notationsThird = thirdNote.getElementsByTagName("notations");
        assertEquals(1, notationsThird.getLength());
        Element notationsThirdElem = (Element) notationsThird.item(0);
        NodeList slurInThird = notationsThirdElem.getElementsByTagName("slur");
        assertEquals(1, slurInThird.getLength());
        Element slurThird = (Element) slurInThird.item(0);
        assertEquals("start", slurThird.getAttribute("type"));
        assertEquals("2", slurThird.getAttribute("number"));

        // Verifică că nota 4 are slur stop cu number=2
        Element fourthNote = (Element) noteNodes.item(3);
        NodeList notationsFourth = fourthNote.getElementsByTagName("notations");
        assertEquals(1, notationsFourth.getLength());
        Element notationsFourthElem = (Element) notationsFourth.item(0);
        NodeList slurInFourth = notationsFourthElem.getElementsByTagName("slur");
        assertEquals(1, slurInFourth.getLength());
        Element slurFourth = (Element) slurInFourth.item(0);
        assertEquals("stop", slurFourth.getAttribute("type"));
        assertEquals("2", slurFourth.getAttribute("number"));
    }
}