package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.clef.ClefType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.*;

public class KeySignatureRenderer {

    private static final double KEY_ACCIDENTAL_SPACING = 8;
    private static final double ACCIDENTAL_FONT_SIZE_FACTOR = 1.2;

    private static final Pitch.Step[] SHARP_ORDER = {
            Pitch.Step.F, Pitch.Step.C, Pitch.Step.G, Pitch.Step.D, Pitch.Step.A, Pitch.Step.E, Pitch.Step.B
    };
    private static final Pitch.Step[] FLAT_ORDER = {
            Pitch.Step.B, Pitch.Step.E, Pitch.Step.A, Pitch.Step.D, Pitch.Step.G, Pitch.Step.C, Pitch.Step.F
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
        List<Pitch> orderedPitches = getAlteredPitchesInOrder(ks);
        for (Pitch pitch : orderedPitches) {
            int position = staffPosition(pitch, clef);
            double y = firstLineY + position * (staffLineSpacing / 2);
            drawAccidental(gc, currentX, y, pitch.accidental(), staffLineSpacing);
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

    private void drawAccidental(GraphicsContext gc, double x, double y,
                                Accidental accidental, double staffLineSpacing) {
        if (accidental == Accidental.NONE) return;
        String symbol = accidental.unicodeSymbol();
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Serif", staffLineSpacing * ACCIDENTAL_FONT_SIZE_FACTOR));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.fillText(symbol, x, y);
    }

    private List<Pitch> getAlteredPitchesInOrder(KeySignature ks) {
        if (ks == null || ks.alteredPitches().isEmpty()) return Collections.emptyList();

        int sharps = ks.sharps();
        List<Pitch> result = new ArrayList<>();
        if (sharps > 0) {
            for (int i = 0; i < sharps; i++) {
                Pitch.Step step = SHARP_ORDER[i];
                for (Pitch p : ks.alteredPitches()) {
                    if (p.step() == step) {
                        result.add(p);
                        break;
                    }
                }
            }
        } else if (sharps < 0) {
            int flats = -sharps;
            for (int i = 0; i < flats; i++) {
                Pitch.Step step = FLAT_ORDER[i];
                for (Pitch p : ks.alteredPitches()) {
                    if (p.step() == step) {
                        result.add(p);
                        break;
                    }
                }
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