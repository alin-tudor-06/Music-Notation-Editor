package com.alin.musicnotation.musicxml.imports;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

/**
 * Wrapper subțire peste XMLStreamReader (StAX).
 * Oferă metode helper pentru navigarea documentului MusicXML.
 */
public class MusicXmlReader {

    private final XMLStreamReader reader;

    public MusicXmlReader(InputStream is) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        // Dezactivează încărcarea DTD-ului extern (previne erori de rețea)
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
        this.reader = factory.createXMLStreamReader(is);
    }

    public boolean hasNext() throws XMLStreamException {
        return reader.hasNext();
    }

    public int next() throws XMLStreamException {
        return reader.next();
    }

    public String getLocalName() {
        return reader.getLocalName();
    }

    public String getAttribute(String name) {
        return reader.getAttributeValue(null, name);
    }

    /**
     * Citește textul elementului curent (START_ELEMENT) și avansează la END_ELEMENT.
     */
    public String getElementText() throws XMLStreamException {
        return reader.getElementText();
    }

    /**
     * Sari peste elementul curent (trebuie să fii la START_ELEMENT).
     * Lasă reader-ul poziționat la END_ELEMENT al elementului sărit.
     */
    public void skipElement() throws XMLStreamException {
        int depth = 1;
        while (depth > 0 && reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) depth++;
            else if (event == XMLStreamConstants.END_ELEMENT) depth--;
        }
    }
}