package com.alin.musicnotation.layout;

public record SystemPlacement(
        SystemLayout systemLayout,
        double x,
        double y,
        double availableWidth
) {
}