package com.alin.musicnotation.renderer;

import com.alin.musicnotation.Duration;
import com.alin.musicnotation.Measure;
import com.alin.musicnotation.Note;
import com.alin.musicnotation.model.NoteLayout;
import com.alin.musicnotation.model.StemDirection;
import com.alin.musicnotation.Tuplet;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.*;

public class TupletRenderer {

    public void draw(GraphicsContext gc, Measure measure, Map<Note, NoteLayout> layoutMap,
                     int middleLinePosition,
                     double tupletOffset, double tupletBracketHeight,
                     double tupletNumberFontSizeFactor, double noteHeadWidth,
                     double stemLengthStandard, double staffLineSpacing) {

        if (measure.tuplets().isEmpty()) return;

        for (Tuplet tuplet : measure.tuplets()) {
            List<Note> notes = tuplet.notes();
            if (notes.isEmpty()) continue;

            List<NoteLayout> noteLayouts = new ArrayList<>();
            for (Note note : notes) {
                for (NoteLayout layout : layoutMap.values()) {
                    if (layout.note() == note) {
                        noteLayouts.add(layout);
                        break;
                    }
                }
            }
            if (noteLayouts.isEmpty()) continue;

            double minX = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double highestPoint = Double.MAX_VALUE;

            for (NoteLayout layout : noteLayouts) {
                double left = layout.x() - noteHeadWidth / 2;
                double right = layout.x() + noteHeadWidth / 2;
                if (left < minX) minX = left;
                if (right > maxX) maxX = right;

                double topOfNote = layout.y();
                Duration dur = layout.note().duration();
                if (dur.requiresStem()) {
                    StemDirection dir = RenderingHelpers.stemDirection(layout.position(), middleLinePosition);
                    if (dir == StemDirection.UP) {
                        topOfNote = layout.y() - stemLengthStandard;
                    }
                }
                if (topOfNote < highestPoint) {
                    highestPoint = topOfNote;
                }
            }

            double centerX = (minX + maxX) / 2;

            boolean allBeamable = notes.stream().allMatch(n -> n.duration().isBeamable());
            boolean inOneBeam = false;
            if (allBeamable) {
                List<NoteLayout> beamableLayouts = new ArrayList<>();
                for (Note note : notes) {
                    for (NoteLayout layout : layoutMap.values()) {
                        if (layout.note() == note && !layout.note().isRest() && layout.note().duration().isBeamable()) {
                            beamableLayouts.add(layout);
                        }
                    }
                }
                List<List<NoteLayout>> beamGroups = RenderingHelpers.groupBeamsByBeat(beamableLayouts);
                for (List<NoteLayout> group : beamGroups) {
                    if (group.stream().allMatch(l -> notes.contains(l.note()))) {
                        inOneBeam = true;
                        break;
                    }
                }
            }

            boolean allSameDuration = notes.stream().allMatch(n -> n.duration() == notes.get(0).duration());
            boolean drawBracket = !inOneBeam || !allSameDuration;

            double yBase;
            if (inOneBeam && allSameDuration) {
                yBase = highestPoint - staffLineSpacing;
            } else {
                yBase = highestPoint - tupletOffset;
            }

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Serif", staffLineSpacing * tupletNumberFontSizeFactor));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(javafx.geometry.VPos.BOTTOM);
            gc.fillText(String.valueOf(tuplet.numerator()), centerX, yBase);

            if (drawBracket) {
                double bracketY = yBase - 2;
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);
                gc.strokeLine(minX, bracketY, maxX, bracketY);
                gc.strokeLine(minX, bracketY, minX, bracketY + tupletBracketHeight);
                gc.strokeLine(maxX, bracketY, maxX, bracketY + tupletBracketHeight);
            }
        }
    }
}