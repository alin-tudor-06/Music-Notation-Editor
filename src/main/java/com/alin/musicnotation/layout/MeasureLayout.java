package com.alin.musicnotation.layout;

/**
 * Layout information for a single measure.
 * This is a pure layout object – it does not reference the musical model.
 *
 * <p>Currently contains only the natural width computed by the spacing engine.
 * Future milestones may add:
 * <ul>
 *   <li>justified width</li>
 *   <li>horizontal positions of notes</li>
 *   <li>bounding box</li>
 *   <li>system index</li>
 * </ul>
 */
public record MeasureLayout(
        double naturalWidth
) {
    /**
     * Creates a MeasureLayout with the given natural width.
     *
     * @param naturalWidth the natural width of the measure in pixels (must be >= 0)
     * @throws IllegalArgumentException if naturalWidth is negative
     */
    public MeasureLayout {
        if (naturalWidth < 0) {
            throw new IllegalArgumentException("Natural width cannot be negative: " + naturalWidth);
        }
    }
}