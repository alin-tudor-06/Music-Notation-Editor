package com.alin.musicnotation.renderer;

import com.alin.musicnotation.Measure;
import com.alin.musicnotation.Note;                // ← import adăugat
import com.alin.musicnotation.model.NoteLayout;
import com.alin.musicnotation.model.StemDirection;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;

public class BeamRenderer {

    // Semnătura corectă: gc, measure, layoutMap, middleLineY, middleLinePosition,
    // apoi 6 parametri double: beamThickness, beamSpacing, beamHookLength, maxBeamSlope, stemLengthStandard, noteHeadWidth
    public void draw(GraphicsContext gc, Measure measure, Map<Note, NoteLayout> layoutMap,
                     double middleLineY, int middleLinePosition,
                     double beamThickness, double beamSpacing,
                     double beamHookLength, double maxBeamSlope,
                     double stemLengthStandard, double noteHeadWidth) {

        List<NoteLayout> beamableLayouts = new ArrayList<>();
        for (NoteLayout layout : layoutMap.values()) {
            if (!layout.note().isRest() && layout.note().duration().isBeamable()) {
                beamableLayouts.add(layout);
            }
        }
        beamableLayouts.sort(Comparator.comparingDouble(NoteLayout::x));

        List<List<NoteLayout>> groups = RenderingHelpers.groupBeamsByBeat(beamableLayouts);
        for (List<NoteLayout> group : groups) {
            if (group.size() >= 2) {
                drawBeamedGroup(gc, group, middleLineY, middleLinePosition,
                        beamThickness, beamSpacing, beamHookLength,
                        maxBeamSlope, stemLengthStandard, noteHeadWidth);
            }
        }
    }

    private void drawBeamedGroup(GraphicsContext gc, List<NoteLayout> group,
                                 double middleLineY, int middleLinePosition,
                                 double beamThickness, double beamSpacing,
                                 double beamHookLength, double maxBeamSlope,
                                 double stemLengthStandard, double noteHeadWidth) {
        int[] flagCounts = group.stream()
                .mapToInt(l -> l.note().duration().flagCount())
                .toArray();
        int maxFlags = Arrays.stream(flagCounts).max().orElse(0);
        if (maxFlags == 0) return;

        StemDirection direction = RenderingHelpers.stemDirection(group, middleLinePosition);
        int sign = direction.sign();

        NoteLayout first = group.get(0);
        NoteLayout last = group.get(group.size() - 1);
        double xStart = RenderingHelpers.stemAttachX(first, direction, noteHeadWidth);
        double xEnd = RenderingHelpers.stemAttachX(last, direction, noteHeadWidth);
        double yStartTip = first.y() + sign * stemLengthStandard;
        double yEndTip = last.y() + sign * stemLengthStandard;

        double slope = (yEndTip - yStartTip) / (xEnd - xStart);
        slope = Math.max(-maxBeamSlope, Math.min(maxBeamSlope, slope));

        double b = sign < 0 ? Double.MAX_VALUE : -Double.MAX_VALUE;
        for (int i = 0; i < group.size(); i++) {
            NoteLayout layout = group.get(i);
            double stemX = RenderingHelpers.stemAttachX(layout, direction, noteHeadWidth);
            double candidateB = (layout.y() + sign * stemLengthStandard) - slope * stemX
                    - sign * (flagCounts[i] - 1) * beamSpacing;
            if (sign < 0) {
                if (candidateB < b) b = candidateB;
            } else {
                if (candidateB > b) b = candidateB;
            }
        }

        for (int i = 0; i < group.size(); i++) {
            NoteLayout layout = group.get(i);
            double stemX = RenderingHelpers.stemAttachX(layout, direction, noteHeadWidth);
            double topY = slope * stemX + b - sign * (flagCounts[i] - 1) * beamSpacing;
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeLine(stemX, layout.y(), stemX, topY);
        }

        for (int level = 1; level <= maxFlags; level++) {
            List<int[]> runs = new ArrayList<>();
            int start = -1;
            for (int i = 0; i < group.size(); i++) {
                if (flagCounts[i] >= level) {
                    if (start == -1) start = i;
                } else {
                    if (start != -1) {
                        runs.add(new int[]{start, i - 1});
                        start = -1;
                    }
                }
            }
            if (start != -1) runs.add(new int[]{start, group.size() - 1});

            for (int[] run : runs) {
                int firstIdx = run[0];
                int lastIdx = run[1];
                double x1 = RenderingHelpers.stemAttachX(group.get(firstIdx), direction, noteHeadWidth);
                double x2 = RenderingHelpers.stemAttachX(group.get(lastIdx), direction, noteHeadWidth);
                double y1 = slope * x1 + b - sign * (level - 1) * beamSpacing;
                double y2 = slope * x2 + b - sign * (level - 1) * beamSpacing;

                if (firstIdx == lastIdx) {
                    drawSlopedHook(gc, x1, y1, slope, firstIdx, group.size(),
                            beamHookLength, beamThickness);
                    if (level > 1) {
                        gc.setStroke(Color.BLACK);
                        gc.setLineWidth(1);
                        gc.strokeLine(x1, y1, x1, y1 - sign * beamSpacing);
                    }
                } else {
                    drawBeam(gc, x1, y1, x2, y2, beamThickness);
                    if (level == 2) {
                        gc.setStroke(Color.BLACK);
                        gc.setLineWidth(1);
                        gc.strokeLine(x1, y1, x1, y1 - sign * beamSpacing);
                        gc.strokeLine(x2, y2, x2, y2 - sign * beamSpacing);
                    }
                }
            }
        }
    }

    private void drawBeam(GraphicsContext gc, double x1, double y1, double x2, double y2,
                          double beamThickness) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.hypot(dx, dy);
        if (len == 0) return;
        double nx = -dy / len * beamThickness / 2;
        double ny = dx / len * beamThickness / 2;
        double[] xPoints = {x1 + nx, x1 - nx, x2 - nx, x2 + nx};
        double[] yPoints = {y1 + ny, y1 - ny, y2 - ny, y2 + ny};
        gc.setFill(Color.BLACK);
        gc.fillPolygon(xPoints, yPoints, 4);
    }

    private void drawSlopedHook(GraphicsContext gc, double stemX, double y, double slope,
                                int index, int total, double beamHookLength, double beamThickness) {
        double length = beamHookLength;
        double direction = (index == 0) ? 1 : (index == total - 1) ? -1 : 1;
        double endX = stemX + direction * length;
        double endY = y + slope * direction * length;
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(beamThickness);
        gc.strokeLine(stemX, y, endX, endY);
    }
}