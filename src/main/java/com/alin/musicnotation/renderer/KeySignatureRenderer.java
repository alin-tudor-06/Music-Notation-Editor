package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.clef.ClefType;
import com.alin.musicnotation.smufl.BravuraFont;
import com.alin.musicnotation.smufl.SmuflGlyph;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

import java.util.*;

public class KeySignatureRenderer {

    private static final double KEY_ACCIDENTAL_SPACING = 8;
    private static final double ACCIDENTAL_FONT_SIZE_FACTOR = 2; // puțin mai mare pentru armură

    // --- Pitch-uri canonice pentru semnăturile de cheie ---
    private static final Pitch[] TREBLE_SHARPS = {
            Pitch.of("F#5"),
            Pitch.of("C#5"),
            Pitch.of("G#5"),
            Pitch.of("D#5"),
            Pitch.of("A#4"),
            Pitch.of("E#5"),
            Pitch.of("B#4")
    };

    private static final Pitch[] BASS_SHARPS = {
            Pitch.of("F#3"),
            Pitch.of("C#3"),
            Pitch.of("G#3"),
            Pitch.of("D#3"),
            Pitch.of("A#2"),
            Pitch.of("E#3"),
            Pitch.of("B#2")
    };

    private static final Pitch[] TREBLE_FLATS = {
            Pitch.of("Bb4"),
            Pitch.of("Eb5"),
            Pitch.of("Ab4"),
            Pitch.of("Db5"),
            Pitch.of("Gb4"),
            Pitch.of("Cb5"),
            Pitch.of("Fb4")
    };

    private static final Pitch[] BASS_FLATS = {
            Pitch.of("Bb2"),
            Pitch.of("Eb3"),
            Pitch.of("Ab2"),
            Pitch.of("Db3"),
            Pitch.of("Gb2"),
            Pitch.of("Cb3"),
            Pitch.of("Fb2")
    };

    public double drawInitial(GraphicsContext gc, double x, double firstLineY,
                              KeySignature ks, ClefType clef, double staffLineSpacing) {
        if (ks == null || ks.alteredPitches().isEmpty()) return 0;
        return drawKeySignature(gc, x, firstLineY, ks, clef, staffLineSpacing);
    }

    public double drawChange(GraphicsContext gc, double x, double firstLineY,
                             KeySignature previousKey, KeySignature newKey,
                             ClefType clef, double staffLineSpacing) {
        if (newKey == null) return 0;

        double totalWidth = 0;

        if (previousKey != null && !previousKey.equals(newKey)) {
            List<Pitch> cancellingPitches = computeCancellingPitches(previousKey, newKey);
            if (!cancellingPitches.isEmpty()) {
                totalWidth += drawAccidentals(gc, x + totalWidth, firstLineY,
                        cancellingPitches, clef, staffLineSpacing);
                totalWidth += 4;
            }
        }

        if (!newKey.alteredPitches().isEmpty()) {
            totalWidth += drawKeySignature(gc, x + totalWidth, firstLineY,
                    newKey, clef, staffLineSpacing);
        }

        return totalWidth;
    }

    private List<Pitch> computeCancellingPitches(KeySignature from, KeySignature to) {
        if (from == null || to == null) return Collections.emptyList();

        Set<Pitch.Step> fromSteps = new HashSet<>(getAlteredSteps(from));
        Set<Pitch.Step> toSteps = new HashSet<>(getAlteredSteps(to));
        fromSteps.removeAll(toSteps);

        if (fromSteps.isEmpty()) return Collections.emptyList();

        List<Pitch> result = new ArrayList<>();
        for (Pitch pitch : from.alteredPitches()) {
            if (fromSteps.contains(pitch.step())) {
                result.add(new Pitch(pitch.step(), Accidental.NATURAL, pitch.octave()));
            }
        }
        return result;
    }

    private Set<Pitch.Step> getAlteredSteps(KeySignature ks) {
        Set<Pitch.Step> steps = new HashSet<>();
        for (Pitch pitch : ks.alteredPitches()) {
            steps.add(pitch.step());
        }
        return steps;
    }

