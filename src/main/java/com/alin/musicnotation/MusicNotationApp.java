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
import com.alin.musicnotation.smufl.BravuraFont;
import javafx.scene.text.Font;
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
    private static final double STAFF_MARGIN = 80;
    private static final double NOTE_SPACING_PER_QUARTER = 120;
    private static final double SYSTEM_SPACING = STAFF_LINE_SPACING * 14;
    private static final double TOP_MARGIN = 140;

    private final Score score = createSampleScore();
    private final RenderConfig config = RenderConfig.defaultConfig();
    private final MeasureSpacingEngine spacingEngine = new MeasureSpacingEngine(NOTE_SPACING_PER_QUARTER);
    private final PageLayoutEngine pageLayoutEngine = new PageLayoutEngine(
            WINDOW_WIDTH, STAFF_MARGIN, STAFF_MARGIN, TOP_MARGIN, SYSTEM_SPACING
    );
    private final ScoreLayoutEngine layoutEngine = new ScoreLayoutEngine(
            spacingEngine,
            pageLayoutEngine,
            config
    );

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
        MeasureRenderer measureRenderer = new MeasureRenderer(config, measurePainter);
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

        // === TEST: Încărcare Bravura font ===
        Font bravura = BravuraFont.getFont();
        System.out.println("Bravura font family: " + bravura.getFamily());
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
        // Score-ul începe în Do major, cheia sol, 4/4
        Score score = new Score(KeySignature.G_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);

        // ============================================================
        // Măsura 1 – 4/4 (common time) – implicit, dar îl setăm explicit
        // ============================================================
        Measure m1 = new Measure();
        m1.addTimeSignature(TimeSignature.COMMON_TIME);
        m1.addNote(new Note(Pitch.of("C4"), Duration.QUARTER));
        m1.addNote(new Note(Pitch.of("D4"), Duration.QUARTER));
        m1.addNote(new Note(Pitch.of("E4"), Duration.QUARTER));
        m1.addNote(new Note(Pitch.of("F4"), Duration.QUARTER));
        score.addMeasure(m1);

        // ============================================================
        // Măsura 2 – 3/4
        // ============================================================
        Measure m2 = new Measure();
        m2.addTimeSignature(TimeSignature.THREE_FOUR);
        m2.addNote(new Note(Pitch.of("G4"), Duration.QUARTER));
        m2.addNote(new Note(Pitch.of("A4"), Duration.QUARTER));
        m2.addNote(new Note(Pitch.of("B4"), Duration.QUARTER));
        score.addMeasure(m2);

        // ============================================================
        // Măsura 3 – 6/8 (umplem cu 6 optimi)
        // ============================================================
        Measure m3 = new Measure();
        m3.addTimeSignature(TimeSignature.SIX_EIGHT);
        for (int i = 0; i < 6; i++) {
            m3.addNote(new Note(Pitch.of("C5"), Duration.EIGHTH));
        }
        score.addMeasure(m3);

        // ============================================================
        // Măsura 4 – 9/8 (9 optimi)
        // ============================================================
        Measure m4 = new Measure();
        m4.addClef(ClefType.BASS);
        m4.addKeySignature(KeySignature.F_MAJOR);
        m4.addTimeSignature(TimeSignature.NINE_EIGHT);
        for (int i = 0; i < 9; i++) {
            m4.addNote(new Note(Pitch.of("C3"), Duration.EIGHTH));
        }
        score.addMeasure(m4);

        // ============================================================
        // Măsura 5 – 12/8 (12 optimi – test pentru numere cu două cifre)
        // ============================================================
        Measure m5 = new Measure();
        m5.addTimeSignature(TimeSignature.TWELVE_EIGHT);
        for (int i = 0; i < 12; i++) {
            m5.addNote(new Note(Pitch.of("D4"), Duration.EIGHTH));
        }
        score.addMeasure(m5);

        // ============================================================
        // Măsura 6 – 2/2 (cut time)
        // ============================================================
        Measure m6 = new Measure();
        m6.addTimeSignature(TimeSignature.CUT_TIME);
        m6.addNote(new Note(Pitch.of("F4"), Duration.HALF));
        m6.addNote(new Note(Pitch.of("G4"), Duration.HALF));
        score.addMeasure(m6);

        // ============================================================
        // Măsura 7 – 2/4
        // ============================================================
        Measure m7 = new Measure();
        m7.addTimeSignature(TimeSignature.TWO_FOUR);
        m7.addNote(new Note(Pitch.of("A4"), Duration.QUARTER));
        m7.addNote(new Note(Pitch.of("B4"), Duration.QUARTER));
        score.addMeasure(m7);

        // ============================================================
        // Măsura 8 – 5/4
        // ============================================================
        Measure m8 = new Measure();
        m8.addTimeSignature(new TimeSignature(5, 4));
        for (int i = 0; i < 5; i++) {
            m8.addNote(new Note(Pitch.of("C5"), Duration.QUARTER));
        }
        score.addMeasure(m8);

        // ============================================================
        // Măsura 9 – revenire la 4/4 (common time)
        // ============================================================
        Measure m9 = new Measure();
        m9.addTimeSignature(TimeSignature.COMMON_TIME);
        m9.addNote(new Note(Pitch.of("D5"), Duration.QUARTER));
        m9.addNote(new Note(Pitch.of("E5"), Duration.QUARTER));
        m9.addNote(new Note(Pitch.of("F5"), Duration.QUARTER));
        m9.addNote(new Note(Pitch.of("G5"), Duration.QUARTER));
        score.addMeasure(m9);

        // ============================================================
        // Măsura 10 – 3/8 (trei optimi)
        // ============================================================
        Measure m10 = new Measure();
        m10.addTimeSignature(new TimeSignature(3, 8));
        for (int i = 0; i < 3; i++) {
            m10.addNote(new Note(Pitch.of("A5"), Duration.EIGHTH));
        }
        score.addMeasure(m10);

        // Validăm score-ul
        score.validate();
        return score;
    }

    public static void main(String[] args) {
        launch(args);
    }
}