package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.clef.ClefType;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MusicXmlExportTest {

    public static void main(String[] args) throws Exception {
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
        m2.addNote(new Note(Pitch.of("B4"), Duration.QUARTER));
        m2.addNote(new Note(Pitch.of("A4"), Duration.QUARTER));
        m2.addNote(new Note(Pitch.of("G4"), Duration.QUARTER,false,true));
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
        Path outputPath = Paths.get(System.getProperty("user.home"), "Desktop", "test.musicxml");
        exporter.export(score, outputPath);

        System.out.println("✅ Fișier generat: " + outputPath);
    }
}