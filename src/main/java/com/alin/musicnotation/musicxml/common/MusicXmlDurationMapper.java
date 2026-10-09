package com.alin.musicnotation.musicxml.common;

import com.alin.musicnotation.model.Duration;
import com.alin.musicnotation.model.Tuplet;

public final class MusicXmlDurationMapper {

    private MusicXmlDurationMapper() {}

    /**
     * Durata de bază (fără tuplet) în divisions.
     */
    public static int getDurationInDivisions(Duration duration) {
        return switch (duration) {
            case WHOLE -> MusicXmlConstants.DIVISIONS;
            case HALF -> MusicXmlConstants.DIVISIONS / 2;
            case QUARTER -> MusicXmlConstants.DIVISIONS / 4;
            case EIGHTH -> MusicXmlConstants.DIVISIONS / 8;
            case SIXTEENTH -> MusicXmlConstants.DIVISIONS / 16;
        };
    }

    /**
     * Durata reală ținând cont de tuplet (dacă există).
     */
    public static int getDurationInDivisions(Duration duration, Tuplet tuplet) {
        int base = getDurationInDivisions(duration);
        if (tuplet == null) {
            return base;
        }
        return (int) Math.round(base * tuplet.durationFactor());
    }

    public static String getTypeName(Duration duration) {
        return switch (duration) {
            case WHOLE -> "whole";
            case HALF -> "half";
            case QUARTER -> "quarter";
            case EIGHTH -> "eighth";
            case SIXTEENTH -> "16th";
        };
    }

    public static Duration fromDivisions(int divisions) {
        if (divisions == MusicXmlConstants.DIVISIONS) return Duration.WHOLE;
        if (divisions == MusicXmlConstants.DIVISIONS / 2) return Duration.HALF;
        if (divisions == MusicXmlConstants.DIVISIONS / 4) return Duration.QUARTER;
        if (divisions == MusicXmlConstants.DIVISIONS / 8) return Duration.EIGHTH;
        if (divisions == MusicXmlConstants.DIVISIONS / 16) return Duration.SIXTEENTH;
        throw new IllegalArgumentException("Unknown divisions: " + divisions);
    }
}