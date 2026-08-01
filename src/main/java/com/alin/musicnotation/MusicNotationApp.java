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
        // Măsura 1 – Staccato și Staccatissimo (AUTO, tijă sus/jos)
        // ============================================================
        Measure m1 = new Measure();
        Note n1 = new Note(Pitch.of("C4"), Duration.SIXTEENTH);
        Note n2 = new Note(Pitch.of("E4"), Duration.SIXTEENTH);
        Note n3 = new Note(Pitch.of("G4"), Duration.SIXTEENTH);
        Note n4 = new Note(Pitch.of("B4"), Duration.SIXTEENTH);
        m1.addNote(n1);
        m1.addNote(n2);
        m1.addNote(n3);
        m1.addNote(n4);
        m1.addArticulation(new Articulation(n1, ArticulationType.STACCATO));
        m1.addArticulation(new Articulation(n2, ArticulationType.STACCATISSIMO));
        m1.addArticulation(new Articulation(n3, ArticulationType.STACCATO));
        m1.addArticulation(new Articulation(n4, ArticulationType.STACCATISSIMO));
        score.addMeasure(m1);

        // ============================================================
        // Măsura 2 – Accent și Marcato (AUTO, tijă sus/jos)
        // ============================================================
        Measure m2 = new Measure();
        Note n5 = new Note(Pitch.of("C4"), Duration.SIXTEENTH);
        Note n6 = new Note(Pitch.of("E4"), Duration.SIXTEENTH);
        Note n7 = new Note(Pitch.of("G4"), Duration.SIXTEENTH);
        Note n8 = new Note(Pitch.of("B4"), Duration.SIXTEENTH);
        m2.addNote(n5);
        m2.addNote(n6);
        m2.addNote(n7);
        m2.addNote(n8);
        m2.addArticulation(new Articulation(n5, ArticulationType.ACCENT));
        m2.addArticulation(new Articulation(n6, ArticulationType.MARCATO));
        m2.addArticulation(new Articulation(n7, ArticulationType.ACCENT));
        m2.addArticulation(new Articulation(n8, ArticulationType.MARCATO));
        score.addMeasure(m2);

        // ============================================================
        // Măsura 3 – Tenuto (AUTO, tijă sus/jos)
        // ============================================================
        Measure m3 = new Measure();
        Note n9 = new Note(Pitch.of("C5"), Duration.SIXTEENTH);
        Note n10 = new Note(Pitch.of("E5"), Duration.SIXTEENTH);
        Note n11 = new Note(Pitch.of("G5"), Duration.SIXTEENTH);
        Note n12 = new Note(Pitch.of("B5"), Duration.SIXTEENTH);
        m3.addNote(n9);
        m3.addNote(n10);
        m3.addNote(n11);
        m3.addNote(n12);
        m3.addArticulation(new Articulation(n9, ArticulationType.TENUTO));
        m3.addArticulation(new Articulation(n10, ArticulationType.TENUTO));
        m3.addArticulation(new Articulation(n11, ArticulationType.TENUTO));
        m3.addArticulation(new Articulation(n12, ArticulationType.TENUTO));
        score.addMeasure(m3);

        // ============================================================
        // Măsura 4 – Fermata deasupra și dedesubt (explicit)
        // ============================================================
        Measure m4 = new Measure();
        Note n13 = new Note(Pitch.of("C4"), Duration.SIXTEENTH);
        Note n14 = new Note(Pitch.of("E4"), Duration.SIXTEENTH);
        Note n15 = new Note(Pitch.of("G4"), Duration.SIXTEENTH);
        Note n16 = new Note(Pitch.of("B4"), Duration.SIXTEENTH);
        m4.addNote(n13);
        m4.addNote(n14);
        m4.addNote(n15);
        m4.addNote(n16);
        m4.addArticulation(new Articulation(n13, ArticulationType.FERMATA, ArticulationPlacement.ABOVE));
        m4.addArticulation(new Articulation(n14, ArticulationType.FERMATA, ArticulationPlacement.BELOW));
        m4.addArticulation(new Articulation(n15, ArticulationType.FERMATA, ArticulationPlacement.ABOVE));
        m4.addArticulation(new Articulation(n16, ArticulationType.FERMATA, ArticulationPlacement.BELOW));
        score.addMeasure(m4);

        // ============================================================
        // Măsura 5 – Combinații: Staccato + Accent pe aceeași notă (AUTO)
        // ============================================================
        Measure m5 = new Measure();
        Note n17 = new Note(Pitch.of("C4"), Duration.SIXTEENTH);
        Note n18 = new Note(Pitch.of("E4"), Duration.SIXTEENTH);
        Note n19 = new Note(Pitch.of("G4"), Duration.SIXTEENTH);
        Note n20 = new Note(Pitch.of("B4"), Duration.SIXTEENTH);
        m5.addNote(n17);
        m5.addNote(n18);
        m5.addNote(n19);
        m5.addNote(n20);
        m5.addArticulation(new Articulation(n17, ArticulationType.STACCATO));
        m5.addArticulation(new Articulation(n17, ArticulationType.ACCENT));
        m5.addArticulation(new Articulation(n18, ArticulationType.STACCATISSIMO));
        m5.addArticulation(new Articulation(n18, ArticulationType.MARCATO));
        m5.addArticulation(new Articulation(n19, ArticulationType.TENUTO));
        m5.addArticulation(new Articulation(n19, ArticulationType.ACCENT));
        m5.addArticulation(new Articulation(n20, ArticulationType.STACCATO));
        m5.addArticulation(new Articulation(n20, ArticulationType.TENUTO));
        score.addMeasure(m5);

        // ============================================================
        // Măsura 6 – Articulații pe note cu tijă în sus și în jos (test AUTO)
        // Toate notele sunt pătrimi, dar poziționate diferit pe portativ
        // ============================================================
        Measure m6 = new Measure();
        Note n21 = new Note(Pitch.of("C4"), Duration.SIXTEENTH);   // tijă sus
        Note n22 = new Note(Pitch.of("F4"), Duration.SIXTEENTH);   // tijă sus (sub mijloc)
        Note n23 = new Note(Pitch.of("A4"), Duration.SIXTEENTH);   // tijă jos (peste mijloc)
        Note n24 = new Note(Pitch.of("C5"), Duration.SIXTEENTH);   // tijă jos
        m6.addNote(n21);
        m6.addNote(n22);
        m6.addNote(n23);
        m6.addNote(n24);
        m6.addArticulation(new Articulation(n21, ArticulationType.STACCATO));
        m6.addArticulation(new Articulation(n22, ArticulationType.ACCENT));
        m6.addArticulation(new Articulation(n23, ArticulationType.STACCATISSIMO));
        m6.addArticulation(new Articulation(n24, ArticulationType.MARCATO));
        score.addMeasure(m6);

        // ============================================================
        // Măsura 7 – Articulații cu placement explicit ABOVE/BELOW (pentru test)
        // ============================================================
        Measure m7 = new Measure();
        Note n25 = new Note(Pitch.of("C4"), Duration.SIXTEENTH);
        Note n26 = new Note(Pitch.of("E4"), Duration.SIXTEENTH);
        Note n27 = new Note(Pitch.of("G4"), Duration.SIXTEENTH);
        Note n28 = new Note(Pitch.of("B4"), Duration.SIXTEENTH);
        m7.addNote(n25);
        m7.addNote(n26);
        m7.addNote(n27);
        m7.addNote(n28);
        m7.addArticulation(new Articulation(n25, ArticulationType.STACCATO, ArticulationPlacement.ABOVE));
        m7.addArticulation(new Articulation(n26, ArticulationType.STACCATO, ArticulationPlacement.BELOW));
        m7.addArticulation(new Articulation(n27, ArticulationType.ACCENT, ArticulationPlacement.ABOVE));
        m7.addArticulation(new Articulation(n28, ArticulationType.ACCENT, ArticulationPlacement.BELOW));
        score.addMeasure(m7);

        // ============================================================
        // Măsura 8 – Fermata cu AUTO (tijă sus/jos)
        // ============================================================
        Measure m8 = new Measure();
        Note n29 = new Note(Pitch.of("C4"), Duration.SIXTEENTH);
        Note n30 = new Note(Pitch.of("E4"), Duration.SIXTEENTH);
        Note n31 = new Note(Pitch.of("G4"), Duration.SIXTEENTH);
        Note n32 = new Note(Pitch.of("B4"), Duration.SIXTEENTH);
        m8.addNote(n29);
        m8.addNote(n30);
        m8.addNote(n31);
        m8.addNote(n32);
        m8.addArticulation(new Articulation(n29, ArticulationType.FERMATA));
        m8.addArticulation(new Articulation(n30, ArticulationType.FERMATA));
        m8.addArticulation(new Articulation(n31, ArticulationType.FERMATA));
        m8.addArticulation(new Articulation(n32, ArticulationType.FERMATA));
        score.addMeasure(m8);

        // Validăm score-ul
        score.validate();
        return score;
    }

    public static void main(String[] args) {
        launch(args);
    }
}