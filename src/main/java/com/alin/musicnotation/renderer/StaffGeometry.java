package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.Duration;

public final class StaffGeometry {

    private StaffGeometry() {
        // Utility class - no instantiation
    }

    public static double getRestY(double firstLineY, double staffLineSpacing, double restHeight,
                                  Duration duration) {
        double line3 = firstLineY + 2 * staffLineSpacing;
        double line2 = firstLineY + staffLineSpacing;

        if (duration == Duration.WHOLE) {
            // Whole rest hangs below the fourth line (line index 4)
            return line2 + restHeight / 10;
        }

        if (duration == Duration.HALF) {
            // Half rest sits above the third line (line index 3)
            // The glyph is centered at (line3 - restHeight / 2)
            return line3 - restHeight / 10;
        }

        // Quarter, eighth, sixteenth rests are centered on the middle line (line 3)
        return line3 + staffLineSpacing * 0.1;
    }

    public static double getLineY(double firstLineY, double staffLineSpacing, int lineIndex) {
        return firstLineY + lineIndex * staffLineSpacing;
    }

    public static double getStaffHeight(double staffLineSpacing) {
        return 4 * staffLineSpacing;
    }
}