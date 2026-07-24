package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.clef.ClefType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

public class ClefRenderer {

    private static final double CLEF_FONT_SIZE_FACTOR = 3.5;

    public void drawClef(GraphicsContext gc, double x, double firstLineY,
                         double staffHeight, double staffLineSpacing, ClefType clefType) {
        if (clefType == null) return;

        double refY = firstLineY + clefType.referenceLineIndex() * staffLineSpacing;

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Serif", FontWeight.LIGHT, staffLineSpacing * CLEF_FONT_SIZE_FACTOR));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(clefType.unicodeSymbol(), x, refY);
    }
}