package com.alin.musicnotation;

import com.alin.musicnotation.layout.HorizontalLayout;
import com.alin.musicnotation.layout.MeasureSpacingEngine;
import com.alin.musicnotation.layout.MeasureSpacingResult;
import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.articulation.*;
import com.alin.musicnotation.model.clef.ClefType;
import com.alin.musicnotation.model.dynamic.*;
import com.alin.musicnotation.model.hairpin.*;
import com.alin.musicnotation.model.layout.*;
import com.alin.musicnotation.model.repeat.BarlineType;
import com.alin.musicnotation.model.tempo.*;
import com.alin.musicnotation.model.text.TextExpression;
import com.alin.musicnotation.model.text.TextPlacement;
import com.alin.musicnotation.renderer.*;
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

import java.util.*;

public class MusicNotationApp extends Application {

    private static final double WINDOW_WIDTH = 800;
    private static final double WINDOW_HEIGHT = 600;

    private static final int STAFF_LINE_COUNT = 5;
    private static final double STAFF_LINE_SPACING = 16;
    private static final double STAFF_MARGIN = 60;

    private static final int TOP_LINE_POSITION = 0;
    private static final int BOTTOM_LINE_POSITION = (STAFF_LINE_COUNT - 1) * 2;
    private static final int MIDDLE_LINE_POSITION = BOTTOM_LINE_POSITION / 2;

    private static final double CLEF_X_OFFSET = 6;
    private static final double KEY_SIGNATURE_X_OFFSET = 44;
    private static final double KEY_ACCIDENTAL_SPACING = 8;
    private static final double ACCIDENTAL_FONT_SIZE_FACTOR = 1.2;
    private static final double CLEF_FONT_SIZE_FACTOR = 4.0;
    private static final double TIME_SIGNATURE_FONT_SIZE_FACTOR = 2.2;
    private static final double TIME_SIGNATURE_WIDTH = 30;

    private static final double NOTE_SPACING_PER_QUARTER = 60;
    private static final double MEASURE_START_PADDING = 12 * 2;
    private static final double MEASURE_END_PADDING = 12;
    public static final double NOTE_HEAD_WIDTH = STAFF_LINE_SPACING * 1.4;
    private static final double NOTE_HEAD_HEIGHT = STAFF_LINE_SPACING;
    public static final double STEM_LENGTH_STANDARD = STAFF_LINE_SPACING * 3.5;
    private static final double LEDGER_LINE_EXTENSION = 6;
    private static final double NOTE_HEAD_OUTLINE_WIDTH = 2;
    private static final double FLAG_WIDTH = 10;
    private static final double FLAG_HEIGHT = 14;
    private static final double FLAG_SPACING = 6;
    private static final double BEAM_THICKNESS = 5;
    private static final double BEAM_SPACING = BEAM_THICKNESS + 2;
    private static final double BEAM_HOOK_LENGTH = NOTE_HEAD_WIDTH * 0.6;
    private static final double MAX_BEAM_SLOPE = 0.25;
    private static final double BEAMABLE_EXTRA_SPACING = 8;
    private static final double ACCIDENTAL_EXTRA_SPACING = 14;
    private static final double ACCIDENTAL_GAP = 6;

    private static final double REST_WIDTH = STAFF_LINE_SPACING * 0.8;
    private static final double REST_HEIGHT = STAFF_LINE_SPACING * 0.6;
    private static final double EIGHTH_REST_SLANT = 2.0;

    private static final double TUPLET_OFFSET = STAFF_LINE_SPACING * 2.0;
    private static final double TUPLET_BRACKET_HEIGHT = STAFF_LINE_SPACING * 0.5;
    private static final double TUPLET_NUMBER_FONT_SIZE_FACTOR = 0.9;

    private static final double SLUR_OFFSET = STAFF_LINE_SPACING * 1.2;
    private static final double ARTICULATION_FONT_SIZE_FACTOR = 1.0;
    private static final double ARTICULATION_OFFSET = STAFF_LINE_SPACING * 0.8; // ← adăugat
    private static final double DYNAMIC_FONT_SIZE_FACTOR = 0.8;
    private static final double DYNAMIC_OFFSET = STAFF_LINE_SPACING * 1.8; // ← adăugat

    private static final double REPEAT_GAP = 14;
    private static final double CLEF_CHANGE_LEFT_PADDING = 6;
    private static final double CLEF_AFTER_SPACING = 15;
    private static final double CLEF_CHANGE_SPACING = 40;

