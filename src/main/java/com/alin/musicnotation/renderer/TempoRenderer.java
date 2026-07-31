package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.tempo.Tempo;
import com.alin.musicnotation.model.tempo.TempoPlacement;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class TempoRenderer {

    private static final double TEMPO_FONT_SIZE_FACTOR = 0.9;
    private static final double TEMPO_OFFSET_FACTOR = 3.0; // păstrat valoarea originală

    public void draw(GraphicsContext gc, Measure measure, double x, double firstLineY, double staffLineSpacing) {
        Tempo tempo = measure.tempo();
        if (tempo == null) return;

        double yBase = switch (tempo.placement()) {
            case ABOVE -> firstLineY - staffLineSpacing * TEMPO_OFFSET_FACTOR;
            case BELOW -> firstLineY + 4 * staffLineSpacing + staffLineSpacing * TEMPO_OFFSET_FACTOR;
        };

        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(javafx.geometry.VPos.BOTTOM);

        Font font = Font.font("Serif", FontWeight.NORMAL, staffLineSpacing * TEMPO_FONT_SIZE_FACTOR);
        gc.setFont(font);

        String displayText = buildTempoText(tempo);
        if (!displayText.isEmpty()) {
            gc.fillText(displayText, x, yBase);
        }
    }

    private String buildTempoText(Tempo tempo) {
        StringBuilder sb = new StringBuilder();
        if (tempo.hasText()) {
            sb.append(tempo.text());
        }
        if (tempo.hasBpm()) {
            if (tempo.hasText()) {
                sb.append(" ");
            }
            sb.append("♩ = ").append(tempo.bpm());
        }
        return sb.toString();
    }
}