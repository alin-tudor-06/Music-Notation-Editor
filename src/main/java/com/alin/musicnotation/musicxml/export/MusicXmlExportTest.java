package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.articulation.Articulation;
import com.alin.musicnotation.model.articulation.ArticulationType;
import com.alin.musicnotation.model.clef.ClefType;
import com.alin.musicnotation.model.dynamic.Dynamic;
import com.alin.musicnotation.model.dynamic.DynamicType;
import com.alin.musicnotation.model.hairpin.Hairpin;
import com.alin.musicnotation.model.hairpin.HairpinPlacement;
import com.alin.musicnotation.model.hairpin.HairpinType;
import com.alin.musicnotation.model.tempo.Tempo;
import com.alin.musicnotation.model.tempo.TempoPlacement;
import com.alin.musicnotation.model.text.TextExpression;
import com.alin.musicnotation.model.text.TextPlacement;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MusicXmlExportTest {

    public static void main(String[] args) throws Exception {
        Score score = new Score(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);

        // ============================================================
        // Măsura 1: 4/4 - Do Re Mi Fa + slur + articulații + dinamici
        // ============================================================
        Measure m1 = new Measure();
        m1.addTempo(new Tempo("Allegro", 120));
        m1.addTextExpression(new TextExpression("dolce", TextPlacement.ABOVE));
        Note m1n1 = new Note(Pitch.of("C4"), Duration.QUARTER);
        Note m1n2 = new Note(Pitch.of("D4"), Duration.QUARTER);
        Note m1n3 = new Note(Pitch.of("E4"), Duration.QUARTER);
        Note m1n4 = new Note(Pitch.of("F4"), Duration.QUARTER);
        m1.addNote(m1n1);
        m1.addNote(m1n2);
        m1.addNote(m1n3);
        m1.addNote(m1n4);
        m1.addSlur(new Slur(List.of(m1n1, m1n2, m1n3, m1n4)));
        m1.addArticulation(new Articulation(m1n1, ArticulationType.STACCATO));
        m1.addArticulation(new Articulation(m1n2, ArticulationType.ACCENT));
        m1.addArticulation(new Articulation(m1n3, ArticulationType.TENUTO));
        m1.addArticulation(new Articulation(m1n4, ArticulationType.MARCATO));
        m1.addDynamic(new Dynamic(m1n1, DynamicType.P));
        m1.addDynamic(new Dynamic(m1n2, DynamicType.MF));
        m1.addDynamic(new Dynamic(m1n3, DynamicType.F));
        m1.addDynamic(new Dynamic(m1n4, DynamicType.FF));
        score.addMeasure(m1);

        // ============================================================
        // Măsura 2: 4/4 - tie
        // ============================================================
        Measure m2 = new Measure();
        m2.addTempo(new Tempo("Andante", 80));
        m2.addNote(new Note(Pitch.of("G4"), Duration.QUARTER, true, false));
        m2.addNote(new Note(Pitch.of("G4"), Duration.QUARTER, false, true));
        m2.addNote(new Note(Pitch.of("A4"), Duration.QUARTER));
        m2.addNote(new Note(Pitch.of("B4"), Duration.QUARTER));
        score.addMeasure(m2);

        // ============================================================
        // Măsura 3: 3/4 - G major
        // ============================================================
        Measure m3 = new Measure();
        m3.addTempo(Tempo.ofBpm(96));
        m3.addTextExpression(new TextExpression("cantabile", TextPlacement.BELOW));
        m3.addKeySignature(KeySignature.G_MAJOR);
        m3.addTimeSignature(TimeSignature.THREE_FOUR);
        m3.addNote(new Note(Pitch.of("C5"), Duration.QUARTER));
        m3.addNote(new Note(Pitch.of("D5"), Duration.QUARTER));
        m3.addNote(new Note(Pitch.of("E5"), Duration.QUARTER));
        score.addMeasure(m3);

        // ============================================================
        // Măsura 4: 4/4 - Bass clef + revenire la 4/4
        // ============================================================
        Measure m4 = new Measure();
        m4.addTempo(new Tempo("Presto", 168, TempoPlacement.BELOW));
        m4.addClef(ClefType.BASS);
        m4.addTimeSignature(TimeSignature.COMMON_TIME);
        m4.addNote(new Note(Pitch.of("C3"), Duration.QUARTER));
        m4.addNote(new Note(Pitch.of("D3"), Duration.QUARTER));
        m4.addNote(new Note(Pitch.of("E3"), Duration.QUARTER));
        m4.addNote(new Note(Pitch.of("F3"), Duration.QUARTER));
        score.addMeasure(m4);

        // ============================================================
        // Măsura 5: 4/4 - resturi
        // ============================================================
        Measure m5 = new Measure();
        m5.addNote(new Note(Duration.HALF));
        m5.addNote(new Note(Duration.QUARTER));
        m5.addNote(new Note(Duration.QUARTER));
        score.addMeasure(m5);

        // ============================================================
        // Măsura 6: 4/4 - accidentale
        // ============================================================
        Measure m6 = new Measure();
        m6.addNote(new Note(Pitch.of("C#4"), Duration.QUARTER));
        m6.addNote(new Note(Pitch.of("Db4"), Duration.QUARTER));
        m6.addNote(new Note(Pitch.of("Dn4"), Duration.QUARTER));
        m6.addNote(new Note(Pitch.of("E4"), Duration.QUARTER));
        score.addMeasure(m6);

        // ============================================================
        // Măsura 7: 4/4 - notă întreagă
        // ============================================================
        Measure m7 = new Measure();
        m7.addNote(new Note(Pitch.of("C4"), Duration.WHOLE));
        score.addMeasure(m7);

        // ============================================================
        // Măsura 8: 4/4 - optimi + pătrimi
        // ============================================================
        Measure m8 = new Measure();
        m8.addNote(new Note(Pitch.of("C4"), Duration.EIGHTH));
        m8.addNote(new Note(Pitch.of("D4"), Duration.EIGHTH));
        m8.addNote(new Note(Pitch.of("E4"), Duration.EIGHTH));
        m8.addNote(new Note(Pitch.of("F4"), Duration.EIGHTH));
        m8.addNote(new Note(Pitch.of("G4"), Duration.QUARTER));
        m8.addNote(new Note(Pitch.of("A4"), Duration.QUARTER));
        score.addMeasure(m8);

        // Măsura 9: Start repeat
        Measure m9 = new Measure();
        m9.addStartRepeat();
        m9.addNote(new Note(Pitch.of("C4"), Duration.QUARTER));
        m9.addNote(new Note(Pitch.of("D4"), Duration.QUARTER));
        m9.addNote(new Note(Pitch.of("E4"), Duration.QUARTER));
        m9.addNote(new Note(Pitch.of("F4"), Duration.QUARTER));
        score.addMeasure(m9);

        // Măsura 10: End repeat
        Measure m10 = new Measure();
        m10.addNote(new Note(Pitch.of("G4"), Duration.QUARTER));
        m10.addNote(new Note(Pitch.of("A4"), Duration.QUARTER));
        m10.addNote(new Note(Pitch.of("B4"), Duration.QUARTER));
        m10.addNote(new Note(Pitch.of("C5"), Duration.QUARTER));
        m10.addEndRepeat();
        score.addMeasure(m10);

        // Măsura 11: Crescendo + Decrescendo
        Measure m11 = new Measure();
        Note hn1 = new Note(Pitch.of("C4"), Duration.QUARTER);
        Note hn2 = new Note(Pitch.of("D4"), Duration.QUARTER);
        Note hn3 = new Note(Pitch.of("E4"), Duration.QUARTER);
        Note hn4 = new Note(Pitch.of("F4"), Duration.QUARTER);
        m11.addNote(hn1);
        m11.addNote(hn2);
        m11.addNote(hn3);
        m11.addNote(hn4);
        m11.addHairpin(new Hairpin(hn1, hn4, HairpinType.CRESCENDO, HairpinPlacement.BELOW));
        score.addMeasure(m11);

        // Măsura 12: Decrescendo
        Measure m12 = new Measure();
        Note dn1 = new Note(Pitch.of("G4"), Duration.QUARTER);
        Note dn2 = new Note(Pitch.of("F4"), Duration.QUARTER);
        Note dn3 = new Note(Pitch.of("E4"), Duration.QUARTER);
        Note dn4 = new Note(Pitch.of("D4"), Duration.QUARTER);
        m12.addNote(dn1);
        m12.addNote(dn2);
        m12.addNote(dn3);
        m12.addNote(dn4);
        m12.addHairpin(new Hairpin(dn1, dn4, HairpinType.DECRESCENDO, HairpinPlacement.ABOVE));
        score.addMeasure(m12);

        score.validate();

        MusicXmlExporter exporter = new MusicXmlExporter();
        Path outputPath = Paths.get(System.getProperty("user.home"), "Desktop", "test.musicxml");
        exporter.export(score, outputPath);

        System.out.println("✅ Fișier generat: " + outputPath);
        System.out.println("📂 Deschide-l în MuseScore pentru verificare.");
    }
}