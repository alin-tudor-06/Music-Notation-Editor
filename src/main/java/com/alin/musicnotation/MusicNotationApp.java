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
import com.alin.musicnotation.model.dynamic.Dynamic;
import com.alin.musicnotation.model.dynamic.DynamicType;
import com.alin.musicnotation.model.hairpin.Hairpin;
import com.alin.musicnotation.model.hairpin.HairpinPlacement;
import com.alin.musicnotation.model.hairpin.HairpinType;
import com.alin.musicnotation.model.tempo.Tempo;
import com.alin.musicnotation.model.tempo.TempoPlacement;
import com.alin.musicnotation.model.text.TextExpression;
import com.alin.musicnotation.model.text.TextPlacement;
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import java.util.ArrayList;
import java.util.List;

public class MusicNotationApp extends Application {

    private static final double WINDOW_WIDTH = 1200;
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
    private final TieRenderer tieRenderer = new TieRenderer();

    private final LayoutRenderer layoutRenderer;

    private final MidiPlayer midiPlayer = new MidiPlayer();
    private final PlaybackCursor playbackCursor = new PlaybackCursor();
    private List<MidiSequenceBuilder.NoteTiming> noteTimings = List.of();

    private final EditorState editorState = new EditorState();

    private Canvas canvas;
    private Label statusBar;
    private ToolBar playbackToolBar;
    private ToolBar itemToolBar;
    private ToggleGroup editModeGroup;
    private Button playBtn;
    private Slider volumeSlider;
    private Slider tempoSlider;

    public MusicNotationApp() {
        MeasurePainter measurePainter = new MeasurePainter(
                tempoRenderer, textExpressionRenderer, noteRenderer, beamRenderer,
                tupletRenderer, slurRenderer, articulationRenderer, dynamicRenderer,
                hairpinRenderer, repeatRenderer,
                clefRenderer, keySignatureRenderer, timeSignatureRenderer,
                tieRenderer
        );
        MeasureRenderer measureRenderer = new MeasureRenderer(config, measurePainter);
        SystemRenderer systemRenderer = new SystemRenderer(
                config, measureRenderer, clefRenderer, keySignatureRenderer, timeSignatureRenderer
        );
        layoutRenderer = new LayoutRenderer(systemRenderer);
    }


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.score = createEmptyScore();

        statusBar = new Label();

        MenuBar menuBar = buildMenuBar();
        ToolBar categoryToolbar = buildCategoryToolbar();
        itemToolBar = new ToolBar();
        itemToolBar.setStyle("-fx-font-size: 18px;");

        ToolBar attributesToolbar = buildAttributesToolbar();

        playbackToolBar = buildPlaybackToolBar();
        playbackToolBar.setVisible(false);
        playbackToolBar.setManaged(false);

        showCategoryItems("Notes");

        canvas = new Canvas();
        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        canvas.widthProperty().bind(scrollPane.widthProperty().subtract(20));
        scrollPane.widthProperty().addListener((e) -> drawStaff());

        BorderPane root = new BorderPane();
        root.setTop(new VBox(menuBar, categoryToolbar, itemToolBar, attributesToolbar, playbackToolBar));
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


