package com.alin.musicnotation;

import com.alin.musicnotation.layout.PageLayoutEngine;
import com.alin.musicnotation.layout.ScoreLayoutEngine;
import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.clef.ClefType;
import com.alin.musicnotation.musicxml.export.MusicXmlExporter;
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
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MusicNotationApp extends Application {

    private static final double WINDOW_WIDTH = 900;
    private static final double WINDOW_HEIGHT = 1000;
    private static final double STAFF_LINE_SPACING = 16;
    private static final double STAFF_MARGIN = 80;
    private static final double NOTE_SPACING_PER_QUARTER = 120;
    private static final double SYSTEM_SPACING = STAFF_LINE_SPACING * 14;
    private static final double TOP_MARGIN = 140;
    private static final double BOTTOM_MARGIN = 100;

    // ─── Stare aplicație ───
    private Score score;
    private Path currentFile;
    private Stage primaryStage;

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
    private Label statusBar;

    // UI playback controls
    private ToolBar playbackToolBar;
    private Button playBtn;
    private Slider volumeSlider;
    private Slider tempoSlider;

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

    // ============================================================
    //  START
    // ============================================================

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Pornim cu un score gol
        score = createEmptyScore();

        MenuBar menuBar = buildMenuBar();

        playbackToolBar = buildPlaybackToolBar();
        playbackToolBar.setVisible(false);
        playbackToolBar.setManaged(false);

        canvas = new Canvas();
        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        canvas.widthProperty().bind(scrollPane.widthProperty().subtract(20));
        scrollPane.widthProperty().addListener((e) -> drawStaff());

        statusBar = new Label();

        BorderPane root = new BorderPane();
        root.setTop(new VBox(menuBar, playbackToolBar));
        root.setCenter(scrollPane);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle("Music Notation");
        primaryStage.setScene(scene);
        primaryStage.show();

        applyScoreToMidi(score);
        updateStatusBar();

        javafx.application.Platform.runLater(this::drawStaff);

        Font bravura = BravuraFont.getFont();
        System.out.println("Bravura font family: " + bravura.getFamily());
    }

    @Override
    public void stop() {
        midiPlayer.close();
    }

    // ============================================================
    //  MENIU
    // ============================================================

    private MenuBar buildMenuBar() {
        MenuItem newItem = new MenuItem("New");
        newItem.setOnAction(e -> actionNew());

        MenuItem openItem = new MenuItem("Open...");
        openItem.setOnAction(e -> actionOpen());

        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> actionSave());

        MenuItem saveAsItem = new MenuItem("Save As...");
        saveAsItem.setOnAction(e -> actionSaveAs());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> primaryStage.close());

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(
                newItem, openItem,
                new SeparatorMenuItem(),
                saveItem, saveAsItem,
                new SeparatorMenuItem(),
                exitItem
        );

        CheckMenuItem showPlaybackItem = new CheckMenuItem("Show Playback Controls");
        showPlaybackItem.setSelected(false);
        showPlaybackItem.setOnAction(e -> {
            boolean show = showPlaybackItem.isSelected();
            playbackToolBar.setVisible(show);
            playbackToolBar.setManaged(show);
        });

        Menu playbackMenu = new Menu("Playback");
        playbackMenu.getItems().add(showPlaybackItem);

        return new MenuBar(fileMenu, playbackMenu);
    }

    private ToolBar buildPlaybackToolBar() {
        playBtn = new Button("▶ Play");
        Button pauseBtn = new Button("⏸ Pause");
        Button stopBtn = new Button("⏹ Stop");

        volumeSlider = new Slider(0, 100, 80);
        volumeSlider.setPrefWidth(120);

        tempoSlider = new Slider(0.5, 2.0, 1.0);
        tempoSlider.setPrefWidth(120);

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

        ToolBar toolBar = new ToolBar(
                playBtn, pauseBtn, stopBtn,
                new Separator(Orientation.VERTICAL),
                new Label("Vol:"), volumeSlider,
                new Separator(Orientation.VERTICAL),
                new Label("Tempo:"), tempoSlider
        );

        AnimationTimer timer = new AnimationTimer() {
            private long lastDraw = 0;
            @Override
            public void handle(long now) {
                if (!playbackCursor.isPlaying()) return;
                if (now - lastDraw < 33_000_000L) return;
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

        return toolBar;
    }

    // ============================================================
    //  ACȚIUNI FILE
    // ============================================================

    private void actionNew() {
        applyScore(createEmptyScore(), null);
    }

    private void actionOpen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open MusicXML");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MusicXML", "*.musicxml", "*.xml", "*.mxl"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        if (currentFile != null && currentFile.getParent() != null) {
            chooser.setInitialDirectory(currentFile.getParent().toFile());
        } else {
            Path desktop = Paths.get(System.getProperty("user.home"), "Desktop");
            if (java.nio.file.Files.exists(desktop)) chooser.setInitialDirectory(desktop.toFile());
        }

        File file = chooser.showOpenDialog(primaryStage);
        if (file == null) return;

        try {
            MusicXmlImporter importer = new MusicXmlImporter();
            Score imported = importer.importFromFile(file.toPath());
            applyScore(imported, file.toPath());
        } catch (Exception e) {
            showError("Eroare la deschidere", e);
        }
    }

    private void actionSave() {
        if (currentFile == null) {
            actionSaveAs();
            return;
        }
        try {
            MusicXmlExporter exporter = new MusicXmlExporter();
            exporter.export(score, currentFile);
            updateStatusBar();
        } catch (Exception e) {
            showError("Eroare la salvare", e);
        }
    }

    private void actionSaveAs() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save MusicXML As");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MusicXML (*.musicxml)", "*.musicxml"),
                new FileChooser.ExtensionFilter("XML (*.xml)", "*.xml"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        chooser.setInitialFileName(currentFile != null
                ? currentFile.getFileName().toString()
                : "untitled.musicxml");

        if (currentFile != null && currentFile.getParent() != null) {
            chooser.setInitialDirectory(currentFile.getParent().toFile());
        } else {
            Path desktop = Paths.get(System.getProperty("user.home"), "Desktop");
            if (java.nio.file.Files.exists(desktop)) chooser.setInitialDirectory(desktop.toFile());
        }

        File file = chooser.showSaveDialog(primaryStage);
        if (file == null) return;

        try {
            MusicXmlExporter exporter = new MusicXmlExporter();
            exporter.export(score, file.toPath());
            currentFile = file.toPath();
            updateStatusBar();
        } catch (Exception e) {
            showError("Eroare la salvare", e);
        }
    }

    // ============================================================
    //  LOGICĂ INTERNĂ
    // ============================================================

    /**
     * Creează un score gol cu o singură măsură goală,
     * la fel ca File → New.
     */
    private static Score createEmptyScore() {
        Score score = new Score(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);
        score.addMeasure(new Measure());
        return score;
    }

    private void applyScore(Score newScore, Path file) {
        midiPlayer.stop();
        this.score = newScore;
        this.currentFile = file;
        playbackCursor.reset();

        applyScoreToMidi(score);
        updateStatusBar();
        drawStaff();
    }

    private void applyScoreToMidi(Score s) {
        try {
            midiPlayer.load(s);
            noteTimings = MidiSequenceBuilder.computeTimings(s);
            midiPlayer.setVolume((int) volumeSlider.getValue());
            midiPlayer.setTempoFactor((float) tempoSlider.getValue());
            playBtn.setDisable(false);
            System.out.println("🎵 MIDI încărcat. Note: " + noteTimings.size());
        } catch (Exception e) {
            System.err.println("❌ Eroare MIDI: " + e.getMessage());
            playBtn.setDisable(true);
            noteTimings = List.of();
        }
    }

    private void updateStatusBar() {
        String name = currentFile != null
                ? currentFile.getFileName().toString()
                : "untitled";
        statusBar.setText("📄 " + name
                + "  |  " + score.measures().size() + " măsuri");
    }

    private void showError(String title, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Eroare");
        alert.setHeaderText(title);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
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
        double neededHeight = TOP_MARGIN + Math.max(systemCount, 1) * SYSTEM_SPACING + BOTTOM_MARGIN;
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
                initialState, playbackCursor.getCurrentNote());
    }

    public static void main(String[] args) {
        launch(args);
    }
}