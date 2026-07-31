package com.alin.musicnotation.renderer;

import com.alin.musicnotation.layout.SystemPlacement;
import com.alin.musicnotation.model.Measure;
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

    private static final double TIME_SIGNATURE_FONT_SIZE_FACTOR = 2.2; // trebuie să corespundă cu cel din TimeSignatureRenderer

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
                                 RenderingState state) {
        double systemX = placement.x();
        double systemY = placement.y();

        // Desenăm cheia, armura și timpul, obținând poziția de start pentru prima măsură
        double startX = drawInitialClefKeyTime(gc, systemX, systemY, state);

        double currentX = startX;
        double lastBarX = systemX;
        RenderingState currentState = state;

        for (var measureLayout : placement.systemLayout().measureLayouts()) {
            int index = measureLayout.measureIndex();
            MeasureRenderData data = renderDataMap.get(index);
            if (data == null) {
                throw new IllegalStateException("No render data for measure index: " + index);
            }

            currentState = measureRenderer.render(gc, data, currentX, systemY, currentState);

            double measureWidth = data.spacingResult().finalCursorX();
            currentX += measureWidth + config.measure().endPadding() + config.measure().startPadding();
            lastBarX = currentX;
        }

        // Desenăm liniile portativului până la sfârșitul ultimei bariere
        drawStaffLines(gc, systemX, systemY, lastBarX - systemX);

        return currentState;
    }

    private double drawInitialClefKeyTime(GraphicsContext gc, double x, double firstLineY, RenderingState state) {
        double staffHeight = StaffGeometry.getStaffHeight(config.staff().lineSpacing());
        double lineSpacing = config.staff().lineSpacing();

        // Cheie
        clefRenderer.drawClef(gc, x + 6, firstLineY, staffHeight, lineSpacing, state.currentClef());

        // Armură
        double currentX = x + 6 + 44;
        double keyWidth = keySignatureRenderer.drawInitial(gc, currentX, firstLineY,
                state.currentKey(), state.currentClef(), lineSpacing);
        currentX += keyWidth;

        // Timp – calculăm lățimea folosind același font ca în TimeSignatureRenderer
        Font font = Font.font("Serif", FontWeight.BOLD, lineSpacing * TIME_SIGNATURE_FONT_SIZE_FACTOR);
        double timeSigX = currentX + 10;
        timeSignatureRenderer.drawInitial(gc, timeSigX, firstLineY, staffHeight, lineSpacing, state.currentTime());

        // Lățimea timpului
        double timeSigWidth = timeSignatureRenderer.calculateWidth(font, state.currentTime());

        // Prima măsură începe după timp + padding
        return timeSigX + timeSigWidth + config.measure().startPadding();
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