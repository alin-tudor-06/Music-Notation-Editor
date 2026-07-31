package com.alin.musicnotation.renderer;

public record RenderConfig(
        StaffConfig staff,
        NoteConfig note,
        BeamConfig beam,
        TupletConfig tuplet,
        SlurConfig slur,
        ArticulationConfig articulation,
        DynamicConfig dynamic,
        MeasureLayoutConfig measure,
        SpacingConfig spacing,
        ClefChangeConfig clefChange
) {

    public static RenderConfig defaultConfig() {
        double s = 16;
        return new RenderConfig(
                new StaffConfig(s, 0, 8, 4),
                new NoteConfig(s * 1.4, s, 2, s * 3.5, 10, 14, 6, 6, s * 0.8, s * 0.6, 2.0),
                new BeamConfig(5, 7, s * 1.4 * 0.6, 0.25),
                new TupletConfig(s * 2.0, s * 0.5, 0.9),
                new SlurConfig(s * 1.2),
                new ArticulationConfig(1.0, s * 0.8),
                new DynamicConfig(0.8, s * 1.8),
                new MeasureLayoutConfig(12, 24, 14),
                new SpacingConfig(8, 14),
                new ClefChangeConfig(6, 15, 40)
        );
    }

    public record StaffConfig(double lineSpacing, int topLine, int bottomLine, int middleLine) {}
    public record NoteConfig(double headWidth, double headHeight, double outlineWidth,
                             double stemLength, double flagWidth, double flagHeight,
                             double flagSpacing, double ledgerExtension,
                             double restWidth, double restHeight, double eighthRestSlant) {}
    public record BeamConfig(double thickness, double spacing, double hookLength, double maxSlope) {}
    public record TupletConfig(double offset, double bracketHeight, double numberFontSize) {}
    public record SlurConfig(double offset) {}
    public record ArticulationConfig(double fontSizeFactor, double offset) {}
    public record DynamicConfig(double fontSizeFactor, double offset) {}
    public record MeasureLayoutConfig(double endPadding, double startPadding, double repeatGap) {}
    public record SpacingConfig(double beamableExtra, double accidentalExtra) {}
    public record ClefChangeConfig(double leftPadding, double afterSpacing, double changeSpacing) {}
}