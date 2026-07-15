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
import java.util.Arrays;
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
    private static final int MIDDLE_LINE_POSITION = BOTTOM_LINE_POSITION / 2;

    private static final double CLEF_X_OFFSET = 6;
    private static final double CLEF_FONT_SIZE_FACTOR = 4.0;
    private static final double TIME_SIGNATURE_X_OFFSET = 72;
    private static final double TIME_SIGNATURE_FONT_SIZE_FACTOR = 2.2;
    private static final double NOTE_START_OFFSET = 120;

    private static final double NOTE_SPACING_PER_QUARTER = 60;
    private static final double MEASURE_START_PADDING = 12 * 2;
    private static final double MEASURE_END_PADDING = 12;
    private static final double NOTE_HEAD_WIDTH = STAFF_LINE_SPACING * 1.4;
    private static final double NOTE_HEAD_HEIGHT = STAFF_LINE_SPACING;
    private static final double STEM_LENGTH_STANDARD = STAFF_LINE_SPACING * 3.5;
    private static final double LEDGER_LINE_EXTENSION = 4;
    private static final double NOTE_HEAD_OUTLINE_WIDTH = 2;
    private static final double FLAG_WIDTH = 10;
    private static final double FLAG_HEIGHT = 14;
    private static final double FLAG_SPACING = 6;
    private static final double BEAM_THICKNESS = 5;
    private static final double BEAM_SPACING = BEAM_THICKNESS + 2;
    private static final double BEAM_HOOK_LENGTH = NOTE_HEAD_WIDTH * 0.6;
    private static final double MAX_BEAM_SLOPE = 0.25;
    private static final double BEAMABLE_EXTRA_SPACING = 8;

    // Constante pentru pauze
    private static final double REST_WIDTH = STAFF_LINE_SPACING * 0.8;
    private static final double REST_HEIGHT = STAFF_LINE_SPACING * 0.6;
    private static final double EIGHTH_REST_SLANT = 2.0;

    // Constante pentru alterații
    private static final double ACCIDENTAL_EXTRA_SPACING = 14;
    private static final double ACCIDENTAL_GAP = 6;

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

    private void drawClef(GraphicsContext gc, double lineStartX, double firstLineY, double staffHeight) {
        double gLineY = firstLineY + staffHeight - 2 * STAFF_LINE_SPACING;
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Serif", STAFF_LINE_SPACING * CLEF_FONT_SIZE_FACTOR));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.fillText("\uD834\uDD1E", lineStartX + CLEF_X_OFFSET, gLineY);
    }

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

    private record NoteLayout(double x, double y, Note note, int position, int startSixteenths) {
    }

    private enum StemDirection {
        UP(-1),
        DOWN(1);

        private final int sign;

        StemDirection(int sign) {
            this.sign = sign;
        }

        int sign() {
            return sign;
        }
    }

    private void drawNotes(GraphicsContext gc, double lineStartX, double firstLineY, double staffHeight) {
        double x = lineStartX + NOTE_START_OFFSET;

        for (Measure measure : score.measures()) {
            List<NoteLayout> layouts = new ArrayList<>();
            double lastNoteX = x;
            Duration lastDuration = null;
            int currentSixteenths = 0;

            for (Note note : measure.notes()) {
                int position = 0;
                double y;
                if (note.isRest()) {
                    y = getRestY(firstLineY, staffHeight, note.duration());
                } else {
                    position = staffPosition(note.pitch());
                    y = firstLineY + position * (STAFF_LINE_SPACING / 2);
                }
                layouts.add(new NoteLayout(x, y, note, position, currentSixteenths));
                lastNoteX = x;
                lastDuration = note.duration();
                double spacing = noteSpacing(note.duration());
                // Spațiu suplimentar pentru notele cu alterație
                if (!note.isRest() && note.pitch().accidental() != Accidental.NONE) {
                    spacing += ACCIDENTAL_EXTRA_SPACING;
                }
                x += spacing;
                currentSixteenths += note.duration().sixteenths();
            }
            drawMeasureNotes(gc, firstLineY, layouts);

            double durationGap = lastDuration != null ? noteSpacing(lastDuration) / 2 : 0;
            double barX = lastNoteX + NOTE_HEAD_WIDTH / 2 + durationGap + MEASURE_END_PADDING;
            drawBarLine(gc, barX, firstLineY, staffHeight);

            x = barX + MEASURE_START_PADDING;
        }
    }

    private double getRestY(double firstLineY, double staffHeight, Duration duration) {
        double line3 = firstLineY + 2 * STAFF_LINE_SPACING;
        double line4 = firstLineY + 3 * STAFF_LINE_SPACING;

        if (duration == Duration.WHOLE) {
            return line4 - REST_HEIGHT;
        } else if (duration == Duration.HALF) {
            return line3 + REST_HEIGHT;
        } else {
            return line3 + STAFF_LINE_SPACING * 0.1;
        }
    }

    private void drawMeasureNotes(GraphicsContext gc, double firstLineY, List<NoteLayout> layouts) {
        // Desenăm capete, pauze și linii adiționale
        for (NoteLayout layout : layouts) {
            if (layout.note().isRest()) {
                drawRest(gc, layout.x(), layout.y(), layout.note().duration());
            } else {
                drawLedgerLines(gc, layout.x(), firstLineY, layout.position());
                drawAccidental(gc, layout.x(), layout.y(), layout.note().pitch().accidental());
                drawNoteHead(gc, layout.x(), layout.y(), layout.note().duration());
            }
        }

        // Desenăm tijele pentru notele care nu sunt beamable (sferturi, jumătăți, întregi)
        // și pentru notele care sunt beamable dar nu sunt grupate (cazuri rare)
        for (NoteLayout layout : layouts) {
            if (layout.note().isRest()) continue;
            Duration duration = layout.note().duration();
            if (!duration.isBeamable()) {
                drawUnbeamedNote(gc, layout);
            }
        }

        // Procesăm grupurile de beam
        for (List<NoteLayout> group : groupBeamsByBeat(layouts)) {
            if (group.size() >= 2) {
                drawBeamedGroup(gc, group);
            } else if (group.size() == 1) {
                // Deja desenat mai sus pentru non-beamable, dar pentru beamable singure trebuie să desenăm tijele
                drawUnbeamedNote(gc, group.get(0));
            }
        }
    }

    private List<List<NoteLayout>> groupBeamsByBeat(List<NoteLayout> layouts) {
        List<List<NoteLayout>> groups = new ArrayList<>();
        List<NoteLayout> currentGroup = null;
        int currentBeat = -1;

        for (NoteLayout layout : layouts) {
            if (!layout.note().duration().isBeamable()) {
                if (currentGroup != null && !currentGroup.isEmpty()) {
                    groups.add(currentGroup);
                    currentGroup = null;
                }
                continue;
            }
            int beat = layout.startSixteenths() / 4;
            if (currentGroup == null) {
                currentGroup = new ArrayList<>();
                currentGroup.add(layout);
                currentBeat = beat;
            } else if (beat == currentBeat) {
                currentGroup.add(layout);
            } else {
                groups.add(currentGroup);
                currentGroup = new ArrayList<>();
                currentGroup.add(layout);
                currentBeat = beat;
            }
        }
        if (currentGroup != null && !currentGroup.isEmpty()) {
            groups.add(currentGroup);
        }
        return groups;
    }

    private void flushRun(List<List<NoteLayout>> groups, List<NoteLayout> currentRun) {
        if (!currentRun.isEmpty()) {
            groups.add(List.copyOf(currentRun));
            currentRun.clear();
        }
    }

    private StemDirection stemDirection(int position) {
        return position > MIDDLE_LINE_POSITION ? StemDirection.UP : StemDirection.DOWN;
    }

    private StemDirection stemDirection(List<NoteLayout> group) {
        double averagePosition = group.stream()
                .mapToInt(NoteLayout::position)
                .average()
                .orElse(MIDDLE_LINE_POSITION);
        return averagePosition > MIDDLE_LINE_POSITION ? StemDirection.UP : StemDirection.DOWN;
    }

    private double stemAttachX(NoteLayout layout, StemDirection direction) {
        return direction == StemDirection.UP
                ? layout.x() + NOTE_HEAD_WIDTH / 2
                : layout.x() - NOTE_HEAD_WIDTH / 2;
    }

    /**
     * Calculează coordonata Y a vârfului tijei pentru o notă individuală.
     * Regula:
     * - normal: lungime = 3.5 spații.
     * - dacă nota este în afara portativului, tija se prelungește până la linia mediană.
     */
    private double computeStemTipY(NoteLayout layout, StemDirection direction, double middleLineY) {
        double y = layout.y();
        int position = layout.position();

        // Linia mediană pentru această notă (absolută)
        double noteMiddleLineY = y + (MIDDLE_LINE_POSITION - position) * (STAFF_LINE_SPACING / 2);
        // Folosim middleLineY global pentru consistență
        double targetMiddleLineY = middleLineY;

        double standardTipY = y + direction.sign() * STEM_LENGTH_STANDARD;

        if (direction == StemDirection.UP) {
            // Pentru note sub linia mediană, tija trebuie să ajungă cel puțin la middleLineY
            if (y > middleLineY) {
                return Math.min(standardTipY, targetMiddleLineY);
            } else {
                return standardTipY;
            }
        } else { // DOWN
            // Pentru note deasupra liniei mediane, tija trebuie să ajungă cel puțin la middleLineY
            if (y < middleLineY) {
                return Math.max(standardTipY, targetMiddleLineY);
            } else {
                return standardTipY;
            }
        }
    }

    private void drawUnbeamedNote(GraphicsContext gc, NoteLayout layout) {
        if (layout.note().isRest()) {
            return;
        }

        Duration duration = layout.note().duration();
        if (!duration.requiresStem()) {
            return;
        }

        StemDirection direction = stemDirection(layout.position());
        double stemX = stemAttachX(layout, direction);
        double middleLineY = layout.y() + (MIDDLE_LINE_POSITION - layout.position()) * (STAFF_LINE_SPACING / 2);
        double stemTipY = computeStemTipY(layout, direction, middleLineY);
        drawStem(gc, stemX, layout.y(), stemTipY);

        if (duration.flagCount() > 0) {
            drawFlags(gc, stemX, stemTipY, duration.flagCount(), direction);
        }
    }

    private void drawBeamedGroup(GraphicsContext gc, List<NoteLayout> group) {
        int[] flagCounts = group.stream()
                .mapToInt(layout -> layout.note().duration().flagCount())
                .toArray();
        int maxFlags = Arrays.stream(flagCounts).max().orElse(0);
        if (maxFlags == 0) return;

        StemDirection direction = stemDirection(group);
        int sign = direction.sign();

        NoteLayout first = group.get(0);
        NoteLayout last = group.get(group.size() - 1);
        double xStart = stemAttachX(first, direction);
        double xEnd   = stemAttachX(last, direction);
        double yStartTip = first.y() + sign * STEM_LENGTH_STANDARD;
        double yEndTip   = last.y()  + sign * STEM_LENGTH_STANDARD;

        double slope = (yEndTip - yStartTip) / (xEnd - xStart);
        slope = Math.max(-MAX_BEAM_SLOPE, Math.min(MAX_BEAM_SLOPE, slope));

        double b = sign < 0 ? Double.MAX_VALUE : -Double.MAX_VALUE;
        for (int i = 0; i < group.size(); i++) {
            NoteLayout layout = group.get(i);
            double stemX = stemAttachX(layout, direction);
            double candidateB = (layout.y() + sign * STEM_LENGTH_STANDARD) - slope * stemX
                    - sign * (flagCounts[i] - 1) * BEAM_SPACING;
            if (sign < 0) {
                if (candidateB < b) b = candidateB;
            } else {
                if (candidateB > b) b = candidateB;
            }
        }

        for (int i = 0; i < group.size(); i++) {
            NoteLayout layout = group.get(i);
            double stemX = stemAttachX(layout, direction);
            double topY = slope * stemX + b - sign * (flagCounts[i] - 1) * BEAM_SPACING;
            drawStem(gc, stemX, layout.y(), topY);
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
                int lastIdx  = run[1];
                double x1 = stemAttachX(group.get(firstIdx), direction);
                double x2 = stemAttachX(group.get(lastIdx), direction);
                double y1 = slope * x1 + b - sign * (level - 1) * BEAM_SPACING;
                double y2 = slope * x2 + b - sign * (level - 1) * BEAM_SPACING;

                if (firstIdx == lastIdx) {
                    drawSlopedHook(gc, x1, y1, slope, firstIdx, group.size());
                    if (level > 1) {
                        gc.setStroke(Color.BLACK);
                        gc.setLineWidth(1);
                        gc.strokeLine(x1, y1, x1, y1 - sign * BEAM_SPACING);
                    }
                } else {
                    drawBeam(gc, x1, y1, x2, y2);
                    if (level == 2) {
                        gc.setStroke(Color.BLACK);
                        gc.setLineWidth(1);
                        gc.strokeLine(x1, y1, x1, y1 - sign * BEAM_SPACING);
                        gc.strokeLine(x2, y2, x2, y2 - sign * BEAM_SPACING);
                    }
                }
            }
        }
    }

    private void drawBeam(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        double thickness = BEAM_THICKNESS;
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

    private void drawSlopedHook(GraphicsContext gc, double stemX, double y, double slope, int index, int total) {
        double length = BEAM_HOOK_LENGTH;
        double direction = (index == 0) ? 1 : (index == total - 1) ? -1 : 1;
        double endX = stemX + direction * length;
        double endY = y + slope * direction * length;
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(BEAM_THICKNESS);
        gc.strokeLine(stemX, y, endX, endY);
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

    private void drawFlags(GraphicsContext gc, double stemX, double stemTipY, int flagCount, StemDirection direction) {
        int towardNoteheadSign = -direction.sign();
        for (int i = 0; i < flagCount; i++) {
            double flagStartY = stemTipY + i * FLAG_SPACING * towardNoteheadSign;
            drawFlag(gc, stemX, flagStartY, direction);
        }
    }

    private void drawFlag(GraphicsContext gc, double startX, double startY, StemDirection direction) {
        int towardNoteheadSign = -direction.sign();
        double controlX = startX + FLAG_WIDTH;
        double controlY = startY + towardNoteheadSign * FLAG_HEIGHT / 2;
        double endX = startX + FLAG_WIDTH * 0.6;
        double endY = startY + towardNoteheadSign * FLAG_HEIGHT;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.beginPath();
        gc.moveTo(startX, startY);
        gc.quadraticCurveTo(controlX, controlY, endX, endY);
        gc.stroke();
    }

    // ===== DESENAREA ALTERAȚIILOR =====

    private void drawAccidental(GraphicsContext gc, double x, double y, Accidental accidental) {
        if (accidental == Accidental.NONE) return;
        String symbol = accidental.unicodeSymbol();
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Serif", STAFF_LINE_SPACING * 1.2));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        double symbolX = x - NOTE_HEAD_WIDTH / 2 - ACCIDENTAL_GAP;
        gc.fillText(symbol, symbolX, y);
    }

    // ===== DESENAREA PAUZELOR =====

    private void drawRest(GraphicsContext gc, double x, double y, Duration duration) {
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        gc.setLineWidth(1.5);

        double w = REST_WIDTH;
        double h = REST_HEIGHT;

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
            double slant = EIGHTH_REST_SLANT;
            double height = STAFF_LINE_SPACING * 0.8;
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

    // ===== SFÂRȘIT DESENARE PAUZE =====

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
        double base = NOTE_SPACING_PER_QUARTER * duration.toQuarterNoteFraction();
        if (duration.isBeamable()) {
            base += BEAMABLE_EXTRA_SPACING;
        }
        return base;
    }

    private int staffPosition(Pitch pitch) {
        return diatonicIndex(TOP_LINE_PITCH) - diatonicIndex(pitch);
    }

    private static int diatonicIndex(Pitch pitch) {
        return pitch.octave() * 7 + pitch.step().ordinal();
    }

    private static Score createSampleScore() {
        Measure measure1 = new Measure();
        measure1.addNote(new Note(Pitch.of("C4"), Duration.WHOLE));

        Measure measure2 = new Measure();
        measure2.addNote(new Note(Pitch.of("F#4"), Duration.HALF));
        measure2.addNote(new Note(Pitch.of("Bb4"), Duration.HALF));

        Measure measure3 = new Measure();
        measure3.addNote(new Note(Pitch.of("Fn5"), Duration.QUARTER));
        measure3.addNote(new Note(Pitch.of("C5"), Duration.QUARTER));
        measure3.addNote(new Note(Pitch.of("G#4"), Duration.EIGHTH));
        measure3.addNote(new Note(Pitch.of("E4"), Duration.EIGHTH));

        Score sampleScore = new Score();
        sampleScore.addMeasure(measure1);
        sampleScore.addMeasure(measure2);
        sampleScore.addMeasure(measure3);

        return sampleScore;
    }

    public static void main(String[] args) {
        launch(args);
    }
}