package com.alin.musicnotation.renderer;

import com.alin.musicnotation.layout.SystemPlacement;
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
                                 boolean isFirstSystem) {

        double systemX = placement.x();
        double systemY = placement.y();

        double staffHeight = StaffGeometry.getStaffHeight(config.staff().lineSpacing());
        double lineSpacing = config.staff().lineSpacing();
        double startPadding = config.measure().startPadding();

        // Determinăm starea care va fi afișată la începutul sistemului
        RenderingState displayState = state;

        if (!isFirstSystem && !placement.systemLayout().measureLayouts().isEmpty()) {
            int firstIndex = placement.systemLayout().measureLayouts().get(0).measureIndex();
            MeasureRenderData firstData = renderDataMap.get(firstIndex);
            if (firstData != null) {
                var firstMeasure = firstData.measure();

                var displayClef = state.currentClef();
                var displayKey = state.currentKey();
                var displayTime = state.currentTime();

                if (firstMeasure.clef() != null && firstMeasure.clef() != state.currentClef()) {
                    displayClef = firstMeasure.clef();
                }
                if (firstMeasure.keySignature() != null && !firstMeasure.keySignature().equals(state.currentKey())) {
                    displayKey = firstMeasure.keySignature();
                }
                if (firstMeasure.timeSignature() != null && !firstMeasure.timeSignature().equals(state.currentTime())) {
                    displayTime = firstMeasure.timeSignature();
                }

                displayState = new RenderingState(displayClef, displayKey, displayTime);
            }
        }

        // Desenăm la începutul sistemului în ordinea: CLEF → KEY → TIME
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

        RenderingState currentState = displayState;
        double lastBarX = systemX;

        for (var measureLayout : placement.systemLayout().measureLayouts()) {
            int index = measureLayout.measureIndex();
            MeasureRenderData data = renderDataMap.get(index);
            if (data == null) {
                throw new IllegalStateException("No render data for measure index: " + index);
            }

            currentState = measureRenderer.render(gc, data, currentX, systemY, currentState);
            double measureWidth = data.justifiedWidth();
            currentX += measureWidth;
            lastBarX = currentX;
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