    private double drawKeySignature(GraphicsContext gc, double x, double firstLineY,
                                    KeySignature ks, ClefType clef, double staffLineSpacing) {
        if (ks == null || ks.alteredPitches().isEmpty()) return 0;

        double currentX = x;
        List<Pitch> orderedPitches = getAlteredPitchesInOrder(ks, clef);

        for (Pitch pitch : orderedPitches) {
            int position = staffPosition(pitch, clef);
            double y = firstLineY + position * (staffLineSpacing / 2);

            // Folosim Accidental din pitch pentru a desena glyph-ul corect
            Accidental accidental = pitch.accidental();
            drawAccidental(gc, currentX, y, accidental, staffLineSpacing);

            currentX += KEY_ACCIDENTAL_SPACING + staffLineSpacing * 0.7;
        }
        return currentX - x;
    }

    private double drawAccidentals(GraphicsContext gc, double x, double firstLineY,
                                   List<Pitch> pitches, ClefType clef, double staffLineSpacing) {
        if (pitches == null || pitches.isEmpty()) return 0;

        double currentX = x;
        for (Pitch pitch : pitches) {
            int position = staffPosition(pitch, clef);
            double y = firstLineY + position * (staffLineSpacing / 2);
            drawAccidental(gc, currentX, y, pitch.accidental(), staffLineSpacing);
            currentX += KEY_ACCIDENTAL_SPACING + staffLineSpacing * 0.7;
        }
        return currentX - x;
    }

    /**
     * Desenează un accidental (diez, bemol sau becar) folosind fontul Bravura.
     */
    private void drawAccidental(GraphicsContext gc, double x, double y,
                                Accidental accidental, double staffLineSpacing) {
        if (accidental == Accidental.NONE) return;

        // Mapare Accidental -> SmuflGlyph
        SmuflGlyph glyph = switch (accidental) {
            case SHARP -> SmuflGlyph.SHARP;
            case FLAT -> SmuflGlyph.FLAT;
            case NATURAL -> SmuflGlyph.NATURAL;
            default -> null;
        };

        if (glyph == null) return;

        // Font Bravura la dimensiunea potrivită
        double fontSize = staffLineSpacing * ACCIDENTAL_FONT_SIZE_FACTOR;
        Font bravura = BravuraFont.getFont();
        Font glyphFont = Font.font(bravura.getFamily(), fontSize);

        gc.setFill(Color.BLACK);
        gc.setFont(glyphFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(glyph.unicode(), x, y);
    }

    private List<Pitch> getAlteredPitchesInOrder(KeySignature ks, ClefType clef) {
        if (ks == null || ks.alteredPitches().isEmpty()) return Collections.emptyList();

        int sharps = ks.sharps();
        List<Pitch> result = new ArrayList<>();

        if (sharps > 0) {
            Pitch[] sharpPitches = clef == ClefType.BASS ? BASS_SHARPS : TREBLE_SHARPS;
            for (int i = 0; i < sharps; i++) {
                result.add(sharpPitches[i]);
            }
        } else if (sharps < 0) {
            int flats = -sharps;
            Pitch[] flatPitches = clef == ClefType.BASS ? BASS_FLATS : TREBLE_FLATS;
            for (int i = 0; i < flats; i++) {
                result.add(flatPitches[i]);
            }
        }
        return result;
    }

    public double calculateChangeWidth(KeySignature previousKey, KeySignature newKey,
                                       double staffLineSpacing) {
        if (newKey == null) return 0;

        double totalWidth = 0;

        if (previousKey != null && !previousKey.equals(newKey)) {
            List<Pitch> cancellingPitches = computeCancellingPitches(previousKey, newKey);
            if (!cancellingPitches.isEmpty()) {
                totalWidth += cancellingPitches.size() *
                        (KEY_ACCIDENTAL_SPACING + staffLineSpacing * 0.7);
                totalWidth += 4;
            }
        }

        if (!newKey.alteredPitches().isEmpty()) {
            totalWidth += newKey.alteredPitches().size() *
                    (KEY_ACCIDENTAL_SPACING + staffLineSpacing * 0.7);
        }

        return totalWidth;
    }

    private int staffPosition(Pitch pitch, ClefType clef) {
        return diatonicIndex(clef.referencePitch()) - diatonicIndex(pitch);
    }

    private static int diatonicIndex(Pitch pitch) {
        return pitch.octave() * 7 + pitch.step().ordinal();
    }
}