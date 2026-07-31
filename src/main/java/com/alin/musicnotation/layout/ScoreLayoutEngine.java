package com.alin.musicnotation.layout;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Score;
import com.alin.musicnotation.renderer.MeasureRenderData;

import java.util.ArrayList;
import java.util.List;

public final class ScoreLayoutEngine {

    private final MeasureSpacingEngine spacingEngine;
    private final PageLayoutEngine pageLayoutEngine;

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
            measureLayouts.add(new MeasureLayout(i, naturalWidth));
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