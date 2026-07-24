package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.hairpin.Hairpin;
import com.alin.musicnotation.model.hairpin.HairpinType;
import com.alin.musicnotation.model.hairpin.HairpinPlacement;
import com.alin.musicnotation.model.layout.NoteLayout;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Map;

public class HairpinRenderer {

    private static final double HAIRPIN_HEIGHT_FACTOR = 1.0;
    private static final double HAIRPIN_OFFSET_FACTOR = 2.0;

    public void draw(GraphicsContext gc, Measure measure, Map<Note, NoteLayout> layoutMap,
                     double firstLineY, double staffHeight, double staffLineSpacing,
                     double noteHeadWidth) {

        if (measure.hairpins().isEmpty()) return;

        double staffBottomY = firstLineY + staffHeight;
        double staffTopY = firstLineY;

        for (Hairpin hairpin : measure.hairpins()) {
            NoteLayout startLayout = layoutMap.get(hairpin.startNote());
            NoteLayout endLayout = layoutMap.get(hairpin.endNote());
            if (startLayout == null || endLayout == null) continue;

            double x1 = startLayout.x() + noteHeadWidth / 2;
            double x2 = endLayout.x() - noteHeadWidth / 2;

            if (x1 > x2) {
                double temp = x1;
                x1 = x2;
                x2 = temp;
            }

            double height = staffLineSpacing * HAIRPIN_HEIGHT_FACTOR;
            double halfHeight = height / 2;
            double offset = staffLineSpacing * HAIRPIN_OFFSET_FACTOR;

            double yBase;
            switch (hairpin.placement()) {
                case ABOVE -> yBase = staffTopY - offset;
                case BELOW -> yBase = staffBottomY + offset;
                default -> throw new IllegalStateException("Unexpected placement: " + hairpin.placement());
            }

            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1.5);
            gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
            gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

            if (hairpin.type() == HairpinType.CRESCENDO) {
                gc.strokeLine(x1, yBase, x2, yBase - halfHeight);
                gc.strokeLine(x1, yBase, x2, yBase + halfHeight);
            } else { // DECRESCENDO
                gc.strokeLine(x1, yBase - halfHeight, x2, yBase);
                gc.strokeLine(x1, yBase + halfHeight, x2, yBase);
            }
        }
    }
}