package com.alin.musicnotation.model.tempo;

import java.util.Objects;

public final class Tempo {
    private final String text;
    private final Integer bpm;
    private final TempoPlacement placement;

    public Tempo(String text) {
        this(text, null, TempoPlacement.ABOVE);
    }

    public Tempo(String text, int bpm) {
        this(text, bpm, TempoPlacement.ABOVE);
    }

    public Tempo(String text, Integer bpm, TempoPlacement placement) {
        if (text == null && bpm == null) {
            throw new IllegalArgumentException("Tempo must have either text or BPM");
        }
        this.text = text;
        this.bpm = bpm;
        this.placement = Objects.requireNonNull(placement, "placement must not be null");
    }

    public static Tempo ofBpm(int bpm) {
        return new Tempo(null, bpm, TempoPlacement.ABOVE);
    }

    public String text() { return text; }
    public Integer bpm() { return bpm; }
    public TempoPlacement placement() { return placement; }
    public boolean hasText() { return text != null && !text.isBlank(); }
    public boolean hasBpm() { return bpm != null; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Tempo{");
        if (text != null) sb.append(text);
        if (bpm != null) {
            if (text != null) sb.append(" ");
            sb.append("♩=").append(bpm);
        }
        sb.append(", placement=").append(placement);
        sb.append('}');
        return sb.toString();
    }
}