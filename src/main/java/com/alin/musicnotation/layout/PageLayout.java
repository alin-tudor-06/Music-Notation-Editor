package com.alin.musicnotation.layout;

import java.util.List;

/**
 * Complete layout of a single page.
 * Contains the positions of all systems on the page.
 */
public record PageLayout(
        List<SystemPlacement> systemPlacements
) {
}