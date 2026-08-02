package com.alin.musicnotation.musicxml.common;

import com.alin.musicnotation.model.clef.ClefType;

public final class MusicXmlClefMapper {

    private MusicXmlClefMapper() {}

    public static String getSign(ClefType clef) {
        return switch (clef) {
            case TREBLE -> "G";
            case BASS -> "F";
            case ALTO, TENOR -> "C";
        };
    }

    public static int getLine(ClefType clef) {
        return switch (clef) {
            case TREBLE -> 2;
            case BASS -> 4;
            case ALTO -> 3;
            case TENOR -> 4;
        };
    }

    public static ClefType fromSignAndLine(String sign, int line) {
        for (ClefType clef : ClefType.values()) {
            if (getSign(clef).equals(sign) && getLine(clef) == line) {
                return clef;
            }
        }
        throw new IllegalArgumentException("Unknown clef: sign=" + sign + ", line=" + line);
    }
}