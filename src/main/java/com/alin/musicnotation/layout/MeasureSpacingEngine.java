package com.alin.musicnotation.layout;

import com.alin.musicnotation.model.Duration;
import com.alin.musicnotation.model.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes natural horizontal spacing for notes within a measure.
 * Spacing is based solely on note duration, with no optical adjustments.
 */
public final class MeasureSpacingEngine {

    private final double noteSpacingPerQuarter;

    public MeasureSpacingEngine(double noteSpacingPerQuarter) {
        this.noteSpacingPerQuarter = noteSpacingPerQuarter;
    }

    /**
     * Computes horizontal positions for all notes in a measure.
     *
     * @param notes   the notes to lay out
     * @param startX  the starting X position
     * @return a MeasureSpacingResult containing layouts and final cursor X
     */
    public MeasureSpacingResult computeSpacing(List<Note> notes, double startX) {
        List<HorizontalLayout> layouts = new ArrayList<>();
        double x = startX;
        int currentSixteenths = 0;

        for (Note note : notes) {
            double spacing = spacing(note.duration());
            layouts.add(new HorizontalLayout(note, x, currentSixteenths));
            x += spacing;
            currentSixteenths += note.duration().sixteenths();
        }

        return new MeasureSpacingResult(layouts, x);
    }

    private double spacing(Duration duration) {
        return noteSpacingPerQuarter * duration.toQuarterNoteFraction();
    }
}