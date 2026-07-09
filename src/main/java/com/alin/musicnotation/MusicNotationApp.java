package com.alin.musicnotation;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MusicNotationApp extends Application {

    private static final double WINDOW_WIDTH = 800;
    private static final double WINDOW_HEIGHT = 600;

    private static final int STAFF_LINE_COUNT = 5;
    private static final double STAFF_LINE_SPACING = 16;
    private static final double STAFF_MARGIN = 60;

    private static final Pitch TOP_LINE_PITCH = Pitch.of("F5");
    private static final int TOP_LINE_POSITION = 0;
    private static final int BOTTOM_LINE_POSITION = (STAFF_LINE_COUNT - 1) * 2;

    private static final double NOTE_START_OFFSET = 40;
    private static final double NOTE_SPACING_PER_QUARTER = 60;
    private static final double NOTE_HEAD_WIDTH = STAFF_LINE_SPACING * 1.4;
    private static final double NOTE_HEAD_HEIGHT = STAFF_LINE_SPACING;
    private static final double STEM_HEIGHT = STAFF_LINE_SPACING * 3;
    private static final double LEDGER_LINE_EXTENSION = 4;
    private static final double NOTE_HEAD_OUTLINE_WIDTH = 2;
    private static final double FLAG_WIDTH = 10;
    private static final double FLAG_HEIGHT = 14;
    private static final double FLAG_SPACING = 6;

    private final Score score = createSampleScore();

    @Override
    public void start(Stage primaryStage) {
        MenuBar menuBar = new MenuBar(
                new Menu("File"),
                new Menu("Edit"),
                new Menu("View"),
                new Menu("Playback")
        );

        Canvas canvas = new Canvas();
        Pane workspace = new Pane(canvas);
        canvas.widthProperty().bind(workspace.widthProperty());
        canvas.heightProperty().bind(workspace.heightProperty());
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> drawStaff(canvas));
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> drawStaff(canvas));

        Label statusBar = new Label("Ready");

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(workspace);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        primaryStage.setTitle("Music Notation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void drawStaff(Canvas canvas) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        double staffHeight = (STAFF_LINE_COUNT - 1) * STAFF_LINE_SPACING;
        double firstLineY = (height - staffHeight) / 2;
        double lineStartX = STAFF_MARGIN;
        double lineEndX = width - STAFF_MARGIN;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        for (int i = 0; i < STAFF_LINE_COUNT; i++) {
            double y = firstLineY + i * STAFF_LINE_SPACING;
            gc.strokeLine(lineStartX, y, lineEndX, y);
        }

        drawBarLine(gc, lineStartX, firstLineY, staffHeight);
        drawBarLine(gc, lineEndX, firstLineY, staffHeight);

        drawNotes(gc, lineStartX, firstLineY);
    }

    private void drawBarLine(GraphicsContext gc, double x, double firstLineY, double staffHeight) {
        gc.strokeLine(x, firstLineY, x, firstLineY + staffHeight);
    }

    private void drawNotes(GraphicsContext gc, double lineStartX, double firstLineY) {
        double x = lineStartX + NOTE_START_OFFSET;

        for (Measure measure : score.measures()) {
            for (Note note : measure.notes()) {
                int position = staffPosition(note.pitch());
                double y = firstLineY + position * (STAFF_LINE_SPACING / 2);
                drawLedgerLines(gc, x, firstLineY, position);
                drawNote(gc, x, y, note);
                x += noteSpacing(note.duration());
            }
        }
    }

    private void drawNote(GraphicsContext gc, double x, double y, Note note) {
        Duration duration = note.duration();
        drawNoteHead(gc, x, y, duration);

        if (duration.requiresStem()) {
            double stemX = x + NOTE_HEAD_WIDTH / 2;
            double stemTopY = y - STEM_HEIGHT;
            drawStem(gc, stemX, y, stemTopY);

            if (duration.flagCount() > 0) {
                drawFlags(gc, stemX, stemTopY, duration.flagCount());
            }
        }
    }

    private void drawNoteHead(GraphicsContext gc, double x, double y, Duration duration) {
        double headX = x - NOTE_HEAD_WIDTH / 2;
        double headY = y - NOTE_HEAD_HEIGHT / 2;

        if (duration.isFilled()) {
            gc.setFill(Color.BLACK);
            gc.fillOval(headX, headY, NOTE_HEAD_WIDTH, NOTE_HEAD_HEIGHT);
        } else {
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(NOTE_HEAD_OUTLINE_WIDTH);
            gc.strokeOval(headX, headY, NOTE_HEAD_WIDTH, NOTE_HEAD_HEIGHT);
        }
    }

    private void drawStem(GraphicsContext gc, double stemX, double stemBottomY, double stemTopY) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(stemX, stemBottomY, stemX, stemTopY);
    }

    private void drawFlags(GraphicsContext gc, double stemX, double stemTopY, int flagCount) {
        for (int i = 0; i < flagCount; i++) {
            double flagStartY = stemTopY + i * FLAG_SPACING;
            drawFlag(gc, stemX, flagStartY);
        }
    }

    private void drawFlag(GraphicsContext gc, double startX, double startY) {
        double controlX = startX + FLAG_WIDTH;
        double controlY = startY + FLAG_HEIGHT / 2;
        double endX = startX + FLAG_WIDTH * 0.6;
        double endY = startY + FLAG_HEIGHT;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.beginPath();
        gc.moveTo(startX, startY);
        gc.quadraticCurveTo(controlX, controlY, endX, endY);
        gc.stroke();
    }

    private void drawLedgerLines(GraphicsContext gc, double x, double firstLineY, int position) {
        if (position < TOP_LINE_POSITION) {
            int count = -position / 2;
            for (int i = 1; i <= count; i++) {
                int ledgerPosition = -2 * i;
                double y = firstLineY + ledgerPosition * (STAFF_LINE_SPACING / 2);
                drawLedgerLine(gc, x, y);
            }
        } else if (position > BOTTOM_LINE_POSITION) {
            int count = (position - BOTTOM_LINE_POSITION) / 2;
            for (int i = 1; i <= count; i++) {
                int ledgerPosition = BOTTOM_LINE_POSITION + 2 * i;
                double y = firstLineY + ledgerPosition * (STAFF_LINE_SPACING / 2);
                drawLedgerLine(gc, x, y);
            }
        }
    }

    private void drawLedgerLine(GraphicsContext gc, double x, double y) {
        double halfWidth = NOTE_HEAD_WIDTH / 2 + LEDGER_LINE_EXTENSION;
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(x - halfWidth, y, x + halfWidth, y);
    }

    private double noteSpacing(Duration duration) {
        return NOTE_SPACING_PER_QUARTER * duration.toQuarterNoteFraction();
    }

    private int staffPosition(Pitch pitch) {
        return diatonicIndex(TOP_LINE_PITCH) - diatonicIndex(pitch);
    }

    private static int diatonicIndex(Pitch pitch) {
        return pitch.octave() * 7 + pitch.step().ordinal();
    }

    private static Score createSampleScore() {
        Measure wholeNoteMeasure = new Measure();
        wholeNoteMeasure.addNote(new Note(Pitch.of("C4"), Duration.WHOLE));

        Measure mixedMeasure = new Measure();
        mixedMeasure.addNote(new Note(Pitch.of("C4"), Duration.HALF));
        mixedMeasure.addNote(new Note(Pitch.of("E4"), Duration.QUARTER));
        mixedMeasure.addNote(new Note(Pitch.of("G4"), Duration.EIGHTH));
        mixedMeasure.addNote(new Note(Pitch.of("A5"), Duration.SIXTEENTH));
        mixedMeasure.addNote(new Note(Pitch.of("B5"), Duration.SIXTEENTH));

        Score sampleScore = new Score();
        sampleScore.addMeasure(wholeNoteMeasure);
        sampleScore.addMeasure(mixedMeasure);
        return sampleScore;
    }

    public static void main(String[] args) {
        launch(args);
    }
}