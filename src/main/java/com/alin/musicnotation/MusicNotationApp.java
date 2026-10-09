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
import com.alin.musicnotation.musicxml.imports.MusicXmlImporter;
import com.alin.musicnotation.playback.MidiPlayer;
import com.alin.musicnotation.playback.MidiSequenceBuilder;
import com.alin.musicnotation.playback.PlaybackCursor;
import com.alin.musicnotation.renderer.*;
import com.alin.musicnotation.layout.*;
import com.alin.musicnotation.smufl.BravuraFont;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MusicNotationApp extends Application {

    private static final double WINDOW_WIDTH = 900;
    private static final double WINDOW_HEIGHT = 1000;
    private static final double STAFF_LINE_SPACING = 16;
    private static final double STAFF_MARGIN = 80;
    private static final double NOTE_SPACING_PER_QUARTER = 120;
    private static final double SYSTEM_SPACING = STAFF_LINE_SPACING * 14;
    private static final double TOP_MARGIN = 140;
    private static final double BOTTOM_MARGIN = 100;

    private static final Path IMPORT_FILE = Paths.get(
            System.getProperty("user.home"), "Desktop", "melody.musicxml");

    private final Score score = loadScore();

    private final RenderConfig config = RenderConfig.defaultConfig();
    private final MeasureSpacingEngine spacingEngine = new MeasureSpacingEngine(NOTE_SPACING_PER_QUARTER);
    private final PageLayoutEngine pageLayoutEngine = new PageLayoutEngine(
            WINDOW_WIDTH, STAFF_MARGIN, STAFF_MARGIN, TOP_MARGIN, SYSTEM_SPACING
    );
    private final ScoreLayoutEngine layoutEngine = new ScoreLayoutEngine(
            spacingEngine, pageLayoutEngine, config
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

    // Playback
    private final MidiPlayer midiPlayer = new MidiPlayer();
    private final PlaybackCursor playbackCursor = new PlaybackCursor();
    private List<MidiSequenceBuilder.NoteTiming> noteTimings = List.of();

    private Canvas canvas;

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

    private Score loadScore() {
        if (Files.exists(IMPORT_FILE)) {
            try {
                System.out.println("📂 Încărcare fișier: " + IMPORT_FILE);
                MusicXmlImporter importer = new MusicXmlImporter();
                Score imported = importer.importFromFile(IMPORT_FILE);
                System.out.println("✅ Fișier importat cu succes: "
                        + imported.measures().size() + " măsuri");
                return imported;
            } catch (Exception e) {
                System.err.println("❌ Eroare la import: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("ℹ️ melody.musicxml nu există. Se folosește sample score-ul.");
        }
        return createSampleScore();
    }

    @Override
    public void start(Stage primaryStage) {
        MenuBar menuBar = new MenuBar(
                new Menu("File"),
                new Menu("Edit"),
                new Menu("View"),
                new Menu("Playback")
        );

        // ─── ToolBar cu Playback ───
        Button playBtn = new Button("▶ Play");
        Button pauseBtn = new Button("⏸ Pause");
        Button stopBtn = new Button("⏹ Stop");

        Slider volumeSlider = new Slider(0, 100, 80);
        volumeSlider.setPrefWidth(120);
        volumeSlider.setShowTickLabels(false);

        Slider tempoSlider = new Slider(0.5, 2.0, 1.0);
        tempoSlider.setPrefWidth(120);
        tempoSlider.setShowTickLabels(false);

        Label volumeLabel = new Label("Vol:");
        Label tempoLabel = new Label("Tempo:");

        ToolBar toolBar = new ToolBar(
                playBtn, pauseBtn, stopBtn,
                new Separator(Orientation.VERTICAL),
                volumeLabel, volumeSlider,
                new Separator(Orientation.VERTICAL),
                tempoLabel, tempoSlider
        );

        // ─── Canvas în ScrollPane ───
        canvas = new Canvas();
        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        canvas.widthProperty().bind(scrollPane.widthProperty().subtract(20));
        scrollPane.widthProperty().addListener((e) -> drawStaff());

        Label statusBar = new Label("Ready");

        BorderPane root = new BorderPane();
        root.setTop(new javafx.scene.layout.VBox(menuBar, toolBar));
        root.setCenter(scrollPane);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle("Music Notation");
        primaryStage.setScene(scene);
        primaryStage.show();

        // ─── Inițializare playback ───
        try {
            midiPlayer.load(score);
            noteTimings = MidiSequenceBuilder.computeTimings(score);
            System.out.println("🎵 MIDI încărcat. Note: " + noteTimings.size());
        } catch (Exception e) {
            System.err.println("❌ Eroare inițializare MIDI: " + e.getMessage());
            playBtn.setDisable(true);
        }

        // ─── Butoane ───
        playBtn.setOnAction(e -> {
            midiPlayer.play();
            playbackCursor.setPlaying(true);
        });
        pauseBtn.setOnAction(e -> {
            midiPlayer.pause();
            playbackCursor.setPlaying(false);
        });
        stopBtn.setOnAction(e -> {
            midiPlayer.stop();
            playbackCursor.reset();
            drawStaff();
        });

        volumeSlider.valueProperty().addListener((obs, oldV, newV) ->
                midiPlayer.setVolume(newV.intValue()));

        tempoSlider.valueProperty().addListener((obs, oldV, newV) ->
                midiPlayer.setTempoFactor(newV.floatValue()));

        // ─── AnimationTimer pentru cursor ───
        AnimationTimer timer = new AnimationTimer() {
            private long lastDraw = 0;

            @Override
            public void handle(long now) {
                if (!playbackCursor.isPlaying()) return;
                if (now - lastDraw < 33_000_000L) return; // ~30 FPS
                lastDraw = now;

                long tick = midiPlayer.getTickPosition();
                playbackCursor.setCurrentTick(tick);

                Note current = findNoteAtTick(tick);
                if (current != playbackCursor.getCurrentNote()) {
                    playbackCursor.setCurrentNote(current);
                    drawStaff();
                }

                if (!midiPlayer.isRunning()) {
                    playbackCursor.setPlaying(false);
                    playbackCursor.setCurrentNote(null);
                    drawStaff();
                }
            }
        };
        timer.start();

        // Primul draw
        javafx.application.Platform.runLater(this::drawStaff);

        Font bravura = BravuraFont.getFont();
        System.out.println("Bravura font family: " + bravura.getFamily());
    }

    private Note findNoteAtTick(long tick) {
        for (var t : noteTimings) {
            if (tick >= t.startTick() && tick < t.endTick()) {
                return t.note();
            }
        }
        return null;
    }

    private void drawStaff() {
        double width = canvas.getWidth();
        if (width < 100) return;

        var result = layoutEngine.layout(score, width, STAFF_MARGIN, STAFF_MARGIN);

        int systemCount = result.pageLayout().systemPlacements().size();
        double neededHeight = TOP_MARGIN + systemCount * SYSTEM_SPACING + BOTTOM_MARGIN;
        if (Math.abs(canvas.getHeight() - neededHeight) > 1) {
            canvas.setHeight(neededHeight);
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        RenderingState initialState = new RenderingState(
                score.initialClef(),
                score.keySignature(),
                score.initialTimeSignature()
        );

        layoutRenderer.render(gc, result.pageLayout(), result.renderDataList(),
                initialState, playbackCursor.getCurrentNote());   // ← highlight
    }

    private static Score createSampleScore() {
        // Score-ul începe în Do major, cheia sol, 4/4
        Score score = new Score(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);

        // Măsura 1 – Accidentale + Slur + Dinamică p
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

        // Măsura 2 – Becar + Tie + Articulație Accent
        Measure m2 = new Measure();
        m2.addTextExpression(new TextExpression("cantabile", TextPlacement.BELOW));

        Note n5 = new Note(Pitch.of("Fn4"), Duration.QUARTER);
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

        // Măsura 3 – Hairpin (crescendo) + Slur + Articulație Staccato
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

        // Măsura 4 – Hairpin (decrescendo) + Dinamică f + Articulație Tenuto
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

        // Măsura 5 – Dinamică mf + Articulație Marcato + Slur
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

        // Măsura 6 – Dinamică pp + Articulație Fermata + Tie
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

        // Măsura 7 – Dinamică ff + Articulație Staccatissimo
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

        // Măsura 8 – Hairpin + Dinamică + Slur + Tie
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

        // Măsura 9 – Dinamică FFF + Articulație Staccato + Slur
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

        // Măsura 10 – Final: Notă întreagă cu Fermata + Dinamică ppp + Text "Fine"
        Measure m10 = new Measure();
        m10.addKeySignature(KeySignature.C_MAJOR);
        m10.addTextExpression(new TextExpression("Fine", TextPlacement.ABOVE));

        Note n35 = new Note(Pitch.of("C4"), Duration.WHOLE);
        m10.addNote(n35);
        m10.addArticulation(new Articulation(n35, ArticulationType.FERMATA));
        m10.addDynamic(new Dynamic(n35, DynamicType.PPP));
        m10.addEndRepeat();
        score.addMeasure(m10);

        score.validate();
        return score;
    }
    @Override
    public void stop() {
        midiPlayer.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}