package com.alin.musicnotation.layout;

/**
 * Position of a single system on the page.
 * Contains the system layout and its coordinates.
 */
public record SystemPlacement(
        SystemLayout systemLayout,
        double x,
        double y,
        double availableWidth
) {
}