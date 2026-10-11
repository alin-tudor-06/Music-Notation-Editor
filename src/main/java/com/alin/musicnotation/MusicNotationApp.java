package com.alin.musicnotation;

import com.alin.musicnotation.editor.EditMode;
import com.alin.musicnotation.editor.EditorState;
import com.alin.musicnotation.editor.HitTester;
import com.alin.musicnotation.editor.RenderedLayout;
import com.alin.musicnotation.editor.ScoreEditor;
import com.alin.musicnotation.layout.PageLayoutEngine;
import com.alin.musicnotation.layout.ScoreLayoutEngine;
import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.articulation.Articulation;
import com.alin.musicnotation.model.articulation.ArticulationType;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MusicNotationApp extends Application {

    private static final double WINDOW_WIDTH = 1000;
    private static final double WINDOW_HEIGHT = 1000;
    private static final double STAFF_LINE_SPACING = 16;
    private static final double STAFF_MARGIN = 80;
    private static final double NOTE_SPACING_PER_QUARTER = 120;
    private static final double SYSTEM_SPACING = STAFF_LINE_SPACING * 14;
    private static final double TOP_MARGIN = 140;
    private static final double BOTTOM_MARGIN = 100;

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

    // Editor
    private final EditorState editorState = new EditorState();

    // UI
    private Canvas canvas;
    private Label statusBar;
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
        this.score = createEmptyScore();

        MenuBar menuBar = buildMenuBar();
        ToolBar paletteToolbar = buildPaletteToolbar();
        ToolBar attributesToolbar = buildAttributesToolbar();

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
        root.setTop(new VBox(menuBar, paletteToolbar, attributesToolbar, playbackToolBar));
        root.setCenter(scrollPane);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle("Music Notation");
        primaryStage.setScene(scene);
        primaryStage.show();

        canvas.setOnMouseClicked(this::handleCanvasClick);
        canvas.setOnMouseMoved(this::handleMouseMoved);
        canvas.setOnMouseExited(e -> {
            editorState.clearHover();
            drawStaff();
        });
        scene.setOnKeyPressed(this::handleKeyPressed);

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

    // ============================================================
    //  PALETTE TOOLBAR
    // ============================================================

    private ToolBar buildPaletteToolbar() {
        ToggleGroup group = new ToggleGroup();

        ToggleButton selectBtn = makeToggle("↖", EditMode.SELECT, group, true);
        ToggleButton wholeBtn = makeToggle("𝅝", EditMode.ADD_WHOLE, group, false);
        ToggleButton halfBtn = makeToggle("𝅗𝅥", EditMode.ADD_HALF, group, false);
        ToggleButton quarterBtn = makeToggle("♩", EditMode.ADD_QUARTER, group, false);
        ToggleButton eighthBtn = makeToggle("♪", EditMode.ADD_EIGHTH, group, false);
        ToggleButton sixteenthBtn = makeToggle("♬", EditMode.ADD_SIXTEENTH, group, false);

        ToggleButton wholeRestBtn = makeToggle("𝄻", EditMode.ADD_WHOLE_REST, group, false);
        ToggleButton halfRestBtn = makeToggle("𝄼", EditMode.ADD_HALF_REST, group, false);
        ToggleButton quarterRestBtn = makeToggle("𝄽", EditMode.ADD_QUARTER_REST, group, false);
        ToggleButton eighthRestBtn = makeToggle("𝄾", EditMode.ADD_EIGHTH_REST, group, false);
        ToggleButton sixteenthRestBtn = makeToggle("𝄿", EditMode.ADD_SIXTEENTH_REST, group, false);

        ToggleButton sharpBtn = makeToggle("♯", EditMode.ACCIDENTAL_SHARP, group, false);
        ToggleButton flatBtn = makeToggle("♭", EditMode.ACCIDENTAL_FLAT, group, false);
        ToggleButton naturalBtn = makeToggle("♮", EditMode.ACCIDENTAL_NATURAL, group, false);

        ToggleButton staccatoBtn = makeToggle("•", EditMode.ARTICULATION_STACCATO, group, false);
        ToggleButton staccatissimoBtn = makeToggle("▾", EditMode.ARTICULATION_STACCATISSIMO, group, false);
        ToggleButton accentBtn = makeToggle(">", EditMode.ARTICULATION_ACCENT, group, false);
        ToggleButton marcatoBtn = makeToggle("^", EditMode.ARTICULATION_MARCATO, group, false);
        ToggleButton tenutoBtn = makeToggle("–", EditMode.ARTICULATION_TENUTO, group, false);
        ToggleButton fermataBtn = makeToggle("⌒", EditMode.ARTICULATION_FERMATA, group, false);

        group.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                editorState.setMode(EditMode.SELECT);
            } else {
                editorState.setMode((EditMode) newT.getUserData());
            }
            editorState.clearHover();
            updateStatusBar();
            drawStaff();
        });

        ToolBar toolbar = new ToolBar(
                selectBtn,
                new Separator(Orientation.VERTICAL),
                wholeBtn, halfBtn, quarterBtn, eighthBtn, sixteenthBtn,
                new Separator(Orientation.VERTICAL),
                wholeRestBtn, halfRestBtn, quarterRestBtn, eighthRestBtn, sixteenthRestBtn,
                new Separator(Orientation.VERTICAL),
                sharpBtn, flatBtn, naturalBtn,
                new Separator(Orientation.VERTICAL),
                staccatoBtn, staccatissimoBtn, accentBtn, marcatoBtn, tenutoBtn, fermataBtn
        );
        toolbar.setStyle("-fx-font-size: 18px;");
        return toolbar;
    }

    private ToggleButton makeToggle(String label, EditMode mode, ToggleGroup group, boolean selected) {
        ToggleButton btn = new ToggleButton(label);
        btn.setToggleGroup(group);
        btn.setUserData(mode);
        btn.setSelected(selected);
        btn.setMinWidth(45);
        return btn;
    }

    // ============================================================
    //  ATTRIBUTES TOOLBAR
    // ============================================================

    private ToolBar buildAttributesToolbar() {
        ComboBox<String> clefBox = new ComboBox<>();
        clefBox.getItems().addAll("Treble", "Bass", "Alto", "Tenor");
        clefBox.setPromptText("Clef");
        clefBox.setPrefWidth(110);
        clefBox.setOnAction(e -> {
            String val = clefBox.getValue();
            if (val == null) return;
            ClefType ct = switch (val) {
                case "Treble" -> ClefType.TREBLE;
                case "Bass" -> ClefType.BASS;
                case "Alto" -> ClefType.ALTO;
                case "Tenor" -> ClefType.TENOR;
                default -> ClefType.TREBLE;
            };
            applyAttributeToSelectedMeasure(m -> m.addClef(ct));
            clefBox.setValue(null);
        });

        ComboBox<String> keyBox = new ComboBox<>();
        keyBox.getItems().addAll(
                "C major", "G major", "D major", "A major", "E major", "B major",
                "F# major", "C# major",
                "F major", "Bb major", "Eb major", "Ab major", "Db major",
                "Gb major", "Cb major"
        );
        keyBox.setPromptText("Key");
        keyBox.setPrefWidth(120);
        keyBox.setOnAction(e -> {
            String val = keyBox.getValue();
            if (val == null) return;
            KeySignature ks = switch (val) {
                case "C major" -> KeySignature.C_MAJOR;
                case "G major" -> KeySignature.G_MAJOR;
                case "D major" -> KeySignature.D_MAJOR;
                case "A major" -> KeySignature.A_MAJOR;
                case "E major" -> KeySignature.E_MAJOR;
                case "B major" -> KeySignature.B_MAJOR;
                case "F# major" -> KeySignature.F_SHARP_MAJOR;
                case "C# major" -> KeySignature.C_SHARP_MAJOR;
                case "F major" -> KeySignature.F_MAJOR;
                case "Bb major" -> KeySignature.B_FLAT_MAJOR;
                case "Eb major" -> KeySignature.E_FLAT_MAJOR;
                case "Ab major" -> KeySignature.A_FLAT_MAJOR;
                case "Db major" -> KeySignature.D_FLAT_MAJOR;
                case "Gb major" -> KeySignature.G_FLAT_MAJOR;
                case "Cb major" -> KeySignature.C_FLAT_MAJOR;
                default -> KeySignature.C_MAJOR;
            };
            applyAttributeToSelectedMeasure(m -> m.addKeySignature(ks));
            keyBox.setValue(null);
        });

        ComboBox<String> timeBox = new ComboBox<>();
        timeBox.getItems().addAll(
                "4/4", "3/4", "2/4", "6/8", "9/8", "12/8", "2/2", "3/8", "5/4"
        );
        timeBox.setPromptText("Time");
        timeBox.setPrefWidth(80);
        timeBox.setOnAction(e -> {
            String val = timeBox.getValue();
            if (val == null) return;
            TimeSignature ts = switch (val) {
                case "4/4" -> TimeSignature.COMMON_TIME;
                case "3/4" -> TimeSignature.THREE_FOUR;
                case "2/4" -> TimeSignature.TWO_FOUR;
                case "6/8" -> TimeSignature.SIX_EIGHT;
                case "9/8" -> TimeSignature.NINE_EIGHT;
                case "12/8" -> TimeSignature.TWELVE_EIGHT;
                case "2/2" -> TimeSignature.CUT_TIME;
                case "3/8" -> new TimeSignature(3, 8);
                case "5/4" -> new TimeSignature(5, 4);
                default -> TimeSignature.COMMON_TIME;
            };
            applyAttributeToSelectedMeasure(m -> m.addTimeSignature(ts));
            timeBox.setValue(null);
        });

        ToolBar toolbar = new ToolBar(
                new Label("Attributes:"),
                new Separator(Orientation.VERTICAL),
                new Label("Clef"), clefBox,
                new Separator(Orientation.VERTICAL),
                new Label("Key"), keyBox,
                new Separator(Orientation.VERTICAL),
                new Label("Time"), timeBox
        );
        return toolbar;
    }

    private void applyAttributeToSelectedMeasure(java.util.function.Consumer<Measure> action) {
        Measure m = editorState.getSelectedMeasure();
        if (m == null) {
            statusBar.setText("⚠️ Selectează mai întâi o măsură (click pe spațiul gol din ea)");
            return;
        }
        action.accept(m);
        applyScoreToMidi(score);
        drawStaff();
    }

    // ============================================================
    //  PLAYBACK TOOLBAR
    // ============================================================

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
    //  FILE ACTIONS
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
            if (Files.exists(desktop)) chooser.setInitialDirectory(desktop.toFile());
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
            if (Files.exists(desktop)) chooser.setInitialDirectory(desktop.toFile());
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
    //  MOUSE HANDLERS
    // ============================================================

    private void handleCanvasClick(MouseEvent event) {
        double mx = event.getX();
        double my = event.getY();

        RenderedLayout layout = layoutRenderer.getLastLayout();

        // ─── Mod ACCIDENTAL ───
        if (editorState.getMode().isAccidentalMode()) {
            Note hit = HitTester.findNoteAt(layout, mx, my);
            if (hit == null) {
                statusBar.setText("⚠️ Click pe o notă pentru a aplica accidentalul");
                return;
            }
            Accidental target = switch (editorState.getMode()) {
                case ACCIDENTAL_SHARP -> Accidental.SHARP;
                case ACCIDENTAL_FLAT -> Accidental.FLAT;
                case ACCIDENTAL_NATURAL -> Accidental.NATURAL;
                default -> null;
            };
            if (target != null) {
                applyAccidentalToNote(hit, target);
                applyScoreToMidi(score);
                drawStaff();
            }
            return;
        }

        // ─── Mod ARTICULAȚIE ───
        if (editorState.getMode().isArticulationMode()) {
            Note hit = HitTester.findNoteAt(layout, mx, my);
            if (hit == null) {
                statusBar.setText("⚠️ Click pe o notă pentru a aplica articulația");
                return;
            }
            ArticulationType target = switch (editorState.getMode()) {
                case ARTICULATION_STACCATO -> ArticulationType.STACCATO;
                case ARTICULATION_STACCATISSIMO -> ArticulationType.STACCATISSIMO;
                case ARTICULATION_ACCENT -> ArticulationType.ACCENT;
                case ARTICULATION_MARCATO -> ArticulationType.MARCATO;
                case ARTICULATION_TENUTO -> ArticulationType.TENUTO;
                case ARTICULATION_FERMATA -> ArticulationType.FERMATA;
                default -> null;
            };
            if (target != null) {
                applyArticulationToNote(hit, target);
                applyScoreToMidi(score);
                drawStaff();
            }
            return;
        }

        // ─── Mod ADD (note/pauze) ───
        if (editorState.getMode().isAddMode()) {
            RenderedLayout.MeasureHitBox mh = HitTester.findMeasureAt(layout, mx, my);
            if (mh == null) return;

            Duration dur = modeToDuration(editorState.getMode());
            Note newNote;
            if (editorState.getMode().isRestMode()) {
                newNote = new Note(dur);
            } else {
                int pos = HitTester.staffPositionFromY(mh, my);
                Pitch pitch = HitTester.pitchFromStaffPosition(pos, mh.clef());
                newNote = new Note(pitch, dur);
            }

            ScoreEditor.addNoteSmart(score, mh.measure(), newNote);
            applyScoreToMidi(score);
            drawStaff();
            return;
        }

        // ─── Mod SELECT ───
        Note hit = HitTester.findNoteAt(layout, mx, my);
        if (hit != null) {
            editorState.setSelectedNote(hit);
            drawStaff();
            return;
        }

        RenderedLayout.MeasureHitBox mh = HitTester.findMeasureAt(layout, mx, my);
        if (mh != null) {
            editorState.setSelectedMeasure(mh.measure());
        } else {
            editorState.clearSelection();
        }
        drawStaff();
    }

    private void handleMouseMoved(MouseEvent event) {
        boolean showGhost = editorState.getMode().isAddMode()
                && !editorState.getMode().isRestMode();

        if (!showGhost) {
            if (editorState.isHovering()) {
                editorState.clearHover();
                drawStaff();
            }
            return;
        }

        double mx = event.getX();
        double my = event.getY();

        RenderedLayout layout = layoutRenderer.getLastLayout();
        RenderedLayout.MeasureHitBox mh = HitTester.findMeasureAt(layout, mx, my);

        if (mh == null) {
            if (editorState.isHovering()) {
                editorState.clearHover();
                drawStaff();
            }
            return;
        }

        Note hit = HitTester.findNoteAt(layout, mx, my);
        if (hit != null) {
            if (editorState.isHovering()) {
                editorState.clearHover();
                drawStaff();
            }
            return;
        }

        int pos = HitTester.staffPositionFromY(mh, my);
        Pitch pitch = HitTester.pitchFromStaffPosition(pos, mh.clef());

        boolean changed = !editorState.isHovering()
                || editorState.getHoverPitch() == null
                || !editorState.getHoverPitch().equals(pitch)
                || Math.abs(editorState.getHoverX() - mx) > 1.5;

        editorState.setHover(mx, pitch, mh.clef(), mh.staffFirstLineY(), mh.staffLineSpacing());

        if (changed) {
            drawStaff();
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
            Note sel = editorState.getSelectedNote();
            if (sel != null) {
                ScoreEditor.removeNote(score, sel);
                editorState.clearSelection();
                applyScoreToMidi(score);
                drawStaff();
            }
        }
    }

    /**
     * Aplică accidentalul pe o notă cu toggle.
     */
    private void applyAccidentalToNote(Note note, Accidental target) {
        if (note.isRest()) {
            statusBar.setText("⚠️ Nu poți aplica accidental pe o pauză");
            return;
        }
        Pitch old = note.pitch();
        Accidental current = old.accidental();
        Accidental newAcc = (current == target) ? Accidental.NONE : target;
        Pitch newPitch = new Pitch(old.step(), newAcc, old.octave());
        note.setPitch(newPitch);
    }

    /**
     * Aplică sau scoate o articulație pe o notă (toggle).
     */
    private void applyArticulationToNote(Note note, ArticulationType type) {
        if (note.isRest()) {
            statusBar.setText("⚠️ Nu poți aplica articulație pe o pauză");
            return;
        }

        // Găsim măsura care conține nota
        Measure targetMeasure = null;
        for (Measure m : score.measures()) {
            if (m.notes().contains(note)) {
                targetMeasure = m;
                break;
            }
        }
        if (targetMeasure == null) return;

        // Căutăm o articulație existentă de același tip pe această notă
        Articulation existing = null;
        for (Articulation a : targetMeasure.articulations()) {
            if (a.note() == note && a.type() == type) {
                existing = a;
                break;
            }
        }

        if (existing != null) {
            targetMeasure.removeArticulation(existing);
        } else {
            targetMeasure.addArticulation(new Articulation(note, type));
        }
    }

    private static Duration modeToDuration(EditMode mode) {
        return switch (mode) {
            case ADD_WHOLE, ADD_WHOLE_REST -> Duration.WHOLE;
            case ADD_HALF, ADD_HALF_REST -> Duration.HALF;
            case ADD_QUARTER, ADD_QUARTER_REST -> Duration.QUARTER;
            case ADD_EIGHTH, ADD_EIGHTH_REST -> Duration.EIGHTH;
            case ADD_SIXTEENTH, ADD_SIXTEENTH_REST -> Duration.SIXTEENTH;
            default -> Duration.QUARTER;
        };
    }

    // ============================================================
    //  INTERNAL
    // ============================================================

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
        editorState.clearSelection();
        editorState.clearHover();

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
        String sel = editorState.getSelectedMeasure() != null
                ? "  |  Măsură selectată"
                : editorState.getSelectedNote() != null
                ? "  |  Notă selectată"
                : "";
        statusBar.setText("📄 " + name
                + "  |  " + score.measures().size() + " măsuri"
                + "  |  Mod: " + editorState.getMode()
                + sel);
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

    // ============================================================
    //  DRAW
    // ============================================================

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

        Note highlight = playbackCursor.isPlaying()
                ? playbackCursor.getCurrentNote()
                : editorState.getSelectedNote();

        layoutRenderer.render(gc, result.pageLayout(), result.renderDataList(),
                initialState, highlight);

        if (editorState.getSelectedMeasure() != null) {
            RenderedLayout layout = layoutRenderer.getLastLayout();
            for (RenderedLayout.MeasureHitBox mb : layout.measures()) {
                if (mb.measure() == editorState.getSelectedMeasure()) {
                    gc.setStroke(Color.DODGERBLUE);
                    gc.setLineWidth(2.5);
                    gc.strokeRect(mb.x() - 4, mb.y() + 20,
                            mb.width() + 8, mb.height() - 40);
                    break;
                }
            }
        }

        drawHoverGhost(gc);

        updateStatusBar();
    }

    private void drawHoverGhost(GraphicsContext gc) {
        if (!editorState.isHovering()) return;
        if (editorState.getHoverPitch() == null) return;

        Pitch hp = editorState.getHoverPitch();
        ClefType clef = editorState.getHoverClef();
        int pos = StaffPositionCalculator.staffPosition(hp, clef);
        double noteY = editorState.getHoverStaffFirstLineY()
                + pos * (editorState.getHoverLineSpacing() / 2.0);
        double noteX = editorState.getHoverX();

        double w = config.note().headWidth();
        double h = config.note().headHeight();

        gc.setGlobalAlpha(0.55);
        gc.setFill(Color.GRAY);
        gc.fillOval(noteX - w / 2, noteY - h / 2, w, h);

        int topLinePos = config.staff().topLine();
        int bottomLinePos = config.staff().bottomLine();
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);

        if (pos < topLinePos) {
            int count = -pos / 2;
            for (int i = 1; i <= count; i++) {
                int lp = -2 * i;
                double ly = editorState.getHoverStaffFirstLineY()
                        + lp * (editorState.getHoverLineSpacing() / 2.0);
                double halfW = w / 2 + 6;
                gc.strokeLine(noteX - halfW, ly, noteX + halfW, ly);
            }
        } else if (pos > bottomLinePos) {
            int count = (pos - bottomLinePos) / 2;
            for (int i = 1; i <= count; i++) {
                int lp = bottomLinePos + 2 * i;
                double ly = editorState.getHoverStaffFirstLineY()
                        + lp * (editorState.getHoverLineSpacing() / 2.0);
                double halfW = w / 2 + 6;
                gc.strokeLine(noteX - halfW, ly, noteX + halfW, ly);
            }
        }

        gc.setGlobalAlpha(1.0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}