    private final NoteRenderer noteRenderer = new NoteRenderer();
    private final BeamRenderer beamRenderer = new BeamRenderer();
    private final TupletRenderer tupletRenderer = new TupletRenderer();
    private final SlurRenderer slurRenderer = new SlurRenderer();
    private final ArticulationRenderer articulationRenderer = new ArticulationRenderer();
    private final DynamicRenderer dynamicRenderer = new DynamicRenderer();
    private final HairpinRenderer hairpinRenderer = new HairpinRenderer();
    private final TempoRenderer tempoRenderer = new TempoRenderer();
    private final RepeatRenderer repeatRenderer = new RepeatRenderer();
    private final TextExpressionRenderer textExpressionRenderer = new TextExpressionRenderer();
    private final ClefRenderer clefRenderer = new ClefRenderer();
    private final KeySignatureRenderer keySignatureRenderer = new KeySignatureRenderer();
    private final TimeSignatureRenderer timeSignatureRenderer = new TimeSignatureRenderer();

    private final MeasureSpacingEngine spacingEngine = new MeasureSpacingEngine(NOTE_SPACING_PER_QUARTER);

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

        drawSingleStaff(gc, firstLineY, staffHeight, lineStartX, lineEndX);
    }

    private void drawSingleStaff(GraphicsContext gc, double firstLineY, double staffHeight,
                                 double lineStartX, double lineEndX) {
        ClefType initialClef = score.initialClef();

        clefRenderer.drawClef(gc, lineStartX + CLEF_X_OFFSET, firstLineY, staffHeight,
                STAFF_LINE_SPACING, initialClef);

        double currentX = lineStartX + CLEF_X_OFFSET + KEY_SIGNATURE_X_OFFSET;
        double keyWidth = keySignatureRenderer.drawInitial(gc, currentX, firstLineY,
                score.keySignature(), initialClef, STAFF_LINE_SPACING);
        currentX += keyWidth;

        double timeSigX = currentX + 10;
        double timeSigWidth = timeSignatureRenderer.drawInitial(gc, timeSigX, firstLineY,
                staffHeight, STAFF_LINE_SPACING, score.initialTimeSignature());
        currentX = timeSigX + timeSigWidth;

        double noteStartX = currentX + MEASURE_START_PADDING;

        List<NoteLayout> allLayouts = drawNotes(gc, firstLineY, staffHeight, noteStartX,
                lineStartX, initialClef);
        drawTies(gc, allLayouts);
    }

    private List<NoteLayout> drawNotes(GraphicsContext gc, double firstLineY, double staffHeight,
                                       double startX, double lineStartX, ClefType initialClef) {
        List<NoteLayout> allLayouts = new ArrayList<>();
        double x = startX;
        double previousBarX = lineStartX;
        boolean isFirstMeasure = true;
        ClefType currentClef = initialClef;
        KeySignature currentKey = score.keySignature();
        TimeSignature currentTime = score.initialTimeSignature();

        for (Measure measure : score.measures()) {
            // ---- Start repeat ----
            if (measure.hasStartRepeat()) {
                double leftBarX;
                if (isFirstMeasure) {
                    leftBarX = x - REPEAT_GAP;
                } else {
                    leftBarX = previousBarX;
                }
                repeatRenderer.drawBarline(gc, leftBarX, firstLineY, staffHeight, STAFF_LINE_SPACING, BarlineType.START_REPEAT);
            }

            // ---- Clef change ----
            ClefType newClef = measure.clef();
            if (newClef != null) {
                double clefX = x + CLEF_CHANGE_LEFT_PADDING;
                clefRenderer.drawClef(gc, clefX, firstLineY, staffHeight,
                        STAFF_LINE_SPACING, newClef);
                currentClef = newClef;
                x += CLEF_CHANGE_SPACING + CLEF_AFTER_SPACING;
            }

            // ---- Key signature change ----
            KeySignature newKey = measure.keySignature();
            if (newKey != null) {
                double keyWidth = keySignatureRenderer.drawChange(gc, x, firstLineY,
                        currentKey, newKey, currentClef, STAFF_LINE_SPACING);
                x += keyWidth + 10;
                currentKey = newKey;
            }

            // ---- Time signature change ----
            TimeSignature newTime = measure.timeSignature();
            if (newTime != null) {
                double timeSigX = x + 10;
                double timeWidth = timeSignatureRenderer.drawChange(gc, timeSigX, firstLineY,
                        staffHeight, STAFF_LINE_SPACING, newTime);
                x = timeSigX + timeWidth;
            }

            // ---- Compute horizontal spacing ----
            MeasureSpacingResult spacingResult = spacingEngine.computeSpacing(measure.notes(), x);

            // ---- Build full NoteLayout objects (add Y and position) ----
            List<NoteLayout> layouts = new ArrayList<>();
            for (HorizontalLayout h : spacingResult.layouts()) {
                Note note = h.note();
                int position = 0;
                double y;
                if (note.isRest()) {
                    y = getRestY(firstLineY, staffHeight, note.duration());
                } else {
                    position = staffPosition(note.pitch(), currentClef);
                    y = firstLineY + position * (STAFF_LINE_SPACING / 2);
                }
                NoteLayout layout = new NoteLayout(h.x(), y, note, position, h.startSixteenths());
                layouts.add(layout);
            }

            allLayouts.addAll(layouts);

            // ---- Render the measure ----
            double measureStartX = x;
            drawMeasureNotes(gc, firstLineY, measure, layouts, lineStartX, measureStartX, currentClef, currentKey);

            // ---- Barline ----
            double barX = spacingResult.finalCursorX() + MEASURE_END_PADDING;
            if (measure.hasEndRepeat()) {
                repeatRenderer.drawBarline(gc, barX, firstLineY, staffHeight, STAFF_LINE_SPACING, BarlineType.END_REPEAT);
            } else {
                repeatRenderer.drawBarline(gc, barX, firstLineY, staffHeight, STAFF_LINE_SPACING, BarlineType.SINGLE);
            }

            previousBarX = barX;
            isFirstMeasure = false;
            x = barX + MEASURE_START_PADDING;
        }
        return allLayouts;
    }

    private double getRestY(double firstLineY, double staffHeight, Duration duration) {
        double line3 = firstLineY + 2 * STAFF_LINE_SPACING;
        double line4 = firstLineY + 3 * STAFF_LINE_SPACING;
        if (duration == Duration.WHOLE) return line4 - REST_HEIGHT;
        if (duration == Duration.HALF) return line3 + REST_HEIGHT;
        return line3 + STAFF_LINE_SPACING * 0.1;
    }

    private void drawMeasureNotes(GraphicsContext gc, double firstLineY, Measure measure,
                                  List<NoteLayout> layouts, double lineStartX,
                                  double measureStartX, ClefType currentClef,
                                  KeySignature currentKey) {
        Map<Note, NoteLayout> layoutMap = new HashMap<>();
        for (NoteLayout layout : layouts) {
            layoutMap.put(layout.note(), layout);
        }
        double middleLineY = firstLineY + MIDDLE_LINE_POSITION * (STAFF_LINE_SPACING / 2);
        double staffHeight = (STAFF_LINE_COUNT - 1) * STAFF_LINE_SPACING;

        Set<Note> beamedNotes = new HashSet<>();
        List<NoteLayout> beamableLayouts = new ArrayList<>();
        for (NoteLayout layout : layouts) {
            if (!layout.note().isRest() && layout.note().duration().isBeamable()) {
                beamableLayouts.add(layout);
            }
        }
        beamableLayouts.sort(Comparator.comparingDouble(NoteLayout::x));
        List<List<NoteLayout>> beamGroups = RenderingHelpers.groupBeamsByBeat(beamableLayouts);
        for (List<NoteLayout> group : beamGroups) {
            if (group.size() >= 2) {
                for (NoteLayout layout : group) {
                    beamedNotes.add(layout.note());
                }
            }
        }

        // 1. Tempo
        double tempoX = measureStartX;
        tempoRenderer.draw(gc, measure, tempoX, firstLineY, STAFF_LINE_SPACING);

        // 2. Text expressions
        textExpressionRenderer.draw(gc, measure, measureStartX, firstLineY, staffHeight, STAFF_LINE_SPACING);

        // 3. Notes
        noteRenderer.draw(gc, measure, layoutMap, firstLineY, middleLineY, MIDDLE_LINE_POSITION,
                NOTE_HEAD_WIDTH, NOTE_HEAD_HEIGHT, STEM_LENGTH_STANDARD,
                LEDGER_LINE_EXTENSION, NOTE_HEAD_OUTLINE_WIDTH,
                FLAG_WIDTH, FLAG_HEIGHT, FLAG_SPACING,
                REST_WIDTH, REST_HEIGHT, EIGHTH_REST_SLANT,
                TOP_LINE_POSITION, BOTTOM_LINE_POSITION, STAFF_LINE_SPACING,
                beamedNotes, currentKey);

        // 4. Beams
        beamRenderer.draw(gc, measure, layoutMap, middleLineY, MIDDLE_LINE_POSITION,
                BEAM_THICKNESS, BEAM_SPACING, BEAM_HOOK_LENGTH,
                MAX_BEAM_SLOPE, STEM_LENGTH_STANDARD, NOTE_HEAD_WIDTH);

        // 5. Tuplets
        tupletRenderer.draw(gc, measure, layoutMap, MIDDLE_LINE_POSITION,
                TUPLET_OFFSET, TUPLET_BRACKET_HEIGHT, TUPLET_NUMBER_FONT_SIZE_FACTOR,
                NOTE_HEAD_WIDTH, STEM_LENGTH_STANDARD, STAFF_LINE_SPACING);

        // 6. Slurs
        slurRenderer.draw(gc, measure, layoutMap, middleLineY, MIDDLE_LINE_POSITION,
                SLUR_OFFSET, STAFF_LINE_SPACING, NOTE_HEAD_WIDTH);

        // 7. Articulations – adăugat ARTICULATION_OFFSET
        articulationRenderer.draw(gc, measure, layoutMap, middleLineY, MIDDLE_LINE_POSITION,
                ARTICULATION_FONT_SIZE_FACTOR, ARTICULATION_OFFSET, STAFF_LINE_SPACING, NOTE_HEAD_WIDTH);

        // 8. Dynamics – adăugat DYNAMIC_OFFSET
        dynamicRenderer.draw(gc, measure, layoutMap, middleLineY,
                DYNAMIC_FONT_SIZE_FACTOR, DYNAMIC_OFFSET, STAFF_LINE_SPACING);

        // 9. Hairpins
        hairpinRenderer.draw(gc, measure, layoutMap, firstLineY, staffHeight,
                STAFF_LINE_SPACING, NOTE_HEAD_WIDTH, NOTE_HEAD_HEIGHT);
    }

    private void drawTies(GraphicsContext gc, List<NoteLayout> allLayouts) {
        if (allLayouts.size() < 2) return;
        for (int i = 0; i < allLayouts.size() - 1; i++) {
            NoteLayout current = allLayouts.get(i);
            NoteLayout next = allLayouts.get(i + 1);
            if (current.note().isRest() || next.note().isRest()) continue;
            if (!current.note().tieStart() || !next.note().tieEnd()) continue;
            if (!current.note().pitch().equals(next.note().pitch())) continue;
            boolean above = determineTieDirection(current, next);
            double startX = current.x() + NOTE_HEAD_WIDTH / 2;
            double endX = next.x() - NOTE_HEAD_WIDTH / 2;
            double startY = current.y();
            double endY = next.y();
            drawTie(gc, startX, startY, endX, endY, above);
        }
    }

    private boolean determineTieDirection(NoteLayout first, NoteLayout second) {
        StemDirection dir1 = RenderingHelpers.stemDirection(first.position(), MIDDLE_LINE_POSITION);
        StemDirection dir2 = RenderingHelpers.stemDirection(second.position(), MIDDLE_LINE_POSITION);
        if (dir1 == dir2) {
            return dir1 == StemDirection.DOWN;
        }
        return true;
    }

    private void drawTie(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean above) {
        double midX = (startX + endX) / 2;
        double offset = STAFF_LINE_SPACING * 0.75;
        double controlY = above ? startY - offset : startY + offset;
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2.0);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.beginPath();
        gc.moveTo(startX, startY);
        gc.quadraticCurveTo(midX, controlY, endX, endY);
        gc.stroke();
    }

    private int staffPosition(Pitch pitch, ClefType clef) {
        return diatonicIndex(clef.referencePitch()) - diatonicIndex(pitch);
    }

    private static int diatonicIndex(Pitch pitch) {
        return pitch.octave() * 7 + pitch.step().ordinal();
    }

    private static Score createSampleScore() {
        Score score = new Score(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);

        // Măsura 1: Tempo + Text deasupra + Articulație dedesubt + Ties
        Measure measure1 = new Measure();
        measure1.addStartRepeat();
        measure1.addTempo(new Tempo("Allegro"));
        measure1.addTextExpression(new TextExpression("dolce", TextPlacement.ABOVE));

        Note n1 = new Note(Pitch.of("C4"), Duration.QUARTER);
        Note n2 = new Note(Pitch.of("C4"), Duration.QUARTER, true, false);
        Note n3 = new Note(Pitch.of("C4"), Duration.QUARTER, false, true);
        Note n4 = new Note(Pitch.of("F4"), Duration.QUARTER);
        measure1.addNote(n1);
        measure1.addNote(n2);
        measure1.addNote(n3);
        measure1.addNote(n4);
        measure1.addArticulation(new Articulation(n4, ArticulationType.STACCATO));
        score.addMeasure(measure1);

        // Măsura 2: Slur + Hairpin crescendo + Dinamică
        Measure measure2 = new Measure();
        measure2.addTempo(new Tempo("Andante", 80));
        measure2.addTextExpression(new TextExpression("cantabile", TextPlacement.BELOW));

        Note c4 = new Note(Pitch.of("C4"), Duration.QUARTER);
        Note d4 = new Note(Pitch.of("D4"), Duration.QUARTER);
        Note e4 = new Note(Pitch.of("E4"), Duration.QUARTER);
        Note f4 = new Note(Pitch.of("F4"), Duration.QUARTER);
        measure2.addNote(c4);
        measure2.addNote(d4);
        measure2.addNote(e4);
        measure2.addNote(f4);
        measure2.addSlur(new Slur(List.of(c4, d4, e4, f4)));
        measure2.addDynamic(new Dynamic(c4, DynamicType.P));
        measure2.addHairpin(new Hairpin(c4, f4, HairpinType.CRESCENDO));
        score.addMeasure(measure2);

        // Măsura 3: Tuplet + Beams + Articulații + Dinamică + Schimbări
        Measure measure3 = new Measure();
        measure3.addTimeSignature(TimeSignature.SIX_EIGHT);
        measure3.addKeySignature(KeySignature.G_MAJOR);
        measure3.addTextExpression(new TextExpression("espressivo", TextPlacement.ABOVE));

        Note g4 = new Note(Pitch.of("G4"), Duration.EIGHTH);
        Note a4 = new Note(Pitch.of("A4"), Duration.EIGHTH);
        Note b4 = new Note(Pitch.of("B4"), Duration.EIGHTH);
        Note c5 = new Note(Pitch.of("C5"), Duration.EIGHTH);
        measure3.addNote(g4);
        measure3.addNote(a4);
        measure3.addNote(b4);
        measure3.addNote(c5);
        measure3.addTuplet(new Tuplet(3, 2, List.of(g4, a4, b4)));
        measure3.addArticulation(new Articulation(g4, ArticulationType.ACCENT));
        measure3.addDynamic(new Dynamic(g4, DynamicType.F));
        measure3.addEndRepeat();
        score.addMeasure(measure3);

        // Măsura 4: Clef change (Bass) + Text dedesubt + Dinamică

        Measure measure4 = new Measure();
        measure4.addClef(ClefType.BASS);
        measure3.addTimeSignature(TimeSignature.NINE_EIGHT);
        measure4.addTextExpression(new TextExpression("simile", TextPlacement.BELOW));

        Note c3 = new Note(Pitch.of("C3"), Duration.QUARTER);
        Note d3 = new Note(Pitch.of("D3"), Duration.QUARTER);
        Note e3 = new Note(Pitch.of("E3"), Duration.QUARTER);
        Note f3 = new Note(Pitch.of("F3"), Duration.QUARTER);
        measure4.addNote(c3);
        measure4.addNote(d3);
        measure4.addNote(e3);
        measure4.addNote(f3);
        measure4.addDynamic(new Dynamic(c3, DynamicType.MF));
        score.addMeasure(measure4);

        // Măsura 5: Hairpin decrescendo + Articulații + Ties + Beam
        Measure measure5 = new Measure();
        measure5.addTempo(Tempo.ofBpm(96));
        measure5.addTextExpression(new TextExpression("con brio", TextPlacement.ABOVE));
        measure5.addTextExpression(new TextExpression("sempre", TextPlacement.BELOW));

        Note x1 = new Note(Pitch.of("C4"), Duration.QUARTER);
        Note x2 = new Note(Pitch.of("D4"), Duration.QUARTER, true, false);
        Note x3 = new Note(Pitch.of("E4"), Duration.QUARTER);
        Note x4 = new Note(Pitch.of("F4"), Duration.QUARTER, false, true);
        measure5.addNote(x1);
        measure5.addNote(x2);
        measure5.addNote(x3);
        measure5.addNote(x4);
        measure5.addArticulation(new Articulation(x1, ArticulationType.STACCATISSIMO));
        measure5.addArticulation(new Articulation(x2, ArticulationType.MARCATO));
        measure5.addDynamic(new Dynamic(x1, DynamicType.MP));
        measure5.addDynamic(new Dynamic(x4, DynamicType.FF));
        measure5.addHairpin(new Hairpin(x1, x4, HairpinType.DECRESCENDO, HairpinPlacement.ABOVE));
        score.addMeasure(measure5);

        score.validate();
        return score;
    }

    public static void main(String[] args) {
        launch(args);
    }
}