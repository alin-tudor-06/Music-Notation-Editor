package com.alin.musicnotation.model.clef;

import com.alin.musicnotation.model.Pitch;

/**
 * Supported clef types.
 * Each clef defines:
 * - referenceLineIndex: the staff line used to center the clef symbol visually.
 * - referencePitch: the pitch that maps to the top line of the staff (for position calculation).
 */
public enum ClefType {
    // Treble clef (G clef)
    // - referenceLineIndex = 2 (centers the symbol on the G4 line)
    // - referencePitch = F5 (top line of the staff)
    TREBLE(3, Pitch.of("F5")),

    // Bass clef (F clef)
    BASS(1, Pitch.of("A3")),

    // Alto clef (C clef) – temporary symbol (uses treble glyph until SMuFL is loaded)
    ALTO(2, Pitch.of("G4")),

    // Tenor clef (C clef) – temporary symbol (uses treble glyph until SMuFL is loaded)
    TENOR(1, Pitch.of("E4"));

    private final int referenceLineIndex;
    private final Pitch referencePitch;

    ClefType(int referenceLineIndex, Pitch referencePitch) {
        this.referenceLineIndex = referenceLineIndex;
        this.referencePitch = referencePitch;
    }

    public int referenceLineIndex() {
        return referenceLineIndex;
    }

    public Pitch referencePitch() {
        return referencePitch;
    }

    /**
     * Unicode symbol for the clef.
     * TODO: Replace ALTO and TENOR with proper SMuFL glyphs when music font is loaded.
     */
    public String unicodeSymbol() {
        return switch (this) {
            case TREBLE, ALTO, TENOR -> "\uD834\uDD1E";
            case BASS -> "\uD834\uDD22";
        };
    }
}