package com.alin.musicnotation;

import com.alin.musicnotation.layout.PageLayoutEngine;
import com.alin.musicnotation.layout.ScoreLayoutEngine;
import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.clef.ClefType;
import com.alin.musicnotation.model.articulation.*;
import com.alin.musicnotation.model.dynamic.*;
import com.alin.musicnotation.model.hairpin.*;
import com.alin.musicnotation.model.tempo.*;
import com.alin.musicnotation.model.text.*;
import com.alin.musicnotation.renderer.*;
import com.alin.musicnotation.layout.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.*;

public class MusicNotationApp extends Application {

    private static final double WINDOW_WIDTH = 800;
    private static final double WINDOW_HEIGHT = 1000;
    private static final double STAFF_LINE_SPACING = 16;
    private static final double STAFF_MARGIN = 60;
    private static final double NOTE_SPACING_PER_QUARTER = 90;
    private static final double SYSTEM_SPACING = STAFF_LINE_SPACING * 14;
    private static final double TOP_MARGIN = 130;

    private final Score score = createSampleScore();

    private final RenderConfig config = RenderConfig.defaultConfig();
    private final MeasureSpacingEngine spacingEngine = new MeasureSpacingEngine(NOTE_SPACING_PER_QUARTER);
    private final PageLayoutEngine pageLayoutEngine = new PageLayoutEngine(
            WINDOW_WIDTH, STAFF_MARGIN, STAFF_MARGIN, TOP_MARGIN, SYSTEM_SPACING
    );
    private final ScoreLayoutEngine layoutEngine = new ScoreLayoutEngine(spacingEngine, pageLayoutEngine);

    // Renderers
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

    private final LayoutRenderer layoutRenderer;

    public MusicNotationApp() {
        MeasurePainter measurePainter = new MeasurePainter(
                tempoRenderer, textExpressionRenderer, noteRenderer, beamRenderer,
                tupletRenderer, slurRenderer, articulationRenderer, dynamicRenderer,
                hairpinRenderer, repeatRenderer,
                clefRenderer, keySignatureRenderer, timeSignatureRenderer
        );
        MeasureRenderer measureRenderer = new MeasureRenderer(config, measurePainter, keySignatureRenderer, timeSignatureRenderer);
        SystemRenderer systemRenderer = new SystemRenderer(
                config, measureRenderer, clefRenderer, keySignatureRenderer, timeSignatureRenderer
        );
        layoutRenderer = new LayoutRenderer(systemRenderer);
    }

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
        canvas.widthProperty().addListener((e) -> drawStaff(canvas));
        canvas.heightProperty().addListener((e) -> drawStaff(canvas));

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

        var result = layoutEngine.layout(score, width, STAFF_MARGIN, STAFF_MARGIN);

