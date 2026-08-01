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
    ACCENT(0xE4A0),
    ACCENT_BELOW(0xE4A1),
    STACCATO(0xE4A2),
    STACCATO_BELOW(0xE4A3),
    TENUTO(0xE4A4),
    TENUTO_BELOW(0xE4A5),
    STACCATISSIMO(0xE4A6),
    STACCATISSIMO_BELOW(0xE4A7),
    MARCATO(0xE4AC),
    MARCATO_BELOW(0xE4AD),
    FERMATA(0xE4C0),
    FERMATA_BELOW(0xE4C1),

    // ===== Time Signature Digits =====
    TIME_SIG_0(0xE080),
    TIME_SIG_1(0xE081),
    TIME_SIG_2(0xE082),
    TIME_SIG_3(0xE083),
    TIME_SIG_4(0xE084),
    TIME_SIG_5(0xE085),
    TIME_SIG_6(0xE086),
    TIME_SIG_7(0xE087),
    TIME_SIG_8(0xE088),
    TIME_SIG_9(0xE089),

    // ===== Time Signature Symbols =====
    TIME_SIG_COMMON(0xE08A),
    TIME_SIG_CUT(0xE08B),

    // ===== Dynamics =====
    DYNAMIC_PIANO(0xE520),
    DYNAMIC_MEZZO(0xE521),
    DYNAMIC_FORTE(0xE522);

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

    /**
     * Maps a numeric character to its corresponding SMuFL time signature glyph.
     *
     * @param digit the character to map ('0' through '9')
     * @return the corresponding SmuflGlyph, or null if invalid
     */
    public static SmuflGlyph timeSignatureDigit(char digit) {
        return switch (digit) {
            case '0' -> TIME_SIG_0;
            case '1' -> TIME_SIG_1;
            case '2' -> TIME_SIG_2;
            case '3' -> TIME_SIG_3;
            case '4' -> TIME_SIG_4;
            case '5' -> TIME_SIG_5;
            case '6' -> TIME_SIG_6;
            case '7' -> TIME_SIG_7;
            case '8' -> TIME_SIG_8;
            case '9' -> TIME_SIG_9;
            default -> null;
        };
    }
}