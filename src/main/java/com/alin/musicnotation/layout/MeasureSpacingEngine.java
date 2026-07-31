package com.alin.musicnotation.layout;

import com.alin.musicnotation.model.Duration;
import com.alin.musicnotation.model.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple engine for computing horizontal positions (X coordinates) of notes
 * and rests based solely on their duration.
 *
 * <p>This is the foundation of the layout engine.
 * Future milestones may introduce:
 * <ul>
 *   <li>optical spacing (based on notehead shape)</li>
 *   <li>accidental spacing</li>
 *   <li>augmentation dot spacing</li>
 *   <li>lyric spacing</li>
 *   <li>measure justification</li>
 * </ul>
 *
 * <p>This engine does NOT:
 * <ul>
 *   <li>know about clefs, key signatures, or time signatures</li>
 *   <li>calculate Y positions</li>
 *   <li>depend on any renderer</li>
 *   <li>duplicate logic from MusicNotationApp</li>
 * </ul>
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
     * @param startX  the starting X position (left edge of the measure content)
     * @return a {@code MeasureSpacingResult} containing the layouts and final cursor X
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

    /**
     * Returns the horizontal spacing for a single duration.
     * Currently duration-based only.
     */
    private double spacing(Duration duration) {
        return noteSpacingPerQuarter * duration.toQuarterNoteFraction();
    }
}