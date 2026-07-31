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

    private static final double HAIRPIN_OFFSET_FACTOR = 2.0;

    public void draw(GraphicsContext gc, Measure measure, Map<Note, NoteLayout> layoutMap,
                     double firstLineY, double staffHeight, double staffLineSpacing,
                     double noteHeadWidth, double noteHeadHeight) {

        if (measure.hairpins().isEmpty()) return;

        double clearance = staffLineSpacing * 0.3;

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

            double minY = Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;
            for (NoteLayout layout : layoutMap.values()) {
                double x = layout.x();
                if (x >= x1 && x <= x2) {
                    double y = layout.y();
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }

            if (minY == Double.MAX_VALUE) {
                minY = Math.min(startLayout.y(), endLayout.y());
                maxY = Math.max(startLayout.y(), endLayout.y());
            }

            double lowestNoteBottom = maxY + noteHeadHeight / 2;
            double highestNoteTop = minY - noteHeadHeight / 2;

            double height = staffLineSpacing * 1.0;
            double halfHeight = height / 2;
            double offset = staffLineSpacing * HAIRPIN_OFFSET_FACTOR;

            double yBase;
            if (hairpin.placement() == HairpinPlacement.ABOVE) {
                yBase = highestNoteTop - offset - clearance - halfHeight;
            } else {
                yBase = lowestNoteBottom + offset + clearance + halfHeight;
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