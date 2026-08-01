package com.alin.musicnotation.layout;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Score;
import com.alin.musicnotation.renderer.MeasureRenderData;

import java.util.ArrayList;
import java.util.List;

public final class ScoreLayoutEngine {

    private final MeasureSpacingEngine spacingEngine;
    private final PageLayoutEngine pageLayoutEngine;

    // Constante aliniate cu RenderConfig
    private static final double START_PADDING = 15;
    private static final double END_PADDING = 30;
    private static final double PRE_NOTE_PADDING = 6;
    private static final double CLEF_WIDTH = 44;
    private static final double KEY_WIDTH = 40;
    private static final double TIME_WIDTH = 45;

    public ScoreLayoutEngine(MeasureSpacingEngine spacingEngine, PageLayoutEngine pageLayoutEngine) {
        this.spacingEngine = spacingEngine;
        this.pageLayoutEngine = pageLayoutEngine;
    }

    public PageLayoutResult layout(Score score, double pageWidth, double leftMargin, double rightMargin) {
        List<Measure> allMeasures = score.measures();

        List<MeasureLayout> measureLayouts = new ArrayList<>();
        List<MeasureRenderData> renderDataList = new ArrayList<>();

        for (int i = 0; i < allMeasures.size(); i++) {
            Measure measure = allMeasures.get(i);
            MeasureSpacingResult spacingResult = spacingEngine.computeSpacing(measure.notes(), 0);
            double naturalWidth = spacingResult.finalCursorX();

            double extraWidth = 0;
            if (measure.clef() != null) {
                extraWidth += CLEF_WIDTH;
            }
            if (measure.keySignature() != null) {
                extraWidth += KEY_WIDTH;
            }
            if (measure.timeSignature() != null) {
                extraWidth += TIME_WIDTH;
            }

            // totalWidth = startPadding + extraWidth + preNotePadding + naturalWidth + endPadding
            double totalWidth = START_PADDING + extraWidth + PRE_NOTE_PADDING + naturalWidth + END_PADDING;
            measureLayouts.add(new MeasureLayout(i, naturalWidth, totalWidth));
            renderDataList.add(new MeasureRenderData(i, measure, spacingResult));
        }

        double availableWidth = pageWidth - leftMargin - rightMargin;
        if (availableWidth <= 0) {
            availableWidth = 200;
        }
        List<SystemLayout> systems = SystemBreaker.computeSystems(measureLayouts, availableWidth);

        PageLayout pageLayout = pageLayoutEngine.computeLayout(systems);
        return new PageLayoutResult(pageLayout, renderDataList);
    }
}