        RenderingState initialState = new RenderingState(
                score.initialClef(),
                score.keySignature(),
                score.initialTimeSignature()
        );
        layoutRenderer.render(gc, result.pageLayout(), result.renderDataList(), initialState);
    }

    private static Score createSampleScore() {
        // Start with C major, treble clef, 4/4
        Score score = new Score(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);

        // ---------- Measure 1 ----------
        Measure m1 = new Measure();
        m1.addStartRepeat();
        m1.addTempo(new Tempo("Allegro", 120));
        m1.addTextExpression(new TextExpression("dolce", TextPlacement.ABOVE));

        Note c4 = new Note(Pitch.of("C4"), Duration.QUARTER);
        Note d4 = new Note(Pitch.of("D4"), Duration.QUARTER);
        Note e4 = new Note(Pitch.of("E4"), Duration.QUARTER);
        Note f4 = new Note(Pitch.of("F4"), Duration.QUARTER);
        m1.addNote(c4);
        m1.addNote(d4);
        m1.addNote(e4);
        m1.addNote(f4);
        m1.addSlur(new Slur(List.of(c4, d4, e4, f4)));          // slur over four notes
        m1.addDynamic(new Dynamic(c4, DynamicType.P));
        score.addMeasure(m1);

        // ---------- Measure 2 ----------
        Measure m2 = new Measure();
        m2.addTextExpression(new TextExpression("cantabile", TextPlacement.BELOW));

        Note g4 = new Note(Pitch.of("G4"), Duration.SIXTEENTH);
        Note a4 = new Note(Pitch.of("A4"), Duration.QUARTER);
        Note b4 = new Note(Pitch.of("B4"), Duration.QUARTER);
        Note c5 = new Note(Pitch.of("C5"), Duration.QUARTER);
        m2.addNote(g4);
        m2.addNote(a4);
        m2.addNote(b4);
        m2.addNote(c5);
        m2.addDynamic(new Dynamic(g4, DynamicType.MF));
        m2.addHairpin(new Hairpin(g4, c5, HairpinType.CRESCENDO, HairpinPlacement.BELOW));
        m2.addArticulation(new Articulation(c5, ArticulationType.ACCENT));
        score.addMeasure(m2);

        // ---------- Measure 3 ----------
        Measure m3 = new Measure();
        m3.addTimeSignature(TimeSignature.SIX_EIGHT);
        m3.addKeySignature(KeySignature.G_MAJOR);   // one sharp
        m3.addTextExpression(new TextExpression("espressivo", TextPlacement.ABOVE));

        // Eighth notes that will be beamed automatically (group by beat)
        Note e8_1 = new Note(Pitch.of("E4"), Duration.EIGHTH);
        Note f8_1 = new Note(Pitch.of("F#4"), Duration.EIGHTH);
        Note g8_1 = new Note(Pitch.of("G4"), Duration.EIGHTH);
        Note a8_1 = new Note(Pitch.of("A4"), Duration.EIGHTH);
        Note b8_1 = new Note(Pitch.of("B4"), Duration.EIGHTH);
        Note c8_2 = new Note(Pitch.of("C5"), Duration.EIGHTH);
        m3.addNote(e8_1);
        m3.addNote(f8_1);
        m3.addNote(g8_1);
        m3.addNote(a8_1);
        m3.addNote(b8_1);
        m3.addNote(c8_2);
        // Slur over first four notes
        m3.addSlur(new Slur(List.of(e8_1, f8_1, g8_1, a8_1)));
        m3.addDynamic(new Dynamic(e8_1, DynamicType.F));
        m3.addHairpin(new Hairpin(e8_1, c8_2, HairpinType.DECRESCENDO, HairpinPlacement.ABOVE));
        m3.addEndRepeat();
        score.addMeasure(m3);

        // ---------- Measure 4 ----------
        Measure m4 = new Measure();
        m4.addClef(ClefType.BASS);
        m4.addTimeSignature(TimeSignature.NINE_EIGHT);
        m4.addTextExpression(new TextExpression("simile", TextPlacement.BELOW));

        Note c3 = new Note(Pitch.of("C3"), Duration.QUARTER);
        Note d3 = new Note(Pitch.of("D3"), Duration.QUARTER);
        Note e3 = new Note(Pitch.of("E3"), Duration.QUARTER);
        Note f3 = new Note(Pitch.of("F3"), Duration.QUARTER);
        m4.addNote(c3);
        m4.addNote(d3);
        m4.addNote(e3);
        m4.addNote(f3);
        m4.addTuplet(new Tuplet(3, 2, List.of(c3, d3, e3)));   // triplet
        m4.addArticulation(new Articulation(f3, ArticulationType.STACCATO));
        m4.addDynamic(new Dynamic(c3, DynamicType.MP));
        score.addMeasure(m4);

        // ---------- Measure 5 ----------
        Measure m5 = new Measure();
        m5.addTempo(Tempo.ofBpm(96));
        m5.addTextExpression(new TextExpression("con brio", TextPlacement.BELOW));
        m5.addTextExpression(new TextExpression("sempre", TextPlacement.BELOW));

        // Tied notes
        Note x1 = new Note(Pitch.of("C4"), Duration.SIXTEENTH, true, false);
        Note x2 = new Note(Pitch.of("C4"), Duration.QUARTER, false, true);
        Note x3 = new Note(Pitch.of("E4"), Duration.QUARTER);
        Note x4 = new Note(Pitch.of("G4"), Duration.QUARTER);
        m5.addNote(x1);
        m5.addNote(x2);
        m5.addNote(x3);
        m5.addNote(x4);
        m5.addSlur(new Slur(List.of(x2, x3, x4)));              // slur after tie
        m5.addArticulation(new Articulation(x1, ArticulationType.MARCATO));
        m5.addArticulation(new Articulation(x4, ArticulationType.TENUTO));
        m5.addDynamic(new Dynamic(x1, DynamicType.F));
        m5.addDynamic(new Dynamic(x4, DynamicType.PP));
        m5.addHairpin(new Hairpin(x1, x4, HairpinType.CRESCENDO, HairpinPlacement.BELOW));
        score.addMeasure(m5);

        // ---------- Measure 6 ----------
        Measure m6 = new Measure();
        m6.addClef(ClefType.TREBLE);              // back to treble
        m6.addKeySignature(KeySignature.C_MAJOR); // back to C major
        m6.addTimeSignature(TimeSignature.THREE_FOUR);

        // Sixteenth notes with beams and flags
        Note s1 = new Note(Pitch.of("C5"), Duration.SIXTEENTH);
        Note s2 = new Note(Pitch.of("D5"), Duration.SIXTEENTH);
        Note s3 = new Note(Pitch.of("E5"), Duration.SIXTEENTH);
        Note s4 = new Note(Pitch.of("F5"), Duration.SIXTEENTH);
        m6.addNote(s1);
        m6.addNote(s2);
        m6.addNote(s3);
        m6.addNote(s4);
        m6.addTuplet(new Tuplet(5, 4, List.of(s1, s2, s3, s4))); // quintuplet
        m6.addArticulation(new Articulation(s1, ArticulationType.STACCATISSIMO));
        m6.addDynamic(new Dynamic(s1, DynamicType.PPP));
        score.addMeasure(m6);

        // ---------- Measure 7 (final) ----------
        Measure m7 = new Measure();
        m7.addTimeSignature(TimeSignature.COMMON_TIME);
        m7.addKeySignature(KeySignature.F_MAJOR);   // one flat

        // Whole note with fermata
        Note whole = new Note(Pitch.of("C4"), Duration.WHOLE);
        m7.addNote(whole);
        m7.addArticulation(new Articulation(whole, ArticulationType.FERMATA));
        m7.addDynamic(new Dynamic(whole, DynamicType.FFF));
        m7.addTextExpression(new TextExpression("Fine", TextPlacement.ABOVE));
        m7.addEndRepeat();
        score.addMeasure(m7);

        // Validate the entire score
        score.validate();
        return score;
    }

    public static void main(String[] args) {
        launch(args);
    }
}