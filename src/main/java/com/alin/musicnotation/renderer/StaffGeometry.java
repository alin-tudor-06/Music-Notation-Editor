package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.Duration;

public final class StaffGeometry {

    private StaffGeometry() {
        // Utility class - no instantiation
    }

    public static double getRestY(double firstLineY, double staffLineSpacing, double restHeight,
                                  Duration duration) {
        double line3 = firstLineY + 2 * staffLineSpacing;
        double line4 = firstLineY + 3 * staffLineSpacing;
        if (duration == Duration.WHOLE) {
            return line4 - restHeight;
        }
        if (duration == Duration.HALF) {
            return line3 + restHeight;
        }
        return line3 + staffLineSpacing * 0.1;
    }

    public static double getLineY(double firstLineY, double staffLineSpacing, int lineIndex) {
        return firstLineY + lineIndex * staffLineSpacing;
    }

    public static double getStaffHeight(double staffLineSpacing) {
        return 4 * staffLineSpacing;
    }
}