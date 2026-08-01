package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.TimeSignature;
import com.alin.musicnotation.smufl.BravuraFont;
import com.alin.musicnotation.smufl.SmuflGlyph;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders time signatures using Bravura SMuFL glyphs.
 * <p>
 * The renderer preserves the exact positioning logic from the original
 * implementation (staffHeight * 0.25 for numerator, * 0.75 for denominator).
 * Only the drawing primitive has been replaced.
 * </p>
 */
public class TimeSignatureRenderer {

    private static final double TIME_SIGNATURE_FONT_SIZE_FACTOR = 2.2;
    private static final double DIGIT_SPACING_FACTOR = 0.08;
    private static final double SIDE_PADDING = 4;

    /**
     * Immutable measurement of a single glyph.
     */
    private record GlyphMeasurement(SmuflGlyph glyph, double width) {}

    public double drawInitial(GraphicsContext gc, double x, double firstLineY,
                              double staffHeight, double staffLineSpacing,
                              TimeSignature ts) {
        if (ts == null) return 0;
        return drawTimeSignature(gc, x, firstLineY, staffHeight, staffLineSpacing, ts);
    }

    public double drawChange(GraphicsContext gc, double x, double firstLineY,
                             double staffHeight, double staffLineSpacing,
                             TimeSignature ts) {
        if (ts == null) return 0;
        return drawTimeSignature(gc, x, firstLineY, staffHeight, staffLineSpacing, ts);
    }

    private double drawTimeSignature(GraphicsContext gc, double x, double firstLineY,
                                     double staffHeight, double staffLineSpacing,
                                     TimeSignature ts) {
        if (ts == null) return 0;

        double fontSize = staffLineSpacing * TIME_SIGNATURE_FONT_SIZE_FACTOR;
        Font bravura = BravuraFont.getFont();
        Font glyphFont = Font.font(bravura.getFamily(), fontSize);

        gc.setFill(Color.BLACK);
        gc.setFont(glyphFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        // Păstrăm exact aceleași coordonate ca în implementarea originală
        double topY = firstLineY + staffHeight * 0.25;
        double bottomY = firstLineY + staffHeight * 0.75;
        double midY = firstLineY + staffHeight / 2;

        switch (ts.displayStyle()) {
            case COMMON -> {
                gc.fillText(SmuflGlyph.TIME_SIG_COMMON.unicode(), x, midY);
                return measureGlyphWidth(glyphFont, SmuflGlyph.TIME_SIG_COMMON.unicode()) + SIDE_PADDING;
            }
            case CUT -> {
                gc.fillText(SmuflGlyph.TIME_SIG_CUT.unicode(), x, midY);
                return measureGlyphWidth(glyphFont, SmuflGlyph.TIME_SIG_CUT.unicode()) + SIDE_PADDING;
            }
            default -> {
                String numerator = String.valueOf(ts.numerator());
                String denominator = String.valueOf(ts.denominator());

                // Măsurăm ambele numere o singură dată
                List<GlyphMeasurement> numMeasurements = measureNumber(glyphFont, numerator);
                List<GlyphMeasurement> denMeasurements = measureNumber(glyphFont, denominator);

                // Desenăm numerele centrate față de lățimea maximă
                double numWidth = sumWidths(numMeasurements);
                double denWidth = sumWidths(denMeasurements);
                double maxWidth = Math.max(numWidth, denWidth);

                drawNumber(gc, numMeasurements, x, topY, maxWidth);
                drawNumber(gc, denMeasurements, x, bottomY, maxWidth);

                return maxWidth + SIDE_PADDING;
            }
        }
    }

    /**
     * Measures a number composed of SMuFL digit glyphs.
     * Spacing is applied between digits, not after the last one.
     */
    private List<GlyphMeasurement> measureNumber(Font font, String number) {
        List<GlyphMeasurement> measurements = new ArrayList<>();
        double digitSpacing = font.getSize() * DIGIT_SPACING_FACTOR;
        int length = number.length();

        for (int i = 0; i < length; i++) {
            char c = number.charAt(i);
            SmuflGlyph glyph = SmuflGlyph.timeSignatureDigit(c);
            if (glyph == null) continue;

            double width = measureGlyphWidth(font, glyph.unicode());

            // Adăugăm spațiere DOAR dacă nu este ultima cifră
            if (i < length - 1) {
                width += digitSpacing;
            }

            measurements.add(new GlyphMeasurement(glyph, width));
        }

        return measurements;
    }

    /**
     * Draws a number at the specified position, centered within the given maxWidth.
     */
    private void drawNumber(GraphicsContext gc, List<GlyphMeasurement> measurements,
                            double centerX, double y, double maxWidth) {
        double totalWidth = sumWidths(measurements);
        double startX = centerX - totalWidth / 2;

        double currentX = startX;
        for (GlyphMeasurement m : measurements) {
            double glyphWidth = m.width;
            gc.fillText(m.glyph.unicode(), currentX + glyphWidth / 2, y);
            currentX += glyphWidth;
        }
    }

    private double sumWidths(List<GlyphMeasurement> measurements) {
        double total = 0;
        for (GlyphMeasurement m : measurements) {
            total += m.width;
        }
        return total;
    }

    /**
     * Measures the actual width of a glyph using JavaFX Text.
     * This is more accurate than estimating from font size.
     */
    private double measureGlyphWidth(Font font, String glyphText) {
        Text helper = new Text(glyphText);
        helper.setFont(font);
        return helper.getLayoutBounds().getWidth();
    }

    public double calculateWidth(Font font, TimeSignature ts) {
        if (ts == null) return 0;

        switch (ts.displayStyle()) {
            case COMMON -> {
                return measureGlyphWidth(font, SmuflGlyph.TIME_SIG_COMMON.unicode()) + SIDE_PADDING;
            }
            case CUT -> {
                return measureGlyphWidth(font, SmuflGlyph.TIME_SIG_CUT.unicode()) + SIDE_PADDING;
            }
            default -> {
                String numerator = String.valueOf(ts.numerator());
                String denominator = String.valueOf(ts.denominator());

                double numWidth = measureNumberWidth(font, numerator);
                double denWidth = measureNumberWidth(font, denominator);

                return Math.max(numWidth, denWidth) + SIDE_PADDING;
            }
        }
    }

    /**
     * Calculates the width of a number without creating GlyphMeasurement objects.
     * Used only for layout calculation.
     */
    private double measureNumberWidth(Font font, String number) {
        double digitSpacing = font.getSize() * DIGIT_SPACING_FACTOR;
        double totalWidth = 0;
        int length = number.length();

        for (int i = 0; i < length; i++) {
            char c = number.charAt(i);
            SmuflGlyph glyph = SmuflGlyph.timeSignatureDigit(c);
            if (glyph == null) continue;

            double width = measureGlyphWidth(font, glyph.unicode());
            if (i < length - 1) {
                width += digitSpacing;
            }
            totalWidth += width;
        }

        return totalWidth;
    }
}