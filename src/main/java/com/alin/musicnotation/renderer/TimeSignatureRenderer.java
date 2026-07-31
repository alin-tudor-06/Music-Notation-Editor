package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.TimeSignature;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

public class TimeSignatureRenderer {

    private static final double TIME_SIGNATURE_FONT_SIZE_FACTOR = 2.2;
    private static final String COMMON_TIME_SYMBOL = "\uD834\uDD45";
    private static final String CUT_TIME_SYMBOL = "\uD834\uDD46";

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

        Font font = Font.font("Serif", FontWeight.BOLD, staffLineSpacing * TIME_SIGNATURE_FONT_SIZE_FACTOR);
        gc.setFont(font);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        switch (ts.displayStyle()) {
            case COMMON -> {
                gc.fillText(COMMON_TIME_SYMBOL, x, firstLineY + staffHeight / 2);
            }
            case CUT -> {
                gc.fillText(CUT_TIME_SYMBOL, x, firstLineY + staffHeight / 2);
            }
            default -> {
                double topY = firstLineY + staffHeight * 0.25;
                double bottomY = firstLineY + staffHeight * 0.75;
                gc.fillText(String.valueOf(ts.numerator()), x, topY);
                gc.fillText(String.valueOf(ts.denominator()), x, bottomY);
            }
        }

        return calculateWidth(font, ts);
    }

    public double calculateWidth(Font font, TimeSignature ts) {
        if (ts == null) return 0;

        switch (ts.displayStyle()) {
            case COMMON -> {
                Text text = new Text(COMMON_TIME_SYMBOL);
                text.setFont(font);
                return text.getLayoutBounds().getWidth() + 4;
            }
            case CUT -> {
                Text text = new Text(CUT_TIME_SYMBOL);
                text.setFont(font);
                return text.getLayoutBounds().getWidth() + 4;
            }
            default -> {
                Text topText = new Text(String.valueOf(ts.numerator()));
                Text bottomText = new Text(String.valueOf(ts.denominator()));
                topText.setFont(font);
                bottomText.setFont(font);
                double topWidth = topText.getLayoutBounds().getWidth();
                double bottomWidth = bottomText.getLayoutBounds().getWidth();
                return Math.max(topWidth, bottomWidth) + 4;
            }
        }
    }
}