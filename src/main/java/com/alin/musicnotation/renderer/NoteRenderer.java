package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Duration;
import com.alin.musicnotation.model.layout.NoteLayout;
import com.alin.musicnotation.model.layout.StemDirection;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Map;


public class NoteRenderer {

    public void draw(GraphicsContext gc, Measure measure, Map<Note, NoteLayout> layoutMap,
                     double firstLineY, double middleLineY, int middleLinePosition,
                     double noteHeadWidth, double noteHeadHeight,
                     double stemLengthStandard, double ledgerLineExtension,
                     double noteHeadOutlineWidth, double flagWidth, double flagHeight,
                     double flagSpacing, double restWidth, double restHeight,
                     double eighthRestSlant, int topLinePosition, int bottomLinePosition,
                     double staffLineSpacing) {

        for (NoteLayout layout : layoutMap.values()) {
            Note note = layout.note();
            if (note.isRest()) {
                drawRest(gc, layout.x(), layout.y(), note.duration(),
                        restWidth, restHeight, eighthRestSlant, staffLineSpacing);
            } else {
                drawLedgerLines(gc, layout.x(), firstLineY, layout.position(),
                        topLinePosition, bottomLinePosition, staffLineSpacing,
                        noteHeadWidth, ledgerLineExtension);
                drawNoteHead(gc, layout.x(), layout.y(), note.duration(),
                        noteHeadWidth, noteHeadHeight, noteHeadOutlineWidth);
            }
        }

        for (NoteLayout layout : layoutMap.values()) {
            Note note = layout.note();
            if (note.isRest()) continue;
            Duration duration = note.duration();
            if (!duration.isBeamable() && duration.requiresStem()) {
                StemDirection dir = RenderingHelpers.stemDirection(layout.position(), middleLinePosition);
                double stemX = RenderingHelpers.stemAttachX(layout, dir, noteHeadWidth);
                double stemTipY = RenderingHelpers.computeStemTipY(layout, dir,
                        stemLengthStandard, middleLineY);
                drawStem(gc, stemX, layout.y(), stemTipY);
                if (duration.flagCount() > 0) {
                    drawFlags(gc, stemX, stemTipY, duration.flagCount(), dir,
                            flagWidth, flagHeight, flagSpacing);
                }
            }
        }
    }

    private void drawNoteHead(GraphicsContext gc, double x, double y, Duration duration,
                              double noteHeadWidth, double noteHeadHeight, double noteHeadOutlineWidth) {
        double headX = x - noteHeadWidth / 2;
        double headY = y - noteHeadHeight / 2;
        if (duration.isFilled()) {
            gc.setFill(Color.BLACK);
            gc.fillOval(headX, headY, noteHeadWidth, noteHeadHeight);
        } else {
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(noteHeadOutlineWidth);
            gc.strokeOval(headX, headY, noteHeadWidth, noteHeadHeight);
        }
    }

    private void drawStem(GraphicsContext gc, double stemX, double stemBottomY, double stemTopY) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(stemX, stemBottomY, stemX, stemTopY);
    }

    private void drawFlag(GraphicsContext gc, double startX, double startY, StemDirection direction,
                          double flagWidth, double flagHeight) {
        int towardNoteheadSign = -direction.sign();
        double controlX = startX + flagWidth;
        double controlY = startY + towardNoteheadSign * flagHeight / 2;
        double endX = startX + flagWidth * 0.6;
        double endY = startY + towardNoteheadSign * flagHeight;
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.beginPath();
        gc.moveTo(startX, startY);
        gc.quadraticCurveTo(controlX, controlY, endX, endY);
        gc.stroke();
    }

    private void drawFlags(GraphicsContext gc, double stemX, double stemTipY, int flagCount,
                           StemDirection direction, double flagWidth, double flagHeight, double flagSpacing) {
        int towardNoteheadSign = -direction.sign();
        for (int i = 0; i < flagCount; i++) {
            double flagStartY = stemTipY + i * flagSpacing * towardNoteheadSign;
            drawFlag(gc, stemX, flagStartY, direction, flagWidth, flagHeight);
        }
    }

    private void drawRest(GraphicsContext gc, double x, double y, Duration duration,
                          double restWidth, double restHeight, double eighthRestSlant,
                          double staffLineSpacing) {
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        gc.setLineWidth(1.5);
        double w = restWidth;
        double h = restHeight;

        if (duration == Duration.WHOLE) {
            gc.fillRect(x - w / 2, y, w, h);
        } else if (duration == Duration.HALF) {
            gc.fillRect(x - w / 2, y - h, w, h);
        } else if (duration == Duration.QUARTER) {
            double cx = x;
            double cy = y;
            gc.beginPath();
            gc.moveTo(cx - w / 2, cy + h / 2);
            gc.quadraticCurveTo(cx - w / 2, cy - h / 2, cx, cy - h / 2);
            gc.quadraticCurveTo(cx + w / 2, cy - h / 2, cx + w / 2, cy);
            gc.quadraticCurveTo(cx + w / 2, cy + h / 2, cx, cy + h / 2);
            gc.quadraticCurveTo(cx - w / 2, cy + h / 2, cx - w / 2, cy + h / 2);
            gc.closePath();
            gc.fill();
        } else if (duration == Duration.EIGHTH || duration == Duration.SIXTEENTH) {
            double slant = eighthRestSlant;
            double height = staffLineSpacing * 0.8;
            double startX = x - w / 2;
            double startY = y - height / 2;
            double endX = x + w / 2;
            double endY = y + height / 2 + slant;
            gc.strokeLine(startX, startY, endX, endY);
            int hooks = (duration == Duration.SIXTEENTH) ? 2 : 1;
            for (int i = 0; i < hooks; i++) {
                double hookX = endX - i * 4;
                double hookY = endY - i * 6;
                gc.strokeLine(endX, endY, hookX, hookY);
            }
        }
    }

    private void drawLedgerLines(GraphicsContext gc, double x, double firstLineY, int position,
                                 int topLinePosition, int bottomLinePosition, double staffLineSpacing,
                                 double noteHeadWidth, double ledgerLineExtension) {
        if (position < topLinePosition) {
            int count = -position / 2;
            for (int i = 1; i <= count; i++) {
                int ledgerPosition = -2 * i;
                double y = firstLineY + ledgerPosition * (staffLineSpacing / 2);
                drawLedgerLine(gc, x, y, noteHeadWidth, ledgerLineExtension);
            }
        } else if (position > bottomLinePosition) {
            int count = (position - bottomLinePosition) / 2;
            for (int i = 1; i <= count; i++) {
                int ledgerPosition = bottomLinePosition + 2 * i;
                double y = firstLineY + ledgerPosition * (staffLineSpacing / 2);
                drawLedgerLine(gc, x, y, noteHeadWidth, ledgerLineExtension);
            }
        }
    }

    private void drawLedgerLine(GraphicsContext gc, double x, double y,
                                double noteHeadWidth, double ledgerLineExtension) {
        double halfWidth = noteHeadWidth / 2 + ledgerLineExtension;
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(x - halfWidth, y, x + halfWidth, y);
    }
}