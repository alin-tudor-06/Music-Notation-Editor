package com.alin.musicnotation.layout;

import java.util.ArrayList;
import java.util.List;

public final class PageLayoutEngine {

    private final double pageWidth;
    private final double leftMargin;
    private final double rightMargin;
    private final double topMargin;
    private final double systemSpacing;

    public PageLayoutEngine(
            double pageWidth,
            double leftMargin,
            double rightMargin,
            double topMargin,
            double systemSpacing) {

        if (pageWidth <= 0) {
            throw new IllegalArgumentException("Page width must be positive: " + pageWidth);
        }
        if (leftMargin < 0) {
            throw new IllegalArgumentException("Left margin cannot be negative: " + leftMargin);
        }
        if (rightMargin < 0) {
            throw new IllegalArgumentException("Right margin cannot be negative: " + rightMargin);
        }
        if (topMargin < 0) {
            throw new IllegalArgumentException("Top margin cannot be negative: " + topMargin);
        }
        if (systemSpacing < 0) {
            throw new IllegalArgumentException("System spacing cannot be negative: " + systemSpacing);
        }

        if (leftMargin + rightMargin >= pageWidth) {
            throw new IllegalArgumentException(
                    "Margins exceed page width: left=" + leftMargin +
                            ", right=" + rightMargin + ", pageWidth=" + pageWidth
            );
        }

        this.pageWidth = pageWidth;
        this.leftMargin = leftMargin;
        this.rightMargin = rightMargin;
        this.topMargin = topMargin;
        this.systemSpacing = systemSpacing;
    }

    public PageLayout computeLayout(List<SystemLayout> systemLayouts) {
        if (systemLayouts == null || systemLayouts.isEmpty()) {
            return new PageLayout(List.of());
        }

        double availableWidth = pageWidth - leftMargin - rightMargin;
        List<SystemPlacement> placements = new ArrayList<>();
        double y = topMargin;

        for (SystemLayout systemLayout : systemLayouts) {
            placements.add(new SystemPlacement(systemLayout, leftMargin, y, availableWidth));
            y += systemSpacing;
        }

        return new PageLayout(placements);
    }
}