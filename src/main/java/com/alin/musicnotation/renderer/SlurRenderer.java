package com.alin.musicnotation.renderer;

import com.alin.musicnotation.Measure;
import com.alin.musicnotation.Note;
import com.alin.musicnotation.model.NoteLayout;
import com.alin.musicnotation.Slur;
import com.alin.musicnotation.model.StemDirection;
import com.alin.musicnotation.MusicNotationApp;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;

public class SlurRenderer {

    public void draw(GraphicsContext gc, Measure measure, Map<Note, NoteLayout> layoutMap,
                     double middleLineY, double slurOffset, double staffLineSpacing,
                     double noteHeadWidth) {

        if (measure.slurs().isEmpty()) return;

        for (Slur slur : measure.slurs()) {
            List<Note> notes = slur.notes();
            if (notes.size() < 2) continue;

            List<NoteLayout> noteLayouts = new ArrayList<>();
            for (Note note : notes) {
                for (NoteLayout layout : layoutMap.values()) {
                    if (layout.note() == note) {
                        noteLayouts.add(layout);
                        break;
                    }
                }
            }
            if (noteLayouts.size() < 2) continue;

            NoteLayout first = noteLayouts.get(0);
            NoteLayout last = noteLayouts.get(noteLayouts.size() - 1);

            boolean above = determineSlurDirection(noteLayouts, middleLineY);
            SlurGeometry geom = computeSlurGeometry(first, last, above, slurOffset);
            drawSlurGeometry(gc, geom);
        }
    }

    private boolean determineSlurDirection(List<NoteLayout> noteLayouts, double middleLineY) {
        boolean allUp = true;
        boolean allDown = true;
        for (NoteLayout layout : noteLayouts) {
            StemDirection dir = MusicNotationApp.stemDirection(layout.position());
            if (dir == StemDirection.DOWN) allUp = false;
            if (dir == StemDirection.UP) allDown = false;
        }
        if (allUp && !allDown) {
            return false;
        } else if (allDown && !allUp) {
            return true;
        } else {
            return true;
        }
    }

    private SlurGeometry computeSlurGeometry(NoteLayout first, NoteLayout last,
                                             boolean above, double slurOffset) {
        double startX = first.x();
        double startY = first.y();
        double endX = last.x();
        double endY = last.y();
        double midX = (startX + endX) / 2;
        double controlY;
        if (above) {
            double topY = Math.min(startY, endY);
            controlY = topY - slurOffset;
        } else {
            double bottomY = Math.max(startY, endY);
            controlY = bottomY + slurOffset;
        }
        return new SlurGeometry(startX, startY, midX, controlY, endX, endY);
    }

    private void drawSlurGeometry(GraphicsContext gc, SlurGeometry geom) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2.0);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.beginPath();
        gc.moveTo(geom.startX, geom.startY);
        gc.quadraticCurveTo(geom.controlX, geom.controlY, geom.endX, geom.endY);
        gc.stroke();
    }

    private static class SlurGeometry {
        final double startX, startY, controlX, controlY, endX, endY;
        SlurGeometry(double startX, double startY, double controlX, double controlY, double endX, double endY) {
            this.startX = startX;
            this.startY = startY;
            this.controlX = controlX;
            this.controlY = controlY;
            this.endX = endX;
            this.endY = endY;
        }
    }
}