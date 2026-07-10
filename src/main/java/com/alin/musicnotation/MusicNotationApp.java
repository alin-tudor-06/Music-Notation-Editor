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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MusicNotationApp extends Application {

    private static final double WINDOW_WIDTH = 800;
    private static final double WINDOW_HEIGHT = 600;

    private static final int STAFF_LINE_COUNT = 5;
    private static final double STAFF_LINE_SPACING = 16;
    private static final double STAFF_MARGIN = 60;

    private static final Pitch TOP_LINE_PITCH = Pitch.of("F5");
    private static final int TOP_LINE_POSITION = 0;
    private static final int BOTTOM_LINE_POSITION = (STAFF_LINE_COUNT - 1) * 2;

    // Horizontal layout of the header (clef + time signature), reserved
    // before any notes are drawn. All offsets are relative to lineStartX.
    private static final double CLEF_X_OFFSET = 6;
    private static final double CLEF_FONT_SIZE_FACTOR = 4.0; // relative to STAFF_LINE_SPACING
    private static final double TIME_SIGNATURE_X_OFFSET = 72;
    private static final double TIME_SIGNATURE_FONT_SIZE_FACTOR = 2.2; // relative to STAFF_LINE_SPACING
    private static final double NOTE_START_OFFSET = 120;

    private static final double NOTE_SPACING_PER_QUARTER = 60;
    private static final double MEASURE_START_PADDING = 12*2;
    private static final double MEASURE_END_PADDING = 12;
    private static final double NOTE_HEAD_WIDTH = STAFF_LINE_SPACING * 1.4;
    private static final double NOTE_HEAD_HEIGHT = STAFF_LINE_SPACING;
    private static final double STEM_HEIGHT = STAFF_LINE_SPACING * 3;
    private static final double LEDGER_LINE_EXTENSION = 4;
    private static final double NOTE_HEAD_OUTLINE_WIDTH = 2;
    private static final double FLAG_WIDTH = 10;
    private static final double FLAG_HEIGHT = 14;
    private static final double FLAG_SPACING = 6;
    private static final double BEAM_THICKNESS = 5;
    private static final double MAX_BEAM_SLOPE = 0.25;

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

        drawClef(gc, lineStartX, firstLineY, staffHeight);
        drawTimeSignature(gc, lineStartX, firstLineY, staffHeight);

        drawNotes(gc, lineStartX, firstLineY, staffHeight);
    }

    private void drawBarLine(GraphicsContext gc, double x, double firstLineY, double staffHeight) {
        gc.strokeLine(x, firstLineY, x, firstLineY + staffHeight);
    }

    /**
     * Draws a treble (G) clef at the start of the staff, using the Unicode
     * musical symbol glyph rendered with a serif font. Purely visual - no
     * Clef model object exists yet; the G clef is simply assumed, matching
     * TOP_LINE_PITCH already being hardcoded to F5 elsewhere in this class.
     * <p>
     * The clef's curl is conventionally centered on the G4 line (the second
     * line from the bottom), so the glyph is vertically anchored there.
     * The font size is capped to the staff height so the glyph stays fully
     * within the 5 lines instead of overflowing above/below the staff.
     */
    private void drawClef(GraphicsContext gc, double lineStartX, double firstLineY, double staffHeight) {
        double gLineY = firstLineY + staffHeight - 2 * STAFF_LINE_SPACING;

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Serif", STAFF_LINE_SPACING * CLEF_FONT_SIZE_FACTOR));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.fillText("\uD834\uDD1E", lineStartX + CLEF_X_OFFSET, gLineY);
    }

    /**
     * Draws a fixed 4/4 time signature as two stacked digits, one centered
     * on the top half of the staff and one on the bottom half. No
     * TimeSignature model exists yet; the meter is assumed 4/4, matching
     * Measure's own hardcoded capacity. Positioned with enough horizontal
     * offset to sit clearly clear of the (now smaller) clef glyph.
     */
    private void drawTimeSignature(GraphicsContext gc, double lineStartX, double firstLineY, double staffHeight) {
        double x = lineStartX + TIME_SIGNATURE_X_OFFSET;
        double topY = firstLineY + staffHeight * 0.25;
        double bottomY = firstLineY + staffHeight * 0.75;

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, STAFF_LINE_SPACING * TIME_SIGNATURE_FONT_SIZE_FACTOR));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.fillText("4", x, topY);
        gc.fillText("4", x, bottomY);
    }

    /**
     * The computed screen position of a single note, laid out horizontally
     * along the staff. Used as an intermediate step so that consecutive
     * beamable notes can be grouped before stems/beams are drawn.
     */
    private record NoteLayout(double x, double y, Note note, int position) {
    }

    /**
     * Lays out and draws each measure in sequence, then closes it with a
     * vertical bar line.
     * <p>
     * The gap between the last note and the bar line is made of two parts:
     * - half of that note's own duration-based spacing (the same
     *   {@link #noteSpacing(Duration)} used between notes), so a WHOLE or
     *   HALF note - which visually "owns" more horizontal space - keeps a
     *   proportionally larger, more natural gap before the bar line than a
     *   QUARTER, EIGHTH or SIXTEENTH note;
     * - a small fixed {@link #MEASURE_END_PADDING}, guaranteeing a minimum
     *   breathing room even for the shortest durations.
     */
    private void drawNotes(GraphicsContext gc, double lineStartX, double firstLineY, double staffHeight) {
        double x = lineStartX + NOTE_START_OFFSET;

        for (Measure measure : score.measures()) {
            List<NoteLayout> layouts = new ArrayList<>();
            double lastNoteX = x;
            Duration lastDuration = null;
            for (Note note : measure.notes()) {
                int position = staffPosition(note.pitch());
                double y = firstLineY + position * (STAFF_LINE_SPACING / 2);
                layouts.add(new NoteLayout(x, y, note, position));
                lastNoteX = x;
                lastDuration = note.duration();
                x += noteSpacing(note.duration());
            }
            drawMeasureNotes(gc, firstLineY, layouts);

            double durationGap = lastDuration != null ? noteSpacing(lastDuration) / 2 : 0;
            double barX = lastNoteX + NOTE_HEAD_WIDTH / 2 + durationGap + MEASURE_END_PADDING;
            drawBarLine(gc, barX, firstLineY, staffHeight);

            x = barX + MEASURE_START_PADDING;
        }
    }

    private void drawMeasureNotes(GraphicsContext gc, double firstLineY, List<NoteLayout> layouts) {
        for (NoteLayout layout : layouts) {
            drawLedgerLines(gc, layout.x(), firstLineY, layout.position());
            drawNoteHead(gc, layout.x(), layout.y(), layout.note().duration());
        }

        for (List<NoteLayout> group : groupBeamableNotes(layouts)) {
            if (group.size() >= 2) {
                drawBeamedGroup(gc, group);
            } else {
                drawUnbeamedNote(gc, group.get(0));
            }
        }
    }

    /**
     * Splits a measure's notes into runs of consecutive beamable notes
     * (eighth notes and shorter) and single-note groups for everything
     * else. A run of length 1 is treated the same as a non-beamable note.
     */
    private List<List<NoteLayout>> groupBeamableNotes(List<NoteLayout> layouts) {
        List<List<NoteLayout>> groups = new ArrayList<>();
        List<NoteLayout> currentRun = new ArrayList<>();

        for (NoteLayout layout : layouts) {
            if (layout.note().duration().isBeamable()) {
                currentRun.add(layout);
            } else {
                flushRun(groups, currentRun);
                groups.add(List.of(layout));
            }
        }
        flushRun(groups, currentRun);

        return groups;
    }

    private void flushRun(List<List<NoteLayout>> groups, List<NoteLayout> currentRun) {
        if (!currentRun.isEmpty()) {
            groups.add(List.copyOf(currentRun));
            currentRun.clear();
        }
    }

    private void drawUnbeamedNote(GraphicsContext gc, NoteLayout layout) {
        Duration duration = layout.note().duration();
        if (!duration.requiresStem()) {
            return;
        }

        double stemX = layout.x() + NOTE_HEAD_WIDTH / 2;
        double stemTopY = layout.y() - STEM_HEIGHT;
        drawStem(gc, stemX, layout.y(), stemTopY);

        if (duration.flagCount() > 0) {
            drawFlags(gc, stemX, stemTopY, duration.flagCount());
        }
    }

    /**
     * Draws stems for every note in the group up to a single shared beam
     * height, then connects them with one horizontal beam. Beam height is
     * the highest (smallest y) of the individual stem tops, so shorter
     * stems are simply extended to reach the beam - stems always point up,
     * and the beam is never sloped.
     */

    private void drawBeamedGroup(GraphicsContext gc, List<NoteLayout> group) {
        NoteLayout first = group.get(0);
        NoteLayout last = group.get(group.size() - 1);

        double xStart = first.x() + NOTE_HEAD_WIDTH / 2;
        double xEnd = last.x() + NOTE_HEAD_WIDTH / 2;
        double yStartTip = first.y() - STEM_HEIGHT;
        double yEndTip = last.y() - STEM_HEIGHT;

        // Calculează panta inițială
        double slope = (yEndTip - yStartTip) / (xEnd - xStart);
        // Limitează panta
        slope = Math.max(-MAX_BEAM_SLOPE, Math.min(MAX_BEAM_SLOPE, slope));

        // Calculează interceptul b pentru a plasa beam-ul deasupra tuturor notelor
        double b = Double.MAX_VALUE;
        for (NoteLayout layout : group) {
            double stemX = layout.x() + NOTE_HEAD_WIDTH / 2;
            double candidateB = (layout.y() - STEM_HEIGHT) - slope * stemX;
            if (candidateB < b) b = candidateB;
        }

        // Desenează tijele
        for (NoteLayout layout : group) {
            double stemX = layout.x() + NOTE_HEAD_WIDTH / 2;
            double beamY = slope * stemX + b;
            drawStem(gc, stemX, layout.y(), beamY);
        }

        // Desenează beam-ul înclinat (versiunea cu linie groasă)
        drawBeam(gc, xStart, slope * xStart + b, xEnd, slope * xEnd + b);
    }

    private void drawBeam(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        double thickness = BEAM_THICKNESS;
        // Calculează un vector perpendicular pe direcția beam-ului
        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.hypot(dx, dy);
        if (len == 0) return;
        double nx = -dy / len * thickness / 2;
        double ny =  dx / len * thickness / 2;

        double[] xPoints = { x1 + nx, x1 - nx, x2 - nx, x2 + nx };
        double[] yPoints = { y1 + ny, y1 - ny, y2 - ny, y2 + ny };
        gc.setFill(Color.BLACK);
        gc.fillPolygon(xPoints, yPoints, 4);
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