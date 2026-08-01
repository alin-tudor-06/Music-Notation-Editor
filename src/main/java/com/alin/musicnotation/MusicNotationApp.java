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
        Score score = new Score(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);

        // ============================================================
        // Măsura 1 – optimi izolate, cu tija în sus și în jos
        // Note: C4, E4 (sus), G4, B4 (jos)
        // Fiecare notă este urmată de un rest de optime pentru a le izola.
        // ============================================================
        Measure m1 = new Measure();
        m1.addNote(new Note(Pitch.of("C4"), Duration.QUARTER));
        m1.addNote(new Note(Pitch.of("E4"), Duration.QUARTER));
        m1.addNote(new Note(Pitch.of("G4"), Duration.QUARTER));
        m1.addNote(new Note(Pitch.of("B4"), Duration.QUARTER));
        score.addMeasure(m1);

        // ============================================================
        // Măsura 2 – șaisprezecimi izolate, toate cu tija în sus (note joase)
        // Note: C4, D4, E4, F4, G4, A4, B4, C5 (toate sub linia de mijloc)
        // Fiecare notă este urmată de un rest de șaisprezecime.
        // ============================================================
        Measure m2 = new Measure();
        m2.addNote(new Note(Pitch.of("C4"), Duration.SIXTEENTH));
        m2.addNote(new Note(Duration.SIXTEENTH));
        m2.addNote(new Note(Pitch.of("D4"), Duration.SIXTEENTH));
        m2.addNote(new Note(Duration.SIXTEENTH));
        m2.addNote(new Note(Pitch.of("E4"), Duration.SIXTEENTH));
        m2.addNote(new Note(Duration.SIXTEENTH));
        m2.addNote(new Note(Pitch.of("F4"), Duration.SIXTEENTH));
        m2.addNote(new Note(Duration.SIXTEENTH));
        m2.addNote(new Note(Pitch.of("G4"), Duration.SIXTEENTH));
        m2.addNote(new Note(Duration.SIXTEENTH));
        m2.addNote(new Note(Pitch.of("A4"), Duration.SIXTEENTH));
        m2.addNote(new Note(Duration.SIXTEENTH));
        m2.addNote(new Note(Pitch.of("B4"), Duration.SIXTEENTH));
        m2.addNote(new Note(Duration.SIXTEENTH));
        m2.addNote(new Note(Pitch.of("C5"), Duration.SIXTEENTH));
        m2.addNote(new Note(Duration.SIXTEENTH));
        score.addMeasure(m2);

        // ============================================================
        // Măsura 3 – șaisprezecimi izolate, toate cu tija în jos (note înalte)
        // Note: G4, A4, B4, C5, D5, E5, F5, G5
        // Fiecare notă este urmată de un rest de șaisprezecime.
        // ============================================================
        Measure m3 = new Measure();
        m3.addNote(new Note(Pitch.of("G4"), Duration.SIXTEENTH));
        m3.addNote(new Note(Duration.SIXTEENTH));
        m3.addNote(new Note(Pitch.of("A4"), Duration.SIXTEENTH));
        m3.addNote(new Note(Duration.SIXTEENTH));
        m3.addNote(new Note(Pitch.of("B4"), Duration.SIXTEENTH));
        m3.addNote(new Note(Duration.SIXTEENTH));
        m3.addNote(new Note(Pitch.of("C5"), Duration.SIXTEENTH));
        m3.addNote(new Note(Duration.SIXTEENTH));
        m3.addNote(new Note(Pitch.of("D5"), Duration.SIXTEENTH));
        m3.addNote(new Note(Duration.SIXTEENTH));
        m3.addNote(new Note(Pitch.of("E5"), Duration.SIXTEENTH));
        m3.addNote(new Note(Duration.SIXTEENTH));
        m3.addNote(new Note(Pitch.of("F5"), Duration.SIXTEENTH));
        m3.addNote(new Note(Duration.SIXTEENTH));
        m3.addNote(new Note(Pitch.of("G5"), Duration.SIXTEENTH));
        m3.addNote(new Note(Duration.SIXTEENTH));
        score.addMeasure(m3);

        // ============================================================
        // Măsura 4 – optimi izolate, toate cu tija în jos (note înalte)
        // Note: C5, D5, E5, F5 (toate deasupra liniei de mijloc)
        // Fiecare notă este urmată de un rest de optime.
        // ============================================================
        Measure m4 = new Measure();
        m4.addNote(new Note(Pitch.of("C5"), Duration.EIGHTH));
        m4.addNote(new Note(Duration.EIGHTH));
        m4.addNote(new Note(Pitch.of("D5"), Duration.EIGHTH));
        m4.addNote(new Note(Duration.EIGHTH));
        m4.addNote(new Note(Pitch.of("E5"), Duration.EIGHTH));
        m4.addNote(new Note(Duration.EIGHTH));
        m4.addNote(new Note(Pitch.of("F5"), Duration.EIGHTH));
        m4.addNote(new Note(Duration.EIGHTH));
        score.addMeasure(m4);

        // Validăm score-ul
        score.validate();
        return score;
    }

    public static void main(String[] args) {
        launch(args);
    }
}