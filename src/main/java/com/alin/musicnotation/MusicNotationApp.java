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
        Score score = new Score(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);

        // ============================================================
        // Măsura 1 – Accidentale (diez, bemol, becar) + Slur + Dinamică p
        // ============================================================
        Measure m1 = new Measure();
        m1.addTempo(new Tempo("Allegro", 120));
        m1.addTextExpression(new TextExpression("dolce", TextPlacement.ABOVE));

        Note n1 = new Note(Pitch.of("C#4"), Duration.QUARTER);
        Note n2 = new Note(Pitch.of("Db4"), Duration.QUARTER);
        Note n3 = new Note(Pitch.of("En4"), Duration.QUARTER);
        Note n4 = new Note(Pitch.of("F4"), Duration.QUARTER);
        m1.addNote(n1);
        m1.addNote(n2);
        m1.addNote(n3);
        m1.addNote(n4);
        m1.addSlur(new Slur(List.of(n1, n2, n3, n4)));
        m1.addDynamic(new Dynamic(n1, DynamicType.P));
        score.addMeasure(m1);

        // ============================================================
        // Măsura 2 – Becar + Tie + Articulație Accent
        // ============================================================
        Measure m2 = new Measure();
        m2.addTextExpression(new TextExpression("cantabile", TextPlacement.BELOW));

        Note n5 = new Note(Pitch.of("Fn4"), Duration.QUARTER);   // Fa natural
        Note n6 = new Note(Pitch.of("G4"), Duration.QUARTER);
        Note n7 = new Note(Pitch.of("A4"), Duration.QUARTER, true, false);
        Note n8 = new Note(Pitch.of("A4"), Duration.QUARTER, false, true);
        m2.addNote(n5);
        m2.addNote(n6);
        m2.addNote(n7);
        m2.addNote(n8);
        m2.addArticulation(new Articulation(n8, ArticulationType.ACCENT));
        m2.addDynamic(new Dynamic(n5, DynamicType.MP));
        score.addMeasure(m2);

        // ============================================================
        // Măsura 3 – Hairpin (crescendo) + Slur + Articulație Staccato
        // ============================================================
        Measure m3 = new Measure();
        m3.addTimeSignature(TimeSignature.THREE_FOUR);
        m3.addTextExpression(new TextExpression("cresc.", TextPlacement.ABOVE));

        Note n9 = new Note(Pitch.of("C4"), Duration.QUARTER);
        Note n10 = new Note(Pitch.of("D4"), Duration.QUARTER);
        Note n11 = new Note(Pitch.of("E4"), Duration.QUARTER);
        m3.addNote(n9);
        m3.addNote(n10);
        m3.addNote(n11);
        m3.addSlur(new Slur(List.of(n9, n10, n11)));
        m3.addArticulation(new Articulation(n9, ArticulationType.STACCATO));
        m3.addArticulation(new Articulation(n11, ArticulationType.STACCATO));
        m3.addDynamic(new Dynamic(n9, DynamicType.P));
        m3.addHairpin(new Hairpin(n9, n11, HairpinType.CRESCENDO, HairpinPlacement.BELOW));
        score.addMeasure(m3);

        // ============================================================
        // Măsura 4 – Hairpin (decrescendo) + Dinamică f + Articulație Tenuto
        // ============================================================
        Measure m4 = new Measure();
        m4.addTimeSignature(TimeSignature.COMMON_TIME);
        m4.addTextExpression(new TextExpression("decresc.", TextPlacement.BELOW));

        Note n12 = new Note(Pitch.of("G4"), Duration.QUARTER);
        Note n13 = new Note(Pitch.of("A4"), Duration.QUARTER);
        Note n14 = new Note(Pitch.of("B4"), Duration.QUARTER);
        Note n15 = new Note(Pitch.of("C5"), Duration.QUARTER);
        m4.addNote(n12);
        m4.addNote(n13);
        m4.addNote(n14);
        m4.addNote(n15);
        m4.addArticulation(new Articulation(n12, ArticulationType.MARCATO));
        m4.addArticulation(new Articulation(n15, ArticulationType.FERMATA));
        m4.addDynamic(new Dynamic(n15, DynamicType.F));
        m4.addHairpin(new Hairpin(n12, n15, HairpinType.DECRESCENDO, HairpinPlacement.ABOVE));
        score.addMeasure(m4);

        // ============================================================
        // Măsura 5 – Dinamică mf + Articulație Marcato + Slur
        // ============================================================
        Measure m5 = new Measure();
        m5.addKeySignature(KeySignature.G_MAJOR);
        m5.addTextExpression(new TextExpression("espressivo", TextPlacement.ABOVE));

        Note n16 = new Note(Pitch.of("G4"), Duration.QUARTER);
        Note n17 = new Note(Pitch.of("A4"), Duration.QUARTER);
        Note n18 = new Note(Pitch.of("B4"), Duration.QUARTER);
        Note n19 = new Note(Pitch.of("C5"), Duration.QUARTER);
        m5.addNote(n16);
        m5.addNote(n17);
        m5.addNote(n18);
        m5.addNote(n19);
        m5.addSlur(new Slur(List.of(n16, n17, n18, n19)));
        m5.addArticulation(new Articulation(n16, ArticulationType.MARCATO));
        m5.addArticulation(new Articulation(n19, ArticulationType.MARCATO));
        m5.addDynamic(new Dynamic(n16, DynamicType.MF));
        score.addMeasure(m5);

        // ============================================================
        // Măsura 6 – Dinamică pp + Articulație Fermata + Tie
        // ============================================================
        Measure m6 = new Measure();
        m6.addTempo(Tempo.ofBpm(72));
        m6.addTextExpression(new TextExpression("adagio", TextPlacement.ABOVE));

        Note n20 = new Note(Pitch.of("E4"), Duration.HALF);
        Note n21 = new Note(Pitch.of("E4"), Duration.QUARTER, true, false);
        Note n22 = new Note(Pitch.of("E4"), Duration.QUARTER, false, true);
        m6.addNote(n20);
        m6.addNote(n21);
        m6.addNote(n22);
        m6.addArticulation(new Articulation(n20, ArticulationType.FERMATA));
        m6.addDynamic(new Dynamic(n20, DynamicType.PP));
        score.addMeasure(m6);

        // ============================================================
        // Măsura 7 – Dinamică ff + Articulație Staccatissimo
        // ============================================================
        Measure m7 = new Measure();
        m7.addKeySignature(KeySignature.C_MAJOR);
        m7.addTextExpression(new TextExpression("con brio", TextPlacement.ABOVE));

        Note n23 = new Note(Pitch.of("C5"), Duration.QUARTER);
        Note n24 = new Note(Pitch.of("D5"), Duration.QUARTER);
        Note n25 = new Note(Pitch.of("E5"), Duration.QUARTER);
        Note n26 = new Note(Pitch.of("F5"), Duration.QUARTER);
        m7.addNote(n23);
        m7.addNote(n24);
        m7.addNote(n25);
        m7.addNote(n26);
        m7.addArticulation(new Articulation(n23, ArticulationType.STACCATISSIMO));
        m7.addArticulation(new Articulation(n24, ArticulationType.STACCATISSIMO));
        m7.addArticulation(new Articulation(n25, ArticulationType.STACCATISSIMO));
        m7.addArticulation(new Articulation(n26, ArticulationType.STACCATISSIMO));
        m7.addDynamic(new Dynamic(n23, DynamicType.FF));
        score.addMeasure(m7);

        // ============================================================
        // Măsura 8 – Hairpin + Dinamică + Slur + Tie (combinație complexă)
        // ============================================================
        Measure m8 = new Measure();
        m8.addTextExpression(new TextExpression("molto legato", TextPlacement.ABOVE));

        Note n27 = new Note(Pitch.of("G4"), Duration.QUARTER);
        Note n28 = new Note(Pitch.of("A4"), Duration.QUARTER);
        Note n29 = new Note(Pitch.of("B4"), Duration.QUARTER, true, false);
        Note n30 = new Note(Pitch.of("B4"), Duration.QUARTER, false, true);
        m8.addNote(n27);
        m8.addNote(n28);
        m8.addNote(n29);
        m8.addNote(n30);
        m8.addSlur(new Slur(List.of(n27, n28, n29)));
        m8.addDynamic(new Dynamic(n27, DynamicType.MP));
        m8.addHairpin(new Hairpin(n27, n30, HairpinType.CRESCENDO, HairpinPlacement.BELOW));
        score.addMeasure(m8);

        // ============================================================
        // Măsura 9 – Dinamică FFF + Articulație Staccato + Slur
        // ============================================================
        Measure m9 = new Measure();
        m9.addTimeSignature(TimeSignature.COMMON_TIME);
        m9.addTextExpression(new TextExpression("fortissimo", TextPlacement.BELOW));

        Note n31 = new Note(Pitch.of("C5"), Duration.QUARTER);
        Note n32 = new Note(Pitch.of("D5"), Duration.QUARTER);
        Note n33 = new Note(Pitch.of("E5"), Duration.QUARTER);
        Note n34 = new Note(Pitch.of("F5"), Duration.QUARTER);
        m9.addNote(n31);
        m9.addNote(n32);
        m9.addNote(n33);
        m9.addNote(n34);
        m9.addSlur(new Slur(List.of(n31, n32, n33, n34)));
        m9.addArticulation(new Articulation(n31, ArticulationType.STACCATO));
        m9.addArticulation(new Articulation(n34, ArticulationType.STACCATO));
        m9.addDynamic(new Dynamic(n31, DynamicType.FFF));
        m9.addHairpin(new Hairpin(n31, n34, HairpinType.DECRESCENDO, HairpinPlacement.ABOVE));
        score.addMeasure(m9);

        // ============================================================
        // Măsura 10 – Final: Notă întreagă cu Fermata + Dinamică ppp + Text "Fine"
        // ============================================================
        Measure m10 = new Measure();
        m10.addKeySignature(KeySignature.C_MAJOR);
        m10.addTextExpression(new TextExpression("Fine", TextPlacement.ABOVE));

        Note n35 = new Note(Pitch.of("C4"), Duration.WHOLE);
        m10.addNote(n35);
        m10.addArticulation(new Articulation(n35, ArticulationType.FERMATA));
        m10.addDynamic(new Dynamic(n35, DynamicType.PPP));
        m10.addEndRepeat();
        score.addMeasure(m10);

        // Validăm score-ul
        score.validate();
        return score;
    }

    public static void main(String[] args) {
        launch(args);
    }
}