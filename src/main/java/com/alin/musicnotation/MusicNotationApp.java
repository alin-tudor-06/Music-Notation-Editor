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
    private static final double WINDOW_HEIGHT = 600;
    private static final double STAFF_LINE_SPACING = 16;
    private static final double STAFF_MARGIN = 60;
    private static final double NOTE_SPACING_PER_QUARTER = 60;

    private final Score score = createSampleScore();

    private final RenderConfig config = RenderConfig.defaultConfig();
    private final MeasureSpacingEngine spacingEngine = new MeasureSpacingEngine(NOTE_SPACING_PER_QUARTER);
    private final PageLayoutEngine pageLayoutEngine = new PageLayoutEngine(
            WINDOW_WIDTH, STAFF_MARGIN, STAFF_MARGIN, 50, STAFF_LINE_SPACING * 4
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

    private LayoutRenderer layoutRenderer;

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

        // Build renderer pipeline
        MeasurePainter measurePainter = new MeasurePainter(
                tempoRenderer, textExpressionRenderer, noteRenderer, beamRenderer,
                tupletRenderer, slurRenderer, articulationRenderer, dynamicRenderer,
                hairpinRenderer, repeatRenderer
        );
        MeasureRenderer measureRenderer = new MeasureRenderer(config, measurePainter);
        SystemRenderer systemRenderer = new SystemRenderer(
                config, measureRenderer, clefRenderer, keySignatureRenderer, timeSignatureRenderer
        );
        layoutRenderer = new LayoutRenderer(systemRenderer);
    }

    private void drawStaff(Canvas canvas) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        // Layout pipeline
        var result = layoutEngine.layout(score, width, STAFF_MARGIN, STAFF_MARGIN);

        // Render
        RenderingState initialState = new RenderingState(
                score.initialClef(),
                score.keySignature(),
                score.initialTimeSignature()
        );
        layoutRenderer.render(gc, result.pageLayout(), result.renderDataList(), initialState);
    }

    private static Score createSampleScore() {
        Score score = new Score(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);

        // Measure 1
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

        // Measure 2
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

        // Measure 3
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

        // Measure 4
        Measure measure4 = new Measure();
        measure4.addClef(ClefType.BASS);
        measure4.addTimeSignature(TimeSignature.NINE_EIGHT);
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

        // Measure 5
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

        Measure measure6 = new Measure();
        measure6.addClef(ClefType.BASS);
        measure6.addTimeSignature(TimeSignature.NINE_EIGHT);
        measure6.addTextExpression(new TextExpression("simile", TextPlacement.BELOW));

        Note a1 = new Note(Pitch.of("C3"), Duration.QUARTER);
        Note a2 = new Note(Pitch.of("D3"), Duration.QUARTER);
        Note a3 = new Note(Pitch.of("E3"), Duration.QUARTER);
        Note a5 = new Note(Pitch.of("F3"), Duration.QUARTER);
        measure6.addNote(a1);
        measure6.addNote(a2);
        measure6.addNote(a3);
        measure6.addNote(a5);
        measure6.addDynamic(new Dynamic(a1, DynamicType.MF));
        score.addMeasure(measure6);


        Measure measure7 = new Measure();
        measure7.addClef(ClefType.BASS);
        measure7.addTimeSignature(TimeSignature.NINE_EIGHT);
        measure7.addTextExpression(new TextExpression("simile", TextPlacement.BELOW));

        Note h1 = new Note(Pitch.of("C3"), Duration.QUARTER);
        Note h2 = new Note(Pitch.of("D3"), Duration.QUARTER);
        Note h3 = new Note(Pitch.of("E3"), Duration.QUARTER);
        Note h4 = new Note(Pitch.of("F3"), Duration.QUARTER);
        measure7.addNote(h1);
        measure7.addNote(h2);
        measure7.addNote(h3);
        measure7.addNote(h4);
        measure7.addDynamic(new Dynamic(h1, DynamicType.MF));
        score.addMeasure(measure7);


        Measure measure8 = new Measure();
        measure8.addClef(ClefType.BASS);
        measure8.addTimeSignature(TimeSignature.NINE_EIGHT);
        measure8.addTextExpression(new TextExpression("simile", TextPlacement.BELOW));

        Note y1 = new Note(Pitch.of("C3"), Duration.QUARTER);
        Note y2 = new Note(Pitch.of("D3"), Duration.QUARTER);
        Note y3 = new Note(Pitch.of("E3"), Duration.QUARTER);
        Note y4 = new Note(Pitch.of("F3"), Duration.QUARTER);
        measure8.addNote(y1);
        measure8.addNote(y2);
        measure8.addNote(y3);
        measure8.addNote(y4);
        measure8.addDynamic(new Dynamic(y1, DynamicType.MF));
        score.addMeasure(measure8);


        Measure measure9 = new Measure();
        measure9.addClef(ClefType.BASS);
        measure9.addTimeSignature(TimeSignature.NINE_EIGHT);
        measure9.addTextExpression(new TextExpression("simile", TextPlacement.BELOW));

        Note g5 = new Note(Pitch.of("C3"), Duration.QUARTER);
        Note g6 = new Note(Pitch.of("D3"), Duration.QUARTER);
        Note g7 = new Note(Pitch.of("E3"), Duration.QUARTER);
        Note g8 = new Note(Pitch.of("F3"), Duration.QUARTER);
        measure9.addNote(g5);
        measure9.addNote(g6);
        measure9.addNote(g7);
        measure9.addNote(g8);
        measure9.addDynamic(new Dynamic(g5, DynamicType.MF));
        score.addMeasure(measure9);


        Measure measure10 = new Measure();
        measure10.addClef(ClefType.BASS);
        measure10.addTimeSignature(TimeSignature.NINE_EIGHT);
        measure10.addTextExpression(new TextExpression("simile", TextPlacement.BELOW));

        Note b5 = new Note(Pitch.of("C3"), Duration.QUARTER);
        Note b6 = new Note(Pitch.of("D3"), Duration.QUARTER);
        Note b7 = new Note(Pitch.of("E3"), Duration.QUARTER);
        Note b8 = new Note(Pitch.of("F3"), Duration.QUARTER);
        measure10.addNote(b5);
        measure10.addNote(b6);
        measure10.addNote(b7);
        measure10.addNote(b8);
        measure10.addDynamic(new Dynamic(b5, DynamicType.MF));
        score.addMeasure(measure10);

        score.validate();
        return score;
    }

    public static void main(String[] args) {
        launch(args);
    }
}