package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.layout.NoteLayout;
import com.alin.musicnotation.model.layout.StemDirection;
import com.alin.musicnotation.smufl.BravuraFont;
import com.alin.musicnotation.smufl.SmuflGlyph;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

import java.util.Map;
import java.util.Set;

public class NoteRenderer {

    public void draw(GraphicsContext gc, Measure measure, Map<Note, NoteLayout> layoutMap,
                     double firstLineY, double middleLineY, int middleLinePosition,
                     double noteHeadWidth, double noteHeadHeight,
                     double stemLengthStandard, double ledgerLineExtension,
                     double noteHeadOutlineWidth, double flagWidth, double flagHeight,
                     double flagSpacing, double restWidth, double restHeight,
                     double eighthRestSlant, int topLinePosition, int bottomLinePosition,
                     double staffLineSpacing,
                     Set<Note> beamedNotes,
                     KeySignature activeKey) {

        // 1. Draw rests, note heads, ledger lines, and accidentals
        for (NoteLayout layout : layoutMap.values()) {
            Note note = layout.note();
            if (note.isRest()) {
                drawRest(gc, layout.x(), layout.y(), note.duration(),
                        restWidth, restHeight, eighthRestSlant, staffLineSpacing);
            } else {
                drawLedgerLines(gc, layout.x(), firstLineY, layout.position(),
                        topLinePosition, bottomLinePosition, staffLineSpacing,
                        noteHeadWidth, ledgerLineExtension);
                drawNoteHead(gc, layout.x(), layout.y(), note.duration(),
                        noteHeadWidth, noteHeadHeight, noteHeadOutlineWidth);
                if (shouldDisplayAccidental(note.pitch(), activeKey)) {
                    double symbolX = layout.x() - noteHeadWidth / 2 - 6;
                    drawAccidental(gc, symbolX, layout.y(), note.pitch().accidental(), staffLineSpacing);
                }
            }
        }

        // 2. Draw stems and flags for ALL notes that require a stem:
        //    - non-beamable notes (quarters, halves, etc.)
        //    - beamable notes that are NOT in a beam group (singletons)
        for (NoteLayout layout : layoutMap.values()) {
            Note note = layout.note();
            if (note.isRest()) continue;
            Duration duration = note.duration();
            if (!duration.requiresStem()) continue;

            // Determine if this note is beamed
            boolean isBeamed = beamedNotes.contains(note);

            // Skip stems for beamed notes (BeamRenderer handles them)
            if (isBeamed) continue;

            // For non-beamable notes OR beamable singletons:
            StemDirection dir = RenderingHelpers.stemDirection(layout.position(), middleLinePosition);
            double stemX = RenderingHelpers.stemAttachX(layout, dir, noteHeadWidth);
            double stemTipY = RenderingHelpers.computeStemTipY(layout, dir,
                    stemLengthStandard, middleLineY);
            drawStem(gc, stemX, layout.y(), stemTipY);

            // Draw flags if the note has flagCount > 0 (e.g., singleton eighth or sixteenth)
            if (duration.flagCount() > 0) {
                drawFlags(gc, stemX, stemTipY, duration.flagCount(), dir,
                        flagWidth, flagHeight, flagSpacing);
            }
        }
    }

    private void drawNoteHead(GraphicsContext gc, double x, double y, Duration duration,
                              double noteHeadWidth, double noteHeadHeight,
                              double noteHeadOutlineWidth) {

        // Select the correct glyph based on duration
        SmuflGlyph glyph = switch (duration) {
            case WHOLE -> SmuflGlyph.NOTEHEAD_WHOLE;
            case HALF -> SmuflGlyph.NOTEHEAD_HALF;
            default -> SmuflGlyph.NOTEHEAD_BLACK; // QUARTER, EIGHTH, SIXTEENTH
        };

        // Font size based on existing noteHeadHeight configuration
        // Factor 1.8 provides a size visually similar to the previous ellipses
        double fontSize = noteHeadHeight * 4;

        Font bravura = BravuraFont.getFont();
        Font glyphFont = Font.font(bravura.getFamily(), fontSize);

        gc.setFill(Color.BLACK);
        gc.setFont(glyphFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        // Draw glyph centered at (x, y)
        // No offsets are applied initially. If visual testing reveals misalignment,
        // add small constants (±1-2 px) after verification.
        gc.fillText(glyph.unicode(), x, y);
    }

    private void drawStem(GraphicsContext gc, double stemX, double stemBottomY, double stemTopY) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(stemX, stemBottomY, stemX, stemTopY);
    }

