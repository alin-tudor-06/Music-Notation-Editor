package com.alin.musicnotation.layout;

/**
 * Layout information for a single measure, containing only its natural width.
 * This is a pure layout object – it does not reference the musical model.
 */
public record MeasureLayout(
        double naturalWidth
) {
}