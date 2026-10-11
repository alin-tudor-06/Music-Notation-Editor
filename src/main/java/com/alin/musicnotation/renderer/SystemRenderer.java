package com.alin.musicnotation.renderer;

import com.alin.musicnotation.editor.RenderedLayout;
import com.alin.musicnotation.layout.SystemPlacement;
import com.alin.musicnotation.model.KeySignature;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.TimeSignature;
import com.alin.musicnotation.model.clef.ClefType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Map;

public final class SystemRenderer {

    private final RenderConfig config;
    private final MeasureRenderer measureRenderer;
    private final ClefRenderer clefRenderer;
    private final KeySignatureRenderer keySignatureRenderer;
    private final TimeSignatureRenderer timeSignatureRenderer;

    private static final double TIME_SIGNATURE_FONT_SIZE_FACTOR = 2.2;
    private static final double SYSTEM_END_PADDING = 10;

    public SystemRenderer(RenderConfig config,
                          MeasureRenderer measureRenderer,
                          ClefRenderer clefRenderer,
                          KeySignatureRenderer keySignatureRenderer,
                          TimeSignatureRenderer timeSignatureRenderer) {
        this.config = config;
        this.measureRenderer = measureRenderer;
        this.clefRenderer = clefRenderer;
        this.keySignatureRenderer = keySignatureRenderer;
        this.timeSignatureRenderer = timeSignatureRenderer;
    }

    public RenderingState render(GraphicsContext gc,
                                 SystemPlacement placement,
                                 Map<Integer, MeasureRenderData> renderDataMap,
                                 RenderingState state,
                                 boolean isFirstSystem,
                                 Note highlightNote,
                                 RenderedLayout.Builder builder) {

        double systemX = placement.x();
        double systemY = placement.y();

        double staffHeight = StaffGeometry.getStaffHeight(config.staff().lineSpacing());
        double lineSpacing = config.staff().lineSpacing();
        double startPadding = config.measure().startPadding();

        // ─── Determinăm starea de afișat la începutul sistemului ───
        // Folosim suprascrierile primei măsuri (dacă are), altfel starea curentă.
        RenderingState displayState = state;

        if (!placement.systemLayout().measureLayouts().isEmpty()) {
            int firstIdx = placement.systemLayout().measureLayouts().get(0).measureIndex();
            MeasureRenderData firstData = renderDataMap.get(firstIdx);
            if (firstData != null) {
                var fm = firstData.measure();
                ClefType c = fm.clef() != null ? fm.clef() : state.currentClef();
                KeySignature k = fm.keySignature() != null ? fm.keySignature() : state.currentKey();
                TimeSignature t = fm.timeSignature() != null ? fm.timeSignature() : state.currentTime();
                displayState = new RenderingState(c, k, t);
            }
        }

        // ─── Desenăm CLEF → KEY → TIME la începutul sistemului ───
        double currentX = systemX + 6;
        clefRenderer.drawClef(gc, currentX, systemY, staffHeight, lineSpacing, displayState.currentClef());
        currentX += 44;

        double keyWidth = keySignatureRenderer.drawInitial(gc, currentX, systemY,
                displayState.currentKey(), displayState.currentClef(), lineSpacing);
        currentX += keyWidth;

        double timeSigX = currentX + 10;
        timeSignatureRenderer.drawInitial(gc, timeSigX, systemY, staffHeight, lineSpacing, displayState.currentTime());
        Font font = Font.font("Serif", FontWeight.BOLD, lineSpacing * TIME_SIGNATURE_FONT_SIZE_FACTOR);
        double timeSigWidth = timeSignatureRenderer.calculateWidth(font, displayState.currentTime());
        currentX = timeSigX + timeSigWidth + startPadding;

        RenderingState currentState = state;
        double lastBarX = systemX;
        boolean isFirstMeasureOfSystem = true;

        for (var measureLayout : placement.systemLayout().measureLayouts()) {
            int index = measureLayout.measureIndex();
            MeasureRenderData data = renderDataMap.get(index);
            if (data == null) {
                throw new IllegalStateException("No render data for measure index: " + index);
            }

            double measureWidth = data.justifiedWidth();

            // ─── BUG FIX 2: Clef corect pentru hit-box ───
            // Dacă măsura are clef propriu, folosim acela; altfel starea curentă.
            ClefType clefForMeasure = data.measure().clef() != null
                    ? data.measure().clef()
                    : currentState.currentClef();

            // Extindem hit box-ul cu 4 spații de portativ sus/jos pentru note cu linii suplimentare
            double extend = 4.0 * lineSpacing;
            builder.addMeasure(data.measure(), currentX, systemY - extend,
                    measureWidth, staffHeight + 2 * extend, systemY, lineSpacing,
                    clefForMeasure);

            currentState = measureRenderer.render(gc, data, currentX, systemY,
                    currentState, highlightNote, builder, isFirstMeasureOfSystem);
            currentX += measureWidth;
            lastBarX = currentX;
            isFirstMeasureOfSystem = false;
        }

        double systemEndX = lastBarX + SYSTEM_END_PADDING;
        drawStaffLines(gc, systemX, systemY, systemEndX - systemX);
        return currentState;
    }

    private void drawStaffLines(GraphicsContext gc, double x, double firstLineY, double width) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        double lineEndX = x + width;
        for (int i = 0; i < 5; i++) {
            double y = firstLineY + i * config.staff().lineSpacing();
            gc.strokeLine(x, y, lineEndX, y);
        }
    }
}