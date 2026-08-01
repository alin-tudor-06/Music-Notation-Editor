package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.clef.ClefType;
import com.alin.musicnotation.smufl.BravuraFont;
import com.alin.musicnotation.smufl.SmuflGlyph;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

/**
 * Renders clefs using the Bravura SMuFL font.
 * <p>
 * This renderer replaces the previous placeholder Unicode symbols with proper
 * SMuFL glyphs from the Bravura font. Positioning and sizing are preserved
 * relative to the staff.
 * </p>
 */
public class ClefRenderer {

    // Same factor as before – produces a glyph height roughly proportional to staff lines
    private static final double CLEF_FONT_SIZE_FACTOR = 3;

    public void drawClef(GraphicsContext gc, double x, double firstLineY,
                         double staffHeight, double staffLineSpacing, ClefType clefType) {
        if (clefType == null) {
            return;
        }

        // Map ClefType to SmuflGlyph
        SmuflGlyph glyph = switch (clefType) {
            case TREBLE -> SmuflGlyph.TREBLE_CLEF;
            case BASS -> SmuflGlyph.BASS_CLEF;
            case ALTO -> SmuflGlyph.ALTO_CLEF;
            case TENOR -> SmuflGlyph.TENOR_CLEF;
        };

        // Determine vertical reference point (same logic as before)
        double refY = firstLineY + clefType.referenceLineIndex() * staffLineSpacing;

        // Create a properly sized font from Bravura
        Font bravura = BravuraFont.getFont();
        double fontSize = staffLineSpacing * CLEF_FONT_SIZE_FACTOR;
        Font glyphFont = Font.font(bravura.getFamily(), fontSize);

        // Draw the glyph
        gc.setFill(Color.BLACK);
        gc.setFont(glyphFont);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(glyph.unicode(), x, refY);
    }
}