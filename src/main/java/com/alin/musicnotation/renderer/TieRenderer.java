package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.layout.NoteLayout;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Map;

/**
 * Desenează tie-uri (legături de durată) între note consecutive cu același pitch.
 * Suportă tie-uri în aceeași măsură (MVP).
 */
public class TieRenderer {

    private static final double TIE_LINE_WIDTH = 2.0;

    public void draw(GraphicsContext gc,
                     Measure measure,
                     Map<Note, NoteLayout> layoutMap,
                     double middleLineY,
                     int middleLinePosition,
                     double tieOffset,
                     double staffLineSpacing,
                     double noteHeadWidth) {

        for (Note note : measure.notes()) {
            if (!note.tieStart() || note.isRest()) continue;

            NoteLayout first = layoutMap.get(note);
            if (first == null) continue;

            NoteLayout second = findTieEnd(measure, note, layoutMap);
            if (second == null) continue;

            // Determinăm dacă tie-ul e deasupra sau dedesubt
            boolean above = first.position() <= middleLinePosition;

            drawTie(gc, first, second, above, tieOffset);
        }
    }

    private NoteLayout findTieEnd(Measure measure, Note start, Map<Note, NoteLayout> layoutMap) {
        boolean passedStart = false;
        for (Note candidate : measure.notes()) {
            if (candidate == start) {
                passedStart = true;
                continue;
            }
            if (!passedStart) continue;

            if (candidate.tieEnd()
                    && !candidate.isRest()
                    && candidate.pitch().equals(start.pitch())) {
                return layoutMap.get(candidate);
            }
        }
        return null;
    }

    private void drawTie(GraphicsContext gc, NoteLayout first, NoteLayout second,
                         boolean above, double offset) {
        double x1 = first.x();
        double y1 = first.y();
        double x2 = second.x();
        double y2 = second.y();

        double midX = (x1 + x2) / 2;
        double controlY;

        if (above) {
            double topY = Math.min(y1, y2);
            controlY = topY - offset;
        } else {
            double bottomY = Math.max(y1, y2);
            controlY = bottomY + offset;
        }

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(TIE_LINE_WIDTH);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.beginPath();
        gc.moveTo(x1, y1);
        gc.quadraticCurveTo(midX, controlY, x2, y2);
        gc.stroke();
    }
}