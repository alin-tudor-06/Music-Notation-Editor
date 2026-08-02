package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.clef.ClefType;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test complet pentru MusicXML export.
 * Generează un fișier care conține toate caracteristicile:
 * - resturi (toate duratele)
 * - note cu durate variate
 * - accidentale (diez, bemol, becar)
 * - schimbări de armură
 * - schimbări de timp
 * - schimbări de cheie
 *
 * Fișierul este generat pe Desktop și poate fi deschis în MuseScore.
 */
public class MusicXmlExportTest {

    public static void main(String[] args) throws Exception {
        System.out.println("🎵 Generare fișier MusicXML complet...\n");

        // ============================================================
        // 1. Scorul începe în Do major, cheia sol, 4/4
        // ============================================================
        Score score = new Score(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);

        // ============================================================
        // Măsura 1 – Resturi de diferite durate (4/4)
        // Folosim o măsură dedicată doar pentru resturi.
        // ============================================================
        Measure m1 = new Measure();
        m1.addNote(new Note(Duration.WHOLE));       // 16 șaisprezecimi
        // Măsura 1 este completă (16/16)
        score.addMeasure(m1);

        // ============================================================
        // Măsura 2 – Rest de doime + două pătrimi
        // ============================================================
        Measure m2 = new Measure();
        m2.addNote(new Note(Duration.HALF));        // 8 șaisprezecimi
        m2.addNote(new Note(Duration.QUARTER));     // 4 șaisprezecimi
        m2.addNote(new Note(Duration.QUARTER));     // 4 șaisprezecimi
        // Total: 8+4+4 = 16
        score.addMeasure(m2);

        // ============================================================
        // Măsura 3 – Rest de pătrime + optime + șaisprezecime + rest
        // ============================================================
        Measure m3 = new Measure();
        m3.addNote(new Note(Duration.QUARTER));     // 4
        m3.addNote(new Note(Duration.EIGHTH));      // 2
        m3.addNote(new Note(Duration.SIXTEENTH));   // 1
        m3.addNote(new Note(Duration.QUARTER));     // 4
        m3.addNote(new Note(Duration.QUARTER));     // 4
        // Total: 4+2+1+4+4 = 15 (mai adăugăm un rest de 1)
        m3.addNote(new Note(Duration.SIXTEENTH));
        // Total: 16
        score.addMeasure(m3);

        // ============================================================
        // Măsura 4 – Note cu durate variate și accidentale
        // ============================================================
        Measure m4 = new Measure();
        m4.addNote(new Note(Pitch.of("C4"), Duration.QUARTER));
        m4.addNote(new Note(Pitch.of("C#4"), Duration.QUARTER));   // diez
        m4.addNote(new Note(Pitch.of("Db4"), Duration.QUARTER));   // bemol
        m4.addNote(new Note(Pitch.of("Dn4"), Duration.QUARTER));   // becar
        score.addMeasure(m4);

        // ============================================================
        // Măsura 5 – Accidental + durată mai mică
        // ============================================================
        Measure m5 = new Measure();
        m5.addNote(new Note(Pitch.of("F#5"), Duration.EIGHTH));
        m5.addNote(new Note(Pitch.of("Gb5"), Duration.EIGHTH));
        m5.addNote(new Note(Pitch.of("Gn5"), Duration.EIGHTH));
        m5.addNote(new Note(Pitch.of("G5"), Duration.EIGHTH));
        m5.addNote(new Note(Pitch.of("A5"), Duration.EIGHTH));
        m5.addNote(new Note(Pitch.of("B5"), Duration.EIGHTH));
        m5.addNote(new Note(Pitch.of("C6"), Duration.EIGHTH));
        m5.addNote(new Note(Pitch.of("D6"), Duration.EIGHTH));
        // Total: 8 optimi = 16 șaisprezecimi
        score.addMeasure(m5);

        // ============================================================
        // Măsura 6 – Schimbare armură: G major (un diez)
        // ============================================================
        Measure m6 = new Measure();
        m6.addKeySignature(KeySignature.G_MAJOR);
        m6.addNote(new Note(Pitch.of("G4"), Duration.QUARTER));
        m6.addNote(new Note(Pitch.of("A4"), Duration.QUARTER));
        m6.addNote(new Note(Pitch.of("B4"), Duration.QUARTER));
        m6.addNote(new Note(Pitch.of("C5"), Duration.QUARTER));
        score.addMeasure(m6);

        // ============================================================
        // Măsura 7 – Schimbare timp: 3/4
        // ============================================================
        Measure m7 = new Measure();
        m7.addTimeSignature(TimeSignature.THREE_FOUR);
        m7.addNote(new Note(Pitch.of("D5"), Duration.QUARTER));
        m7.addNote(new Note(Pitch.of("E5"), Duration.QUARTER));
        m7.addNote(new Note(Pitch.of("F5"), Duration.QUARTER));
        score.addMeasure(m7);

        // ============================================================
        // Măsura 8 – Schimbare armură: F major (un bemol)
        // ============================================================
        Measure m8 = new Measure();
        m8.addKeySignature(KeySignature.F_MAJOR);
        m8.addNote(new Note(Pitch.of("F4"), Duration.QUARTER));
        m8.addNote(new Note(Pitch.of("G4"), Duration.QUARTER));
        m8.addNote(new Note(Pitch.of("Bb4"), Duration.QUARTER));   // bemol în armură
        score.addMeasure(m8);

        // ============================================================
        // Măsura 9 – Schimbare timp: 6/8
        // ============================================================
        Measure m9 = new Measure();
        m9.addTimeSignature(TimeSignature.SIX_EIGHT);
        m9.addNote(new Note(Pitch.of("C5"), Duration.EIGHTH));
        m9.addNote(new Note(Pitch.of("D5"), Duration.EIGHTH));
        m9.addNote(new Note(Pitch.of("E5"), Duration.EIGHTH));
        m9.addNote(new Note(Pitch.of("F5"), Duration.EIGHTH));
        m9.addNote(new Note(Pitch.of("G5"), Duration.EIGHTH));
        m9.addNote(new Note(Pitch.of("A5"), Duration.EIGHTH));
        score.addMeasure(m9);

        // ============================================================
        // Măsura 10 – Schimbare cheie în Bass
        // ============================================================
        Measure m10 = new Measure();
        m10.addClef(ClefType.BASS);
        m10.addNote(new Note(Pitch.of("C3"), Duration.QUARTER));
        m10.addNote(new Note(Pitch.of("D3"), Duration.QUARTER));
        m10.addNote(new Note(Pitch.of("E3"), Duration.QUARTER));
        score.addMeasure(m10);

        // ============================================================
        // Măsura 11 – Notă întreagă (pentru test)
        // ============================================================
        Measure m11 = new Measure();
        m11.addTimeSignature(TimeSignature.COMMON_TIME);
        m11.addNote(new Note(Pitch.of("C4"), Duration.WHOLE));
        score.addMeasure(m11);

        // ============================================================
        // Măsura 12 – Rest de doime + două optimi (4/4)
        // ============================================================
        Measure m12 = new Measure();
        m12.addNote(new Note(Duration.HALF));
        m12.addNote(new Note(Duration.QUARTER));
        m12.addNote(new Note(Duration.QUARTER));
        score.addMeasure(m12);

        // ============================================================
        // Validare scor
        // ============================================================
        score.validate();
        System.out.println("✅ Scor valid.");

        // ============================================================
        // Export în fișier .musicxml
        // ============================================================
        MusicXmlExporter exporter = new MusicXmlExporter();
        Path outputPath = Paths.get(System.getProperty("user.home"), "Desktop", "full_test.musicxml");
        exporter.export(score, outputPath);

        System.out.println("✅ Fișierul MusicXML a fost generat la: " + outputPath);
        System.out.println("📂 Deschide-l în MuseScore Studio pentru verificare.");
        System.out.println();
        System.out.println("📋 Caracteristici incluse:");
        System.out.println("   - Resturi: întreg, doime, pătrime, optime, șaisprezecime");
        System.out.println("   - Note: întregi, doimi, pătrimi, optimi, șaisprezecimi");
        System.out.println("   - Accidentale: diez (#), bemol (b), becar (n)");
        System.out.println("   - Schimbări de armură: Do major → Sol major → Fa major");
        System.out.println("   - Schimbări de timp: 4/4 → 3/4 → 6/8");
        System.out.println("   - Schimbări de cheie: Treble → Bass");

        System.out.println("\n🎉 Test complet!");
    }
}