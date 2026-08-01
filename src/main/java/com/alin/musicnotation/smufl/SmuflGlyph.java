package com.alin.musicnotation.smufl;

/**
 * SMuFL glyph mapping used by the application.
 * <p>
 * Each enum constant stores the Unicode code point for a Bravura glyph.
 * Renderers obtain the glyph text through {@link #unicode()}.
 * The glyph strings are precomputed at enum initialization for efficiency.
 * </p>
 *
 * @see com.alin.musicnotation.smufl.BravuraFont
 */
public enum SmuflGlyph {

    // ===== Clefs =====
    TREBLE_CLEF(0xE050),
    BASS_CLEF(0xE062),
    ALTO_CLEF(0xE05C),
    TENOR_CLEF(0xE05C),

    // ===== Accidentals =====
    SHARP(0xE262),
    FLAT(0xE260),
    NATURAL(0xE261),

    // ===== Noteheads =====
    NOTEHEAD_BLACK(0xE0A4),
    NOTEHEAD_HALF(0xE0A3),
    NOTEHEAD_WHOLE(0xE0A2),

    // ===== Rests =====
    REST_WHOLE(0xE4E3),
    REST_HALF(0xE4E4),
    REST_QUARTER(0xE4E5),
    REST_EIGHTH(0xE4E6),
    REST_SIXTEENTH(0xE4E7),

    // ===== Flags =====
    FLAG_8TH_UP(0xE240),
    FLAG_8TH_DOWN(0xE241),
    FLAG_16TH_UP(0xE242),
    FLAG_16TH_DOWN(0xE243),

    // ===== Articulations =====
    FERMATA(0xE4C0),
    ACCENT(0xE4A0),
    STACCATO(0xE4A2),
    TENUTO(0xE4A1),
    MARCATO(0xE4A3),
    STACCATISSIMO(0xE4A4);

    private final int codePoint;
    private final String unicode;

    SmuflGlyph(int codePoint) {
        this.codePoint = codePoint;
        this.unicode = new String(Character.toChars(codePoint));
    }

    /**
     * Returns the SMuFL Unicode character for this glyph.
     * The string is precomputed and cached.
     *
     * @return the Unicode character as a String
     */
    public String unicode() {
        return unicode;
    }

    /**
     * Returns the Unicode code point for this glyph.
     *
     * @return the code point
     */
    public int codePoint() {
        return codePoint;
    }
}