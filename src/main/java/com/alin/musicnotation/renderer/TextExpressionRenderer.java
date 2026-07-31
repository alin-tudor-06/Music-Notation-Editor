package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.text.TextExpression;
import com.alin.musicnotation.model.text.TextPlacement;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class TextExpressionRenderer {

    private static final double TEXT_EXPRESSION_FONT_SIZE_FACTOR = 0.9;
    private static final double TEXT_EXPRESSION_STACK_SPACING_FACTOR = 1.2;
    private static final double TEXT_EXPRESSION_OFFSET_ABOVE_FACTOR = 2.5;
    private static final double TEXT_EXPRESSION_OFFSET_BELOW_FACTOR = 1.4;

    public void draw(GraphicsContext gc, Measure measure, double contentStartX,
                     double firstLineY, double staffHeight, double staffLineSpacing) {
        List<TextExpression> expressions = measure.textExpressions();
        if (expressions.isEmpty()) return;

        double staffTopY = firstLineY;
        double staffBottomY = firstLineY + staffHeight;

        double fontHeight = staffLineSpacing * TEXT_EXPRESSION_FONT_SIZE_FACTOR;
        double stackSpacing = staffLineSpacing * TEXT_EXPRESSION_STACK_SPACING_FACTOR;

        // Extra space if tempo is present
        double tempoExtra = measure.tempo() != null ? staffLineSpacing * 1.5 : 0.0;

        // Extra space if hairpin is present (to avoid overlap)
        double hairpinExtra = measure.hairpins().isEmpty() ? 0 : staffLineSpacing * 2;

        Font font = Font.font("Serif", FontWeight.NORMAL, FontPosture.ITALIC, fontHeight);

        gc.setFill(Color.BLACK);
        gc.setFont(font);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(javafx.geometry.VPos.BOTTOM);

        int aboveIndex = 0;
        int belowIndex = 0;

        // Textul de deasupra trebuie să fie mai sus decât hairpin-ul (dacă există)
        // Textul de dedesubt trebuie să fie mai jos decât hairpin-ul (dacă există)
        double aboveExtra = tempoExtra + hairpinExtra;
        double belowExtra = hairpinExtra;

        for (TextExpression expr : expressions) {
            double yBase;
            if (expr.placement() == TextPlacement.ABOVE) {
                double offset = staffLineSpacing * TEXT_EXPRESSION_OFFSET_ABOVE_FACTOR;
                yBase = staffTopY - offset - aboveExtra - aboveIndex * stackSpacing;
                aboveIndex++;
            } else {
                double offset = staffLineSpacing * TEXT_EXPRESSION_OFFSET_BELOW_FACTOR;
                yBase = staffBottomY + offset + belowExtra + belowIndex * stackSpacing;
                belowIndex++;
            }

            gc.fillText(expr.text(), contentStartX, yBase);
        }
    }
}