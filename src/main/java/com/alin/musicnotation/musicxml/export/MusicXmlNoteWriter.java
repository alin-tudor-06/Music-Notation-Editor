package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.musicxml.common.MusicXmlDurationMapper;
import com.alin.musicnotation.musicxml.common.MusicXmlPitchMapper;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class MusicXmlNoteWriter {

    private final XMLStreamWriter writer;
    private final MusicXmlNotationWriter notationWriter;

    public MusicXmlNoteWriter(XMLStreamWriter writer) {
        this.writer = writer;
        this.notationWriter = new MusicXmlNotationWriter(writer);
    }

    public void writeNote(Note note) throws XMLStreamException {
        writer.writeStartElement("note");

        if (note.isRest()) {
            writer.writeEmptyElement("rest");
        } else {
            writePitch(note);
        }

        writeDuration(note);
        writeType(note);

        // Tie (legătură de durată) – elemente directe în <note>
        if (note.tieStart()) {
            writer.writeStartElement("tie");
            writer.writeAttribute("type", "start");
            writer.writeEndElement();
        }
        if (note.tieEnd()) {
            writer.writeStartElement("tie");
            writer.writeAttribute("type", "stop");
            writer.writeEndElement();
        }

        // Notations – un singur element care conține toate notațiile
        notationWriter.writeNotations(note);

        writer.writeEndElement(); // note
    }

    private void writePitch(Note note) throws XMLStreamException {
        var pitch = note.pitch();
        writer.writeStartElement("pitch");

        writer.writeStartElement("step");
        writer.writeCharacters(MusicXmlPitchMapper.getStep(pitch));
        writer.writeEndElement();

        Integer alter = MusicXmlPitchMapper.getAlter(pitch);
        if (alter != null) {
            writer.writeStartElement("alter");
            writer.writeCharacters(String.valueOf(alter));
            writer.writeEndElement();
        }

        writer.writeStartElement("octave");
        writer.writeCharacters(String.valueOf(MusicXmlPitchMapper.getOctave(pitch)));
        writer.writeEndElement();

        writer.writeEndElement(); // pitch
    }

    private void writeDuration(Note note) throws XMLStreamException {
        int duration = MusicXmlDurationMapper.getDurationInDivisions(note.duration());
        writer.writeStartElement("duration");
        writer.writeCharacters(String.valueOf(duration));
        writer.writeEndElement();
    }

    private void writeType(Note note) throws XMLStreamException {
        String type = MusicXmlDurationMapper.getTypeName(note.duration());
        writer.writeStartElement("type");
        writer.writeCharacters(type);
        writer.writeEndElement();
    }
}