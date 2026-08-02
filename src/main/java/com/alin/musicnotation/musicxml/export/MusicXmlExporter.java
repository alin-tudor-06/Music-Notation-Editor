package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Score;
import com.alin.musicnotation.model.clef.ClefType;
import com.alin.musicnotation.model.KeySignature;
import com.alin.musicnotation.model.TimeSignature;
import com.alin.musicnotation.musicxml.common.MusicXmlConstants;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.function.Supplier;

public class MusicXmlExporter {

    private Supplier<LocalDate> dateSupplier = LocalDate::now;

    public void export(Score score, Path outputFile) throws Exception {
        try (OutputStream out = Files.newOutputStream(outputFile)) {
            exportToStream(score, out);
        }
    }

    public String exportToString(Score score) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            exportToStream(score, out);
            return out.toString(MusicXmlConstants.ENCODING);
        }
    }

    void setDateSupplier(Supplier<LocalDate> dateSupplier) {
        this.dateSupplier = dateSupplier;
    }

    private void exportToStream(Score score, OutputStream out) throws XMLStreamException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(out, MusicXmlConstants.ENCODING);

        try {
            writer.writeStartDocument(MusicXmlConstants.ENCODING, MusicXmlConstants.XML_VERSION);
            writer.writeDTD("<!DOCTYPE score-partwise PUBLIC \"-//Recordare//DTD MusicXML 4.0 Partwise//EN\"\n"
                    + "\"http://www.musicxml.org/dtds/partwise.dtd\">");

            writer.writeStartElement("score-partwise");
            writer.writeDefaultNamespace("http://www.musicxml.org/xsd/MusicXML.xsd");
            writer.writeAttribute("version", MusicXmlConstants.VERSION);

            writeIdentification(writer);

            writer.writeStartElement("part-list");
            writer.writeStartElement("score-part");
            writer.writeAttribute("id", MusicXmlConstants.PART_ID);
            writer.writeStartElement("part-name");
            writer.writeCharacters(MusicXmlConstants.DEFAULT_PART_NAME);
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();

            writer.writeStartElement("part");
            writer.writeAttribute("id", MusicXmlConstants.PART_ID);

            MusicXmlContext context = new MusicXmlContext(
                    score.initialClef(),
                    score.keySignature(),
                    score.initialTimeSignature()
            );

            MusicXmlMeasureWriter measureWriter = new MusicXmlMeasureWriter(writer);

            int measureNumber = 1;
            for (Measure measure : score.measures()) {
                ClefType currentClef = context.getCurrentClef();
                KeySignature currentKey = context.getCurrentKey();
                TimeSignature currentTime = context.getCurrentTime();

                measureWriter.writeMeasure(
                        measure,
                        measureNumber,
                        currentClef,
                        currentKey,
                        currentTime,
                        measureNumber == 1
                );

                if (measure.clef() != null) context.setCurrentClef(measure.clef());
                if (measure.keySignature() != null) context.setCurrentKey(measure.keySignature());
                if (measure.timeSignature() != null) context.setCurrentTime(measure.timeSignature());

                measureNumber++;
            }

            writer.writeEndElement(); // part
            writer.writeEndElement(); // score-partwise
            writer.writeEndDocument();
            writer.flush();

        } finally {
            try { writer.close(); } catch (XMLStreamException e) { /* ignore */ }
        }
    }

    private void writeIdentification(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("identification");
        writer.writeStartElement("encoding");
        writer.writeStartElement("software");
        writer.writeCharacters(MusicXmlConstants.SOFTWARE_NAME);
        writer.writeEndElement();
        writer.writeStartElement("encoding-date");
        writer.writeCharacters(dateSupplier.get().toString());
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
    }
}