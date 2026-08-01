package com.alin.musicnotation.layout;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Score;
import com.alin.musicnotation.renderer.MeasureRenderData;
import com.alin.musicnotation.renderer.RenderConfig;
import com.alin.musicnotation.renderer.RenderingState;
import com.alin.musicnotation.renderer.RenderingStateMachine;

import java.util.ArrayList;
import java.util.List;

public final class ScoreLayoutEngine {

    private final MeasureSpacingEngine spacingEngine;
    private final PageLayoutEngine pageLayoutEngine;
    private final RenderConfig config;

    private static final double START_PADDING = 15;
    private static final double END_PADDING = 45;
    private static final double PRE_NOTE_PADDING = 6;
    private static final double CLEF_WIDTH = 44;
    private static final double KEY_WIDTH = 40;
    private static final double TIME_WIDTH = 45;

    public ScoreLayoutEngine(MeasureSpacingEngine spacingEngine,
                             PageLayoutEngine pageLayoutEngine,
                             RenderConfig config) {
        this.spacingEngine = spacingEngine;
        this.pageLayoutEngine = pageLayoutEngine;
        this.config = config;
    }

    public PageLayoutResult layout(Score score, double pageWidth, double leftMargin, double rightMargin) {
        List<Measure> allMeasures = score.measures();

        List<MeasureLayout> measureLayouts = new ArrayList<>();
        List<MeasureSpacingResult> spacingResults = new ArrayList<>();
        List<Double> extraWidths = new ArrayList<>();

        RenderingState tempState = new RenderingState(
                score.initialClef(),
                score.keySignature(),
                score.initialTimeSignature()
        );

        for (Measure measure : allMeasures) {
            MeasureSpacingResult spacingResult = spacingEngine.computeSpacing(measure.notes(), 0);
            spacingResults.add(spacingResult);
            double naturalWidth = spacingResult.finalCursorX();

            double extraWidth = 0;
            if (measure.clef() != null && measure.clef() != tempState.currentClef()) {
                extraWidth += CLEF_WIDTH;
            }
            if (measure.keySignature() != null && !measure.keySignature().equals(tempState.currentKey())) {
                extraWidth += KEY_WIDTH;
            }
            if (measure.timeSignature() != null && !measure.timeSignature().equals(tempState.currentTime())) {
                extraWidth += TIME_WIDTH;
            }
            extraWidths.add(extraWidth);

            double totalWidth = START_PADDING + extraWidth + PRE_NOTE_PADDING + naturalWidth + END_PADDING;
            measureLayouts.add(new MeasureLayout(
                    allMeasures.indexOf(measure),
                    naturalWidth,
                    totalWidth,
                    1.0
            ));

            tempState = RenderingStateMachine.advance(tempState, measure);
        }

        double availableWidth = pageWidth - leftMargin - rightMargin;
        if (availableWidth <= 0) availableWidth = 200;

        List<SystemLayout> systems = SystemBreaker.computeSystems(measureLayouts, availableWidth);
        List<SystemLayout> justifiedSystems = MeasureJustificationEngine.justifySystems(systems, availableWidth);

        PageLayout pageLayout = pageLayoutEngine.computeLayout(justifiedSystems);

        List<MeasureRenderData> renderDataList = new ArrayList<>();
        RenderingState state = new RenderingState(
                score.initialClef(),
                score.keySignature(),
                score.initialTimeSignature()
        );

        int totalMeasures = allMeasures.size();

        for (SystemLayout system : justifiedSystems) {
            for (MeasureLayout ml : system.measureLayouts()) {
                int idx = ml.measureIndex();
                Measure measure = allMeasures.get(idx);
                MeasureSpacingResult spacingResult = spacingResults.get(idx);
                double extraWidth = extraWidths.get(idx);

                double fixedWidth = START_PADDING + extraWidth + PRE_NOTE_PADDING + END_PADDING;
                double justifiedTotalWidth = fixedWidth + (ml.naturalWidth() * ml.justificationScale());

                boolean isLastMeasure = (idx == totalMeasures - 1);

                renderDataList.add(new MeasureRenderData(
                        idx,
                        measure,
                        spacingResult,
                        extraWidth,
                        justifiedTotalWidth,
                        ml.justificationScale(),
                        isLastMeasure
                ));

                state = RenderingStateMachine.advance(state, measure);
            }
        }

        return new PageLayoutResult(pageLayout, renderDataList);
    }
}