    private ToolBar buildCategoryToolbar() {
        ToggleGroup catGroup = new ToggleGroup();

        ToggleButton notesCat = makeCategoryButton("Notes", catGroup, true);
        ToggleButton restsCat = makeCategoryButton("Rests", catGroup, false);
        ToggleButton accCat = makeCategoryButton("Accidentals", catGroup, false);
        ToggleButton artCat = makeCategoryButton("Articulations", catGroup, false);
        ToggleButton dynCat = makeCategoryButton("Dynamics", catGroup, false);
        ToggleButton connCat = makeCategoryButton("Connections", catGroup, false);
        ToggleButton hairCat = makeCategoryButton("Hairpins", catGroup, false);
        ToggleButton signsCat = makeCategoryButton("Signs", catGroup, false);

        catGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                catGroup.selectToggle(oldT);
                return;
            }
            String cat = (String) newT.getUserData();
            showCategoryItems(cat);
        });

        ToolBar tb = new ToolBar(
                notesCat, restsCat, accCat, artCat, dynCat, connCat, hairCat, signsCat
        );
        tb.setStyle("-fx-font-size: 13px; -fx-padding: 4;");
        return tb;
    }

    private ToggleButton makeCategoryButton(String label, ToggleGroup g, boolean selected) {
        ToggleButton b = new ToggleButton(label);
        b.setToggleGroup(g);
        b.setUserData(label);
        b.setSelected(selected);
        b.setMinWidth(100);
        b.setStyle("-fx-font-size: 13px;");
        return b;
    }


    private void showCategoryItems(String category) {
        editorState.setMode(EditMode.SELECT);
        editorState.clearHover();
        editorState.clearPendingHairpin();
        editorState.clearPendingConnections();

        editModeGroup = new ToggleGroup();
        editModeGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                editorState.setMode(EditMode.SELECT);
            } else {
                editorState.setMode((EditMode) newT.getUserData());
            }
            editorState.clearHover();
            editorState.clearPendingHairpin();
            editorState.clearPendingConnections();
            updateStatusBar();
            drawStaff();
        });

        itemToolBar.getItems().clear();

        switch (category) {
            case "Notes" -> {
                addSelectButton();
                itemToolBar.getItems().add(new Separator(Orientation.VERTICAL));
                itemToolBar.getItems().addAll(
                        makeToggle("𝅝", EditMode.ADD_WHOLE, editModeGroup, false),
                        makeToggle("𝅗𝅥", EditMode.ADD_HALF, editModeGroup, false),
                        makeToggle("♩", EditMode.ADD_QUARTER, editModeGroup, false),
                        makeToggle("♪", EditMode.ADD_EIGHTH, editModeGroup, false),
                        makeToggle("♬", EditMode.ADD_SIXTEENTH, editModeGroup, false)
                );
            }
            case "Rests" -> {
                addSelectButton();
                itemToolBar.getItems().add(new Separator(Orientation.VERTICAL));
                itemToolBar.getItems().addAll(
                        makeToggle("𝄻", EditMode.ADD_WHOLE_REST, editModeGroup, false),
                        makeToggle("𝄼", EditMode.ADD_HALF_REST, editModeGroup, false),
                        makeToggle("𝄽", EditMode.ADD_QUARTER_REST, editModeGroup, false),
                        makeToggle("𝄾", EditMode.ADD_EIGHTH_REST, editModeGroup, false),
                        makeToggle("𝄿", EditMode.ADD_SIXTEENTH_REST, editModeGroup, false)
                );
            }
            case "Accidentals" -> {
                addSelectButton();
                itemToolBar.getItems().add(new Separator(Orientation.VERTICAL));
                itemToolBar.getItems().addAll(
                        makeToggle("♯", EditMode.ACCIDENTAL_SHARP, editModeGroup, false),
                        makeToggle("♭", EditMode.ACCIDENTAL_FLAT, editModeGroup, false),
                        makeToggle("♮", EditMode.ACCIDENTAL_NATURAL, editModeGroup, false)
                );
            }
            case "Articulations" -> {
                addSelectButton();
                itemToolBar.getItems().add(new Separator(Orientation.VERTICAL));
                itemToolBar.getItems().addAll(
                        makeToggle("•", EditMode.ARTICULATION_STACCATO, editModeGroup, false),
                        makeToggle("▾", EditMode.ARTICULATION_STACCATISSIMO, editModeGroup, false),
                        makeToggle(">", EditMode.ARTICULATION_ACCENT, editModeGroup, false),
                        makeToggle("^", EditMode.ARTICULATION_MARCATO, editModeGroup, false),
                        makeToggle("–", EditMode.ARTICULATION_TENUTO, editModeGroup, false),
                        makeToggle("⌒", EditMode.ARTICULATION_FERMATA, editModeGroup, false)
                );
            }
            case "Dynamics" -> {
                addSelectButton();
                itemToolBar.getItems().add(new Separator(Orientation.VERTICAL));
                itemToolBar.getItems().addAll(
                        makeToggle("ppp", EditMode.DYNAMIC_PPP, editModeGroup, false),
                        makeToggle("pp", EditMode.DYNAMIC_PP, editModeGroup, false),
                        makeToggle("p", EditMode.DYNAMIC_P, editModeGroup, false),
                        makeToggle("mp", EditMode.DYNAMIC_MP, editModeGroup, false),
                        makeToggle("mf", EditMode.DYNAMIC_MF, editModeGroup, false),
                        makeToggle("f", EditMode.DYNAMIC_F, editModeGroup, false),
                        makeToggle("ff", EditMode.DYNAMIC_FF, editModeGroup, false),
                        makeToggle("fff", EditMode.DYNAMIC_FFF, editModeGroup, false)
                );
            }
            case "Connections" -> {
                addSelectButton();
                itemToolBar.getItems().add(new Separator(Orientation.VERTICAL));
                ToggleButton tieBtn = makeToggle("⌒", EditMode.TIE, editModeGroup, false);
                tieBtn.setTooltip(new Tooltip("Tie / legătură de durată (2 click-uri)"));
                ToggleButton slurBtn = makeToggle("‿", EditMode.SLUR, editModeGroup, false);
                slurBtn.setTooltip(new Tooltip("Slur / legătură de portament (2 click-uri)"));
                itemToolBar.getItems().addAll(tieBtn, slurBtn);
            }
            case "Hairpins" -> {
                addSelectButton();
                itemToolBar.getItems().add(new Separator(Orientation.VERTICAL));
                ToggleButton cresc = makeToggle("↗", EditMode.HAIRPIN_CRESCENDO, editModeGroup, false);
                cresc.setTooltip(new Tooltip("Crescendo (2 click-uri: start, end)"));
                ToggleButton decresc = makeToggle("↘", EditMode.HAIRPIN_DECRESCENDO, editModeGroup, false);
                decresc.setTooltip(new Tooltip("Decrescendo (2 click-uri: start, end)"));
                itemToolBar.getItems().addAll(cresc, decresc);
            }
            case "Signs" -> {
                Button tempoBtn = makeActionButton("♩= Tempo", "Adaugă tempo pe măsura selectată", e -> showTempoDialog());
                Button textBtn = makeActionButton("Text", "Adaugă expresie text pe măsura selectată", e -> showTextDialog());
                Button startRepBtn = makeActionButton("‖:", "Toggle Start Repeat pe măsura selectată", e -> toggleStartRepeat());
                Button endRepBtn = makeActionButton(":‖", "Toggle End Repeat pe măsura selectată", e -> toggleEndRepeat());

                itemToolBar.getItems().addAll(
                        tempoBtn,
                        textBtn,
                        new Separator(Orientation.VERTICAL),
                        startRepBtn,
                        endRepBtn
                );
            }
        }

        updateStatusBar();
        if (canvas != null) drawStaff();
    }

    private void addSelectButton() {
        ToggleButton selectToggle = makeToggle("↖", EditMode.SELECT, editModeGroup, true);
        selectToggle.setTooltip(new Tooltip("Select (mod implicit)"));
        itemToolBar.getItems().add(selectToggle);
    }

    private ToggleButton makeToggle(String label, EditMode mode, ToggleGroup group, boolean selected) {
        ToggleButton btn = new ToggleButton(label);
        btn.setToggleGroup(group);
        btn.setUserData(mode);
        btn.setSelected(selected);
        btn.setMinWidth(45);
        btn.setStyle("-fx-font-size: 16px;");
        return btn;
    }

    private Button makeActionButton(String label, String tooltip, EventHandler<ActionEvent> handler) {
        Button btn = new Button(label);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setOnAction(handler);
        btn.setStyle("-fx-font-size: 13px;");
        return btn;
    }


    private void toggleStartRepeat() {
        Measure m = editorState.getSelectedMeasure();
        if (m == null) {
            statusBar.setText("⚠️ Selectează mai întâi o măsură (click pe spațiul gol din ea)");
            return;
        }
        if (m.hasStartRepeat()) {
            m.removeStartRepeat();
        } else {
            m.addStartRepeat();
        }
        applyScoreToMidi(score);
        drawStaff();
    }

    private void toggleEndRepeat() {
        Measure m = editorState.getSelectedMeasure();
        if (m == null) {
            statusBar.setText("⚠️ Selectează mai întâi o măsură (click pe spațiul gol din ea)");
            return;
        }
        if (m.hasEndRepeat()) {
            m.removeEndRepeat();
        } else {
            m.addEndRepeat();
        }
        applyScoreToMidi(score);
        drawStaff();
    }


    private void showTempoDialog() {
        Measure m = editorState.getSelectedMeasure();
        if (m == null) {
            statusBar.setText("⚠️ Selectează mai întâi o măsură (click pe spațiul gol din ea)");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tempo");
        dialog.setHeaderText("Setează tempo pentru măsura selectată");

        TextField textField = new TextField();
        if (m.tempo() != null && m.tempo().hasText()) {
            textField.setText(m.tempo().text());
        }
        textField.setPromptText("Allegro");

        TextField bpmField = new TextField();
        if (m.tempo() != null && m.tempo().hasBpm()) {
            bpmField.setText(String.valueOf(m.tempo().bpm()));
        }
        bpmField.setPromptText("120");

        ComboBox<String> placementBox = new ComboBox<>();
        placementBox.getItems().addAll("Above", "Below");
        if (m.tempo() != null && m.tempo().placement() == TempoPlacement.BELOW) {
            placementBox.setValue("Below");
        } else {
            placementBox.setValue("Above");
        }

        VBox content = new VBox(8,
                new Label("Text (opțional):"), textField,
                new Label("BPM (opțional):"), bpmField,
                new Label("Poziționare:"), placementBox
        );
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        String text = textField.getText().isBlank() ? null : textField.getText();
        String bpmStr = bpmField.getText().isBlank() ? null : bpmField.getText();
        TempoPlacement placement = placementBox.getValue().equals("Below")
                ? TempoPlacement.BELOW : TempoPlacement.ABOVE;

        try {
            Integer bpm = bpmStr != null ? Integer.parseInt(bpmStr) : null;
            if (text == null && bpm == null) {
                m.removeTempo();
            } else {
                Tempo tempo = new Tempo(text, bpm, placement);
                m.setTempo(tempo);
            }
            applyScoreToMidi(score);
            drawStaff();
        } catch (NumberFormatException ex) {
            statusBar.setText("⚠️ BPM invalid (trebuie număr)");
        }
    }

    private void showTextDialog() {
        Measure m = editorState.getSelectedMeasure();
        if (m == null) {
            statusBar.setText("⚠️ Selectează mai întâi o măsură (click pe spațiul gol din ea)");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Text Expression");
        dialog.setHeaderText("Adaugă o expresie text pe măsura selectată");

        TextField textField = new TextField();
        textField.setPromptText("dolce, cantabile, ...");

        ComboBox<String> placementBox = new ComboBox<>();
        placementBox.getItems().addAll("Above", "Below");
        placementBox.setValue("Above");

        VBox content = new VBox(8,
                new Label("Text:"), textField,
                new Label("Poziționare:"), placementBox
        );
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        String text = textField.getText().isBlank() ? null : textField.getText();
        if (text == null) {
            statusBar.setText("⚠️ Text gol");
            return;
        }
        TextPlacement placement = placementBox.getValue().equals("Below")
                ? TextPlacement.BELOW : TextPlacement.ABOVE;

        try {
            m.addTextExpression(new TextExpression(text, placement));
            applyScoreToMidi(score);
            drawStaff();
        } catch (IllegalStateException ex) {
            statusBar.setText("⚠️ Expresia există deja");
        }
    }


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


    private void handleCanvasClick(MouseEvent event) {
        double mx = event.getX();
        double my = event.getY();

        RenderedLayout layout = layoutRenderer.getLastLayout();

        if (editorState.getMode() == EditMode.TIE) {
            Note hit = HitTester.findNoteAt(layout, mx, my);
            if (hit == null) {
                statusBar.setText("⚠️ Click pe o notă");
                return;
            }
            if (hit.isRest()) {
                statusBar.setText("⚠️ Tie nu se aplică pe pauze");
                return;
            }

            Note pending = editorState.getPendingTieStartNote();
            if (pending == null) {
                editorState.setPendingTieStartNote(hit);
                statusBar.setText("🎵 Nota de start setată. Click pe nota de end.");
                drawStaff();
                return;
            }

            if (hit == pending) {
                editorState.clearPendingConnections();
                statusBar.setText("⚠️ Anulat (aceeași notă)");
                drawStaff();
                return;
            }

            applyTieBetween(pending, hit);
            editorState.clearPendingConnections();
            applyScoreToMidi(score);
            drawStaff();
            return;
        }

        if (editorState.getMode() == EditMode.SLUR) {
            Note hit = HitTester.findNoteAt(layout, mx, my);
            if (hit == null) {
                statusBar.setText("⚠️ Click pe o notă");
                return;
            }
            if (hit.isRest()) {
                statusBar.setText("⚠️ Slur nu se aplică pe pauze");
                return;
            }

            Note pending = editorState.getPendingSlurStartNote();
            if (pending == null) {
                editorState.setPendingSlurStartNote(hit);
                statusBar.setText("🎵 Nota de start setată. Click pe nota de end.");
                drawStaff();
                return;
            }

            if (hit == pending) {
                editorState.clearPendingConnections();
                statusBar.setText("⚠️ Anulat (aceeași notă)");
                drawStaff();
                return;
            }

            applySlurBetween(pending, hit);
            editorState.clearPendingConnections();
            applyScoreToMidi(score);
            drawStaff();
            return;
        }

        if (editorState.getMode().isHairpinMode()) {
            Note hit = HitTester.findNoteAt(layout, mx, my);
            if (hit == null) {
                statusBar.setText("⚠️ Click pe o notă");
                return;
            }

            Note pending = editorState.getPendingHairpinStartNote();
            if (pending == null) {
                editorState.setPendingHairpinStartNote(hit);
                statusBar.setText("🎵 Nota de start setată. Click pe nota de end.");
                drawStaff();
                return;
            }

            if (hit == pending) {
                editorState.clearPendingHairpin();
                statusBar.setText("⚠️ Anulat (aceeași notă)");
                drawStaff();
                return;
            }

            HairpinType type = editorState.getMode() == EditMode.HAIRPIN_CRESCENDO
                    ? HairpinType.CRESCENDO : HairpinType.DECRESCENDO;

            try {
                Measure measure = findMeasureContaining(pending);
                if (measure == null) return;
                measure.addHairpin(new Hairpin(pending, hit, type, HairpinPlacement.BELOW));
                editorState.clearPendingHairpin();
                applyScoreToMidi(score);
                statusBar.setText("✅ Hairpin adăugat");
            } catch (IllegalStateException ex) {
                statusBar.setText("⚠️ " + ex.getMessage());
                editorState.clearPendingHairpin();
            }
            drawStaff();
            return;
        }

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

        if (editorState.getMode().isDynamicMode()) {
            Note hit = HitTester.findNoteAt(layout, mx, my);
            if (hit == null) {
                statusBar.setText("⚠️ Click pe o notă pentru a aplica dinamica");
                return;
            }
            DynamicType target = switch (editorState.getMode()) {
                case DYNAMIC_PPP -> DynamicType.PPP;
                case DYNAMIC_PP -> DynamicType.PP;
                case DYNAMIC_P -> DynamicType.P;
                case DYNAMIC_MP -> DynamicType.MP;
                case DYNAMIC_MF -> DynamicType.MF;
                case DYNAMIC_F -> DynamicType.F;
                case DYNAMIC_FF -> DynamicType.FF;
                case DYNAMIC_FFF -> DynamicType.FFF;
                default -> null;
            };
            if (target != null) {
                applyDynamicToNote(hit, target);
                applyScoreToMidi(score);
                drawStaff();
            }
            return;
        }

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
        if (event.getCode() == KeyCode.ESCAPE) {
            editorState.clearPendingHairpin();
            editorState.clearPendingConnections();
            drawStaff();
            return;
        }
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

    private void applyArticulationToNote(Note note, ArticulationType type) {
        if (note.isRest()) {
            statusBar.setText("⚠️ Nu poți aplica articulație pe o pauză");
            return;
        }
        Measure targetMeasure = findMeasureContaining(note);
        if (targetMeasure == null) return;

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

    private void applyDynamicToNote(Note note, DynamicType target) {
        if (note.isRest()) {
            statusBar.setText("⚠️ Nu poți aplica dinamică pe o pauză");
            return;
        }
        Measure targetMeasure = findMeasureContaining(note);
        if (targetMeasure == null) return;

        Dynamic existing = null;
        for (Dynamic d : targetMeasure.dynamics()) {
            if (d.note() == note) {
                existing = d;
                break;
            }
        }

        if (existing != null) {
            if (existing.type() == target) {
                targetMeasure.removeDynamic(existing);
            } else {
                targetMeasure.removeDynamic(existing);
                targetMeasure.addDynamic(new Dynamic(note, target));
            }
        } else {
            targetMeasure.addDynamic(new Dynamic(note, target));
        }
    }

    private void applyTieBetween(Note first, Note second) {
        if (first.isRest() || second.isRest()) {
            statusBar.setText("⚠️ Tie nu se aplică pe pauze");
            return;
        }
        if (!first.pitch().equals(second.pitch())) {
            statusBar.setText("⚠️ Tie necesită același pitch");
            return;
        }

        int idxFirst = findGlobalIndex(first);
        int idxSecond = findGlobalIndex(second);
        if (idxFirst == -1 || idxSecond == -1) return;

        if (idxFirst > idxSecond) {
            Note tmp = first;
            first = second;
            second = tmp;
        }

        if (first.tieStart() && second.tieEnd()) {
            first.setTieStart(false);
            second.setTieEnd(false);
            statusBar.setText("✅ Tie șters");
            return;
        }

        first.setTieStart(true);
        second.setTieEnd(true);
        statusBar.setText("✅ Tie adăugat");
    }

    private void applySlurBetween(Note first, Note second) {
        int idxFirst = findGlobalIndex(first);
        int idxSecond = findGlobalIndex(second);
        if (idxFirst == -1 || idxSecond == -1) return;

        if (idxFirst > idxSecond) {
            Note tmp = first;
            first = second;
            second = tmp;
            int tmpIdx = idxFirst;
            idxFirst = idxSecond;
            idxSecond = tmpIdx;
        }

        List<Note> between = new ArrayList<>();
        int counter = 0;
        for (Measure m : score.measures()) {
            for (Note n : m.notes()) {
                if (counter >= idxFirst && counter <= idxSecond) {
                    if (!n.isRest()) between.add(n);
                }
                counter++;
            }
        }

        if (between.size() < 2) {
            statusBar.setText("⚠️ Slur necesită cel puțin 2 note non-pauză");
            return;
        }

        Slur toRemove = null;
        Measure slurMeasure = null;
        for (Measure m : score.measures()) {
            for (Slur s : m.slurs()) {
                if (s.notes().size() == between.size()) {
                    boolean same = true;
                    for (int i = 0; i < between.size(); i++) {
                        if (s.notes().get(i) != between.get(i)) {
                            same = false;
                            break;
                        }
                    }
                    if (same) {
                        toRemove = s;
                        slurMeasure = m;
                        break;
                    }
                }
            }
            if (toRemove != null) break;
        }

        if (toRemove != null) {
            slurMeasure.removeSlur(toRemove);
            statusBar.setText("✅ Slur șters");
        } else {
            Measure targetMeasure = findMeasureContaining(between.get(0));
            if (targetMeasure == null) {
                statusBar.setText("⚠️ Nu am găsit măsura");
                return;
            }
            try {
                targetMeasure.addSlur(new Slur(between));
                statusBar.setText("✅ Slur adăugat");
            } catch (IllegalStateException ex) {
                statusBar.setText("⚠️ " + ex.getMessage());
            }
        }
    }

    private int findGlobalIndex(Note note) {
        int counter = 0;
        for (Measure m : score.measures()) {
            for (Note n : m.notes()) {
                if (n == note) return counter;
                counter++;
            }
        }
        return -1;
    }

    private Measure findMeasureContaining(Note note) {
        for (Measure m : score.measures()) {
            if (m.notes().contains(note)) return m;
        }
        return null;
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
        editorState.clearPendingHairpin();
        editorState.clearPendingConnections();

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
        if (statusBar == null) return;

        String name = currentFile != null
                ? currentFile.getFileName().toString()
                : "untitled";
        String sel = editorState.getSelectedMeasure() != null
                ? "  |  Măsură selectată"
                : editorState.getSelectedNote() != null
                ? "  |  Notă selectată"
                : "";
        String pendingInfo = "";
        if (editorState.getPendingHairpinStartNote() != null) {
            pendingInfo = "  |  Hairpin: aștept nota de end (ESC anulează)";
        } else if (editorState.getPendingTieStartNote() != null) {
            pendingInfo = "  |  Tie: aștept nota de end (ESC anulează)";
        } else if (editorState.getPendingSlurStartNote() != null) {
            pendingInfo = "  |  Slur: aștept nota de end (ESC anulează)";
        }
        statusBar.setText("📄 " + name
                + "  |  " + score.measures().size() + " măsuri"
                + "  |  Mod: " + editorState.getMode()
                + sel
                + pendingInfo);
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
        if (canvas == null) return;

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

        RenderedLayout layout = layoutRenderer.getLastLayout();

        if (editorState.getSelectedMeasure() != null) {
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

        if (editorState.getPendingHairpinStartNote() != null) {
            drawNoteRing(gc, layout, editorState.getPendingHairpinStartNote(), Color.LIMEGREEN);
        }
        if (editorState.getPendingTieStartNote() != null) {
            drawNoteRing(gc, layout, editorState.getPendingTieStartNote(), Color.ORANGE);
        }
        if (editorState.getPendingSlurStartNote() != null) {
            drawNoteRing(gc, layout, editorState.getPendingSlurStartNote(), Color.MAGENTA);
        }

        drawHoverGhost(gc);

        updateStatusBar();
    }

    private void drawNoteRing(GraphicsContext gc, RenderedLayout layout, Note note, Color color) {
        for (RenderedLayout.NoteHitBox nb : layout.notes()) {
            if (nb.note() == note) {
                gc.setStroke(color);
                gc.setLineWidth(3.0);
                gc.strokeOval(nb.centerX() - nb.radius(),
                        nb.centerY() - nb.radius(),
                        nb.radius() * 2, nb.radius() * 2);
                break;
            }
        }
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