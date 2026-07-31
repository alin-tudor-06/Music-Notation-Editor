package com.alin.musicnotation.layout;

import java.util.List;

/**
 * Layout information for a system (staff line), containing a list of measure layouts.
 * This is a pure layout object – it does not reference the musical model.
 */
public record SystemLayout(
        List<MeasureLayout> measureLayouts
) {
}