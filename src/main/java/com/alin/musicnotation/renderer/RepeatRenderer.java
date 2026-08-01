package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.repeat.BarlineType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RepeatRenderer {

    public void drawBarline(GraphicsContext gc, double x, double firstLineY, double staffHeight,
                            double staffLineSpacing, BarlineType type) {
        double bottomY = firstLineY + staffHeight;
        double lineWidth = staffLineSpacing * 0.2;
        double thinWidth = staffLineSpacing * 0.08;
        double gap = staffLineSpacing * 0.3;
        double dotSize = staffLineSpacing * 0.25;
        double dotSpacing = staffLineSpacing * 0.6;
        double midY = firstLineY + staffHeight / 2;

        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);

        switch (type) {
            case SINGLE -> {
                gc.setLineWidth(1);
                gc.strokeLine(x, firstLineY, x, bottomY);
            }
            case START_REPEAT -> {
                gc.setLineWidth(lineWidth);
                gc.strokeLine(x, firstLineY, x, bottomY);
                double thinX = x + gap;
                gc.setLineWidth(thinWidth);
                gc.strokeLine(thinX, firstLineY, thinX, bottomY);
                double dotX = thinX + gap * 0.5;
                double dotOffsetY = dotSpacing / 2;
                gc.fillOval(dotX - dotSize / 2, midY - dotOffsetY - dotSize / 2, dotSize, dotSize);
                gc.fillOval(dotX - dotSize / 2, midY + dotOffsetY - dotSize / 2, dotSize, dotSize);
            }
            case END_REPEAT -> {
                gc.setLineWidth(thinWidth);
                gc.strokeLine(x, firstLineY, x, bottomY);
                double thickX = x + gap;
                gc.setLineWidth(lineWidth);
                gc.strokeLine(thickX, firstLineY, thickX, bottomY);
                double dotX = x - gap * 0.5;
                double dotOffsetY = dotSpacing / 2;
                gc.fillOval(dotX - dotSize / 2, midY - dotOffsetY - dotSize / 2, dotSize, dotSize);
                gc.fillOval(dotX - dotSize / 2, midY + dotOffsetY - dotSize / 2, dotSize, dotSize);
            }
        }
    }

    public void drawFinalBarline(GraphicsContext gc, double x, double firstLineY,
                                 double staffHeight, double staffLineSpacing) {
        double bottomY = firstLineY + staffHeight;
        double thinWidth = staffLineSpacing * 0.08;
        double thickWidth = staffLineSpacing * 0.25;
        double gap = staffLineSpacing * 0.15;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        gc.setLineWidth(thinWidth);
        gc.strokeLine(x, firstLineY, x, bottomY);

        double thickX = x + gap + thinWidth;
        gc.setLineWidth(thickWidth);
        gc.strokeLine(thickX, firstLineY, thickX, bottomY);
    }
}