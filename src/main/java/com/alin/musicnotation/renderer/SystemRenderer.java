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
    private static final double SYSTEM_END_PADDING = 15; // Padding la sfârșitul sistemului

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

        // Desenăm cheia, gama și timpul la începutul fiecărui sistem
        double currentX = systemX + 6;
        clefRenderer.drawClef(gc, currentX, systemY, staffHeight, lineSpacing, state.currentClef());
        currentX += 44;

        double keyWidth = keySignatureRenderer.drawInitial(gc, currentX, systemY,
                state.currentKey(), state.currentClef(), lineSpacing);
        currentX += keyWidth;

        double timeSigX = currentX + 10;
        timeSignatureRenderer.drawInitial(gc, timeSigX, systemY, staffHeight, lineSpacing, state.currentTime());
        Font font = Font.font("Serif", FontWeight.BOLD, lineSpacing * TIME_SIGNATURE_FONT_SIZE_FACTOR);
        double timeSigWidth = timeSignatureRenderer.calculateWidth(font, state.currentTime());
        currentX = timeSigX + timeSigWidth + startPadding;

        RenderingState currentState = state;
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

        // Adăugăm un mic padding la sfârșitul sistemului înainte de a desena liniile
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