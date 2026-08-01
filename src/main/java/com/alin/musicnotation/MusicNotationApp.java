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
    private static final double NOTE_SPACING_PER_QUARTER = 90;
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
        // Măsura 1 – start repeat, tempo, text, slur, dinamică p
        // ============================================================
        Measure m1 = new Measure();
        m1.addStartRepeat();
        m1.addTempo(new Tempo("Allegro", 120));
        m1.addTextExpression(new TextExpression("dolce", TextPlacement.ABOVE));

        Note c4 = new Note(Pitch.of("C#4"), Duration.QUARTER);
        Note d4 = new Note(Pitch.of("Db4"), Duration.QUARTER);
        Note e4 = new Note(Pitch.of("En4"), Duration.QUARTER);
        Note f4 = new Note(Pitch.of("F4"), Duration.QUARTER);
        m1.addNote(c4);
        m1.addNote(d4);
        m1.addNote(e4);
        m1.addNote(f4);
        m1.addSlur(new Slur(List.of(c4, d4, e4, f4)));
        m1.addDynamic(new Dynamic(c4, DynamicType.P));
        score.addMeasure(m1);

        // ============================================================
        // Măsura 2 – text dedesubt, dinamică mf, hairpin (crescendo), accent
        // ============================================================
        Measure m2 = new Measure();
        m2.addTextExpression(new TextExpression("cantabile", TextPlacement.BELOW));

        Note g4 = new Note(Pitch.of("G4"), Duration.QUARTER);
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

        // ============================================================
        // Măsura 3 – time 6/8, key G major, beaming (optimi), slur, f, decrescendo, end repeat
        // ============================================================
        Measure m3 = new Measure();
        m3.addTimeSignature(TimeSignature.SIX_EIGHT);
        m3.addKeySignature(KeySignature.G_MAJOR);
        m3.addTextExpression(new TextExpression("espressivo", TextPlacement.ABOVE));

        Note e8_1 = new Note(Pitch.of("E4"), Duration.EIGHTH);
        Note f8_1 = new Note(Pitch.of("Fn4"), Duration.EIGHTH);
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
        m3.addSlur(new Slur(List.of(e8_1, f8_1, g8_1, a8_1)));
        m3.addDynamic(new Dynamic(e8_1, DynamicType.F));
        m3.addHairpin(new Hairpin(e8_1, c8_2, HairpinType.DECRESCENDO, HairpinPlacement.ABOVE));
        m3.addEndRepeat();
        score.addMeasure(m3);

        // ============================================================
        // Măsura 4 – clef bass, time 9/8, tuplet (triplet), staccato, mp
        // ============================================================
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
        m4.addTuplet(new Tuplet(3, 2, List.of(c3, d3, e3)));
        m4.addArticulation(new Articulation(f3, ArticulationType.STACCATO));
        m4.addDynamic(new Dynamic(c3, DynamicType.MP));
        score.addMeasure(m4);

        // ============================================================
        // Măsura 5 – tempo BPM, două texte, tie, slur, marcato, tenuto, f, pp, hairpin crescendo
        // ============================================================
        Measure m5 = new Measure();
        m5.addTempo(Tempo.ofBpm(96));
        m5.addTextExpression(new TextExpression("con brio", TextPlacement.ABOVE));
        m5.addTextExpression(new TextExpression("sempre", TextPlacement.BELOW));

        Note x1 = new Note(Pitch.of("C4"), Duration.QUARTER, true, false);
        Note x2 = new Note(Pitch.of("C4"), Duration.QUARTER, false, true);
        Note x3 = new Note(Pitch.of("E4"), Duration.QUARTER);
        Note x4 = new Note(Pitch.of("G4"), Duration.QUARTER);
        m5.addNote(x1);
        m5.addNote(x2);
        m5.addNote(x3);
        m5.addNote(x4);
        m5.addSlur(new Slur(List.of(x2, x3, x4)));
        m5.addArticulation(new Articulation(x1, ArticulationType.MARCATO));
        m5.addArticulation(new Articulation(x4, ArticulationType.TENUTO));
        m5.addDynamic(new Dynamic(x1, DynamicType.F));
        m5.addDynamic(new Dynamic(x4, DynamicType.PP));
        m5.addHairpin(new Hairpin(x1, x4, HairpinType.CRESCENDO, HairpinPlacement.BELOW));
        score.addMeasure(m5);

        // ============================================================
        // Măsura 6 – clef alto, key C major, time 3/4, sixteenths, tuplet 5:4, staccatissimo, ppp
        // ============================================================
        Measure m6 = new Measure();
        m6.addClef(ClefType.ALTO);
        m6.addKeySignature(KeySignature.C_MAJOR);
        m6.addTimeSignature(TimeSignature.THREE_FOUR);

        Note s1 = new Note(Pitch.of("C5"), Duration.SIXTEENTH);
        Note s2 = new Note(Pitch.of("D5"), Duration.SIXTEENTH);
        Note s3 = new Note(Pitch.of("E5"), Duration.SIXTEENTH);
        Note s4 = new Note(Pitch.of("F5"), Duration.SIXTEENTH);
        m6.addNote(s1);
        m6.addNote(s2);
        m6.addNote(s3);
        m6.addNote(s4);
        m6.addTuplet(new Tuplet(5, 4, List.of(s1, s2, s3, s4)));
        m6.addArticulation(new Articulation(s1, ArticulationType.STACCATISSIMO));
        m6.addDynamic(new Dynamic(s1, DynamicType.PPP));
        score.addMeasure(m6);

        // ============================================================
        // Măsura 7 – clef tenor, time 4/4, key F major, whole note with fermata, fff, text "Fine", end repeat
        // ============================================================
        Measure m7 = new Measure();
        m7.addClef(ClefType.TENOR);
        m7.addTimeSignature(TimeSignature.COMMON_TIME);
        m7.addKeySignature(KeySignature.F_MAJOR);

        Note whole = new Note(Pitch.of("C4"), Duration.WHOLE);
        m7.addNote(whole);
        m7.addArticulation(new Articulation(whole, ArticulationType.FERMATA));
        m7.addDynamic(new Dynamic(whole, DynamicType.FFF));
        m7.addTextExpression(new TextExpression("Fine", TextPlacement.ABOVE));
        m7.addEndRepeat();
        score.addMeasure(m7);

        // ============================================================
        // Măsura 8 – WHOLE rest (4/4)
        // ============================================================
        Measure m8 = new Measure();
        Note restWhole = new Note(Duration.WHOLE);
        m8.addNote(restWhole);
        score.addMeasure(m8);

        // ============================================================
        // Măsura 9 – HALF rest + două note pătrimi (pentru a umple 2 bătăi)
        // ============================================================
        Measure m9 = new Measure();
        Note restHalf = new Note(Duration.HALF);
        Note nHalf1 = new Note(Pitch.of("C4"), Duration.QUARTER);
        Note nHalf2 = new Note(Pitch.of("D4"), Duration.QUARTER);
        m9.addNote(restHalf);
        m9.addNote(nHalf1);
        m9.addNote(nHalf2);
        score.addMeasure(m9);

        // ============================================================
        // Măsura 10 – QUARTER rest, notă, quarter rest, notă
        // ============================================================
        Measure m10 = new Measure();
        Note restQ1 = new Note(Duration.QUARTER);
        Note nQ1 = new Note(Pitch.of("E4"), Duration.QUARTER);
        Note restQ2 = new Note(Duration.QUARTER);
        Note nQ2 = new Note(Pitch.of("F4"), Duration.QUARTER);
        m10.addNote(restQ1);
        m10.addNote(nQ1);
        m10.addNote(restQ2);
        m10.addNote(nQ2);
        score.addMeasure(m10);

        // ============================================================
        // Măsura 11 – EIGHTH rest, notă, eighth rest, notă, și încă două optimi
        // ============================================================
        Measure m11 = new Measure();
        Note restE1 = new Note(Duration.EIGHTH);
        Note nE1 = new Note(Pitch.of("G4"), Duration.EIGHTH);
        Note restE2 = new Note(Duration.EIGHTH);
        Note nE2 = new Note(Pitch.of("A4"), Duration.EIGHTH);
        Note nE3 = new Note(Pitch.of("B4"), Duration.EIGHTH);
        Note nE4 = new Note(Pitch.of("C5"), Duration.EIGHTH);
        m11.addNote(restE1);
        m11.addNote(nE1);
        m11.addNote(restE2);
        m11.addNote(nE2);
        m11.addNote(nE3);
        m11.addNote(nE4);
        score.addMeasure(m11);

        // ============================================================
        // Măsura 12 – SIXTEENTH rest + notă, + rest + notă, etc. (umplem 4 bătăi)
        // ============================================================
        Measure m12 = new Measure();
        Note restS1 = new Note(Duration.SIXTEENTH);
        Note nS1 = new Note(Pitch.of("D5"), Duration.SIXTEENTH);
        Note restS2 = new Note(Duration.SIXTEENTH);
        Note nS2 = new Note(Pitch.of("E5"), Duration.SIXTEENTH);
        Note restS3 = new Note(Duration.SIXTEENTH);
        Note nS3 = new Note(Pitch.of("F5"), Duration.SIXTEENTH);
        Note restS4 = new Note(Duration.SIXTEENTH);
        Note nS4 = new Note(Pitch.of("G5"), Duration.SIXTEENTH);
        // Mai adăugăm încă 8 șaisprezecimi (2 bătăi) pentru a umple măsura 4/4
        // Total 16 șaisprezecimi = 4 bătăi
        m12.addNote(restS1);
        m12.addNote(nS1);
        m12.addNote(restS2);
        m12.addNote(nS2);
        m12.addNote(restS3);
        m12.addNote(nS3);
        m12.addNote(restS4);
        m12.addNote(nS4);
        // Încă 8 șaisprezecimi (2 bătăi) – le adăugăm ca note
        for (int i = 0; i < 8; i++) {
            m12.addNote(new Note(Pitch.of("A5"), Duration.SIXTEENTH));
        }
        score.addMeasure(m12);

        // Validăm score-ul
        score.validate();
        return score;
    }

    public static void main(String[] args) {
        launch(args);
    }
}