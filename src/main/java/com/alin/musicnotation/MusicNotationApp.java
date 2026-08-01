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
        // Măsura 1 – PPP și PP
        // ============================================================
        Measure m1 = new Measure();
        Note n1 = new Note(Pitch.of("C4"), Duration.QUARTER);
        Note n2 = new Note(Pitch.of("E4"), Duration.QUARTER);
        Note n3 = new Note(Pitch.of("G4"), Duration.QUARTER);
        Note n4 = new Note(Pitch.of("B4"), Duration.QUARTER);
        m1.addNote(n1);
        m1.addNote(n2);
        m1.addNote(n3);
        m1.addNote(n4);
        m1.addDynamic(new Dynamic(n1, DynamicType.PPP));
        m1.addDynamic(new Dynamic(n2, DynamicType.PP));
        m1.addDynamic(new Dynamic(n3, DynamicType.PPP));
        m1.addDynamic(new Dynamic(n4, DynamicType.PP));
        // Adăugăm text pentru identificare
        m1.addArticulation(new Articulation(n1,ArticulationType.STACCATISSIMO,ArticulationPlacement.ABOVE));
        score.addMeasure(m1);

        // ============================================================
        // Măsura 2 – P și MP
        // ============================================================
        Measure m2 = new Measure();
        Note n5 = new Note(Pitch.of("C4"), Duration.QUARTER);
        Note n6 = new Note(Pitch.of("E4"), Duration.QUARTER);
        Note n7 = new Note(Pitch.of("G4"), Duration.QUARTER);
        Note n8 = new Note(Pitch.of("B4"), Duration.QUARTER);
        m2.addNote(n5);
        m2.addNote(n6);
        m2.addNote(n7);
        m2.addNote(n8);
        m2.addDynamic(new Dynamic(n5, DynamicType.P));
        m2.addDynamic(new Dynamic(n6, DynamicType.MP));
        m2.addDynamic(new Dynamic(n7, DynamicType.P));
        m2.addDynamic(new Dynamic(n8, DynamicType.MP));
        m2.addTextExpression(new TextExpression("P", TextPlacement.ABOVE));
        m2.addTextExpression(new TextExpression("MP", TextPlacement.BELOW));
        score.addMeasure(m2);

        // ============================================================
        // Măsura 3 – MF și F
        // ============================================================
        Measure m3 = new Measure();
        Note n9 = new Note(Pitch.of("C5"), Duration.QUARTER);
        Note n10 = new Note(Pitch.of("E5"), Duration.QUARTER);
        Note n11 = new Note(Pitch.of("G5"), Duration.QUARTER);
        Note n12 = new Note(Pitch.of("B5"), Duration.QUARTER);
        m3.addNote(n9);
        m3.addNote(n10);
        m3.addNote(n11);
        m3.addNote(n12);
        m3.addDynamic(new Dynamic(n9, DynamicType.MF));
        m3.addDynamic(new Dynamic(n10, DynamicType.F));
        m3.addDynamic(new Dynamic(n11, DynamicType.MF));
        m3.addDynamic(new Dynamic(n12, DynamicType.F));
        m3.addTextExpression(new TextExpression("MF", TextPlacement.ABOVE));
        m3.addTextExpression(new TextExpression("F", TextPlacement.BELOW));
        score.addMeasure(m3);

        // ============================================================
        // Măsura 4 – FF și FFF
        // ============================================================
        Measure m4 = new Measure();
        Note n13 = new Note(Pitch.of("C4"), Duration.QUARTER);
        Note n14 = new Note(Pitch.of("E4"), Duration.QUARTER);
        Note n15 = new Note(Pitch.of("G4"), Duration.QUARTER);
        Note n16 = new Note(Pitch.of("B4"), Duration.QUARTER);
        m4.addNote(n13);
        m4.addNote(n14);
        m4.addNote(n15);
        m4.addNote(n16);
        m4.addDynamic(new Dynamic(n13, DynamicType.FF));
        m4.addDynamic(new Dynamic(n14, DynamicType.FFF));
        m4.addDynamic(new Dynamic(n15, DynamicType.FF));
        m4.addDynamic(new Dynamic(n16, DynamicType.FFF));
        m4.addTextExpression(new TextExpression("FF", TextPlacement.ABOVE));
        m4.addTextExpression(new TextExpression("FFF", TextPlacement.BELOW));
        score.addMeasure(m4);

        // ============================================================
        // Măsura 5 – Dinamici cu placement explicit ABOVE și BELOW
        // ============================================================
        Measure m5 = new Measure();
        Note n17 = new Note(Pitch.of("C4"), Duration.QUARTER);
        Note n18 = new Note(Pitch.of("E4"), Duration.QUARTER);
        Note n19 = new Note(Pitch.of("G4"), Duration.QUARTER);
        Note n20 = new Note(Pitch.of("B4"), Duration.QUARTER);
        m5.addNote(n17);
        m5.addNote(n18);
        m5.addNote(n19);
        m5.addNote(n20);
        m5.addDynamic(new Dynamic(n17, DynamicType.P, DynamicPlacement.ABOVE));
        m5.addDynamic(new Dynamic(n18, DynamicType.P, DynamicPlacement.BELOW));
        m5.addDynamic(new Dynamic(n19, DynamicType.F, DynamicPlacement.ABOVE));
        m5.addDynamic(new Dynamic(n20, DynamicType.F, DynamicPlacement.BELOW));
        m5.addTextExpression(new TextExpression("P (ABOVE)", TextPlacement.ABOVE));
        m5.addTextExpression(new TextExpression("P (BELOW)", TextPlacement.BELOW));
        score.addMeasure(m5);

        // ============================================================
        // Măsura 6 – Secvență crescătoare: P → MF → F → FF
        // ============================================================
        Measure m6 = new Measure();
        Note n21 = new Note(Pitch.of("C4"), Duration.QUARTER);
        Note n22 = new Note(Pitch.of("E4"), Duration.QUARTER);
        Note n23 = new Note(Pitch.of("G4"), Duration.QUARTER);
        Note n24 = new Note(Pitch.of("B4"), Duration.QUARTER);
        m6.addNote(n21);
        m6.addNote(n22);
        m6.addNote(n23);
        m6.addNote(n24);
        m6.addDynamic(new Dynamic(n21, DynamicType.P));
        m6.addDynamic(new Dynamic(n22, DynamicType.MF));
        m6.addDynamic(new Dynamic(n23, DynamicType.F));
        m6.addDynamic(new Dynamic(n24, DynamicType.FF));
        m6.addTextExpression(new TextExpression("Crescendo", TextPlacement.ABOVE));
        score.addMeasure(m6);

        // ============================================================
        // Măsura 7 – Secvență descrescătoare: FF → F → MF → P
        // ============================================================
        Measure m7 = new Measure();
        Note n25 = new Note(Pitch.of("C5"), Duration.QUARTER);
        Note n26 = new Note(Pitch.of("E5"), Duration.QUARTER);
        Note n27 = new Note(Pitch.of("G5"), Duration.QUARTER);
        Note n28 = new Note(Pitch.of("B5"), Duration.QUARTER);
        m7.addNote(n25);
        m7.addNote(n26);
        m7.addNote(n27);
        m7.addNote(n28);
        m7.addDynamic(new Dynamic(n25, DynamicType.FF));
        m7.addDynamic(new Dynamic(n26, DynamicType.F));
        m7.addDynamic(new Dynamic(n27, DynamicType.MF));
        m7.addDynamic(new Dynamic(n28, DynamicType.P));
        m7.addTextExpression(new TextExpression("Decrescendo", TextPlacement.ABOVE));
        score.addMeasure(m7);

        // ============================================================
        // Măsura 8 – Dinamici pe note cu durate diferite
        // ============================================================
        Measure m8 = new Measure();
        Note n29 = new Note(Pitch.of("C4"), Duration.WHOLE);
        m8.addNote(n29);
        m8.addDynamic(new Dynamic(n29, DynamicType.PPP));
        m8.addTextExpression(new TextExpression("PPP (whole note)", TextPlacement.ABOVE));
        score.addMeasure(m8);

        Measure m9 = new Measure();
        Note n30 = new Note(Pitch.of("E4"), Duration.HALF);
        Note n31 = new Note(Pitch.of("G4"), Duration.HALF);
        m9.addNote(n30);
        m9.addNote(n31);
        m9.addDynamic(new Dynamic(n30, DynamicType.FF));
        m9.addDynamic(new Dynamic(n31, DynamicType.PP));
        m9.addTextExpression(new TextExpression("FF / PP", TextPlacement.ABOVE));
        score.addMeasure(m9);

        // Validăm score-ul
        score.validate();
        return score;
    }

    public static void main(String[] args) {
        launch(args);
    }
}