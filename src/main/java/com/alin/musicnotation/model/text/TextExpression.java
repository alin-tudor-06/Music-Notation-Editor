package com.alin.musicnotation.model.text;

import java.util.Objects;

/**
 * Represents a non-tempo textual musical indication (e.g., dolce, cantabile, rit., etc.).
 * Immutable.
 */
public final class TextExpression {
    private final String text;
    private final TextPlacement placement;

    public TextExpression(String text, TextPlacement placement) {
        this.text = Objects.requireNonNull(text, "text must not be null");
        if (text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        this.placement = Objects.requireNonNull(placement, "placement must not be null");
    }

    public TextExpression(String text) {
        this(text, TextPlacement.ABOVE);
    }

    public String text() {
        return text;
    }

    public TextPlacement placement() {
        return placement;
    }

    @Override
    public String toString() {
        return "TextExpression{" +
                "text='" + text + '\'' +
                ", placement=" + placement +
                '}';
    }
}