    private void drawFlag(GraphicsContext gc, double startX, double startY, StemDirection direction,
                          double flagWidth, double flagHeight) {
        int towardNoteheadSign = -direction.sign();
        double controlX = startX + flagWidth;
        double controlY = startY + towardNoteheadSign * flagHeight / 2;
        double endX = startX + flagWidth * 0.6;
        double endY = startY + towardNoteheadSign * flagHeight;
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.beginPath();
        gc.moveTo(startX, startY);
        gc.quadraticCurveTo(controlX, controlY, endX, endY);
        gc.stroke();
    }

    private void drawFlags(GraphicsContext gc, double stemX, double stemTipY, int flagCount,
                           StemDirection direction, double flagWidth, double flagHeight, double flagSpacing) {
        int towardNoteheadSign = -direction.sign();
        for (int i = 0; i < flagCount; i++) {
            double flagStartY = stemTipY + i * flagSpacing * towardNoteheadSign;
            drawFlag(gc, stemX, flagStartY, direction, flagWidth, flagHeight);
        }
    }

    private void drawRest(GraphicsContext gc, double x, double y, Duration duration,
                          double restWidth, double restHeight, double eighthRestSlant,
                          double staffLineSpacing) {
        // Map Duration to SmuflGlyph
        SmuflGlyph glyph = switch (duration) {
            case WHOLE -> SmuflGlyph.REST_WHOLE;
            case HALF -> SmuflGlyph.REST_HALF;
            case QUARTER -> SmuflGlyph.REST_QUARTER;
            case EIGHTH -> SmuflGlyph.REST_EIGHTH;
            case SIXTEENTH -> SmuflGlyph.REST_SIXTEENTH;
            default -> null;
        };

        if (glyph == null) {
            return;
        }

        // Font size derived from restHeight to preserve visual proportion.
        // The multiplication factor (2.0) compensates for the typical difference
        // between the bounding box of the old geometric drawing and the SMuFL glyph's em‑square.
        double fontSize = restHeight * 4;
        Font bravura = BravuraFont.getFont();
        Font glyphFont = Font.font(bravura.getFamily(), fontSize);

        gc.setFill(Color.BLACK);
        gc.setFont(glyphFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(glyph.unicode(), x, y);
    }

    private void drawLedgerLines(GraphicsContext gc, double x, double firstLineY, int position,
                                 int topLinePosition, int bottomLinePosition, double staffLineSpacing,
                                 double noteHeadWidth, double ledgerLineExtension) {
        if (position < topLinePosition) {
            int count = -position / 2;
            for (int i = 1; i <= count; i++) {
                int ledgerPosition = -2 * i;
                double y = firstLineY + ledgerPosition * (staffLineSpacing / 2);
                drawLedgerLine(gc, x, y, noteHeadWidth, ledgerLineExtension);
            }
        } else if (position > bottomLinePosition) {
            int count = (position - bottomLinePosition) / 2;
            for (int i = 1; i <= count; i++) {
                int ledgerPosition = bottomLinePosition + 2 * i;
                double y = firstLineY + ledgerPosition * (staffLineSpacing / 2);
                drawLedgerLine(gc, x, y, noteHeadWidth, ledgerLineExtension);
            }
        }
    }

    private void drawLedgerLine(GraphicsContext gc, double x, double y,
                                double noteHeadWidth, double ledgerLineExtension) {
        double halfWidth = noteHeadWidth / 2 + ledgerLineExtension;
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(x - halfWidth, y, x + halfWidth, y);
    }

    /**
     * Draws an accidental using the Bravura SMuFL font.
     * The mapping from Accidental to SmuflGlyph is done directly in this renderer.
     * This keeps the model (Accidental) independent of rendering concerns.
     */
    private void drawAccidental(GraphicsContext gc, double x, double y,
                                Accidental accidental, double staffLineSpacing) {
        if (accidental == Accidental.NONE) {
            return;
        }

        // Map Accidental to SmuflGlyph
        SmuflGlyph glyph = switch (accidental) {
            case SHARP -> SmuflGlyph.SHARP;
            case FLAT -> SmuflGlyph.FLAT;
            case NATURAL -> SmuflGlyph.NATURAL;
            default -> null;
        };

        if (glyph == null) {
            return;
        }

        // Create properly sized Bravura font
        double fontSize = staffLineSpacing * 2; // same factor as before
        Font bravura = BravuraFont.getFont();
        Font glyphFont = Font.font(bravura.getFamily(), fontSize);

        gc.setFill(Color.BLACK);
        gc.setFont(glyphFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(glyph.unicode(), x, y);
    }

    private boolean shouldDisplayAccidental(Pitch pitch, KeySignature activeKey) {
        Accidental pitchAcc = pitch.accidental();
        if (pitchAcc == Accidental.NONE) return false;
        if (pitchAcc == Accidental.NATURAL) {
            return activeKey != null && activeKey.altersStep(pitch.step());
        }
        return activeKey == null || activeKey.accidentalForStep(pitch.step()) != pitchAcc;
    }
}