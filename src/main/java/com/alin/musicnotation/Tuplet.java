package com.alin.musicnotation;

import java.util.List;
import java.util.Objects;

/**
 * Represents a tuplet (e.g., triplet, quintuplet) as a group of notes
 * that are played in a fraction of their normal duration.
 */
public class Tuplet {
    private final int numerator;
    private final int denominator;
    private final List<Note> notes;

    /**
     * Creates a tuplet with the given ratio and list of notes.
     *
     * @param numerator   the numerator of the ratio (e.g., 3 for triplet)
     * @param denominator the denominator of the ratio (e.g., 2 for triplet)
     * @param notes       the notes belonging to this tuplet
     * @throws IllegalArgumentException if numerator or denominator <= 0, or notes list is empty
     * @throws IllegalStateException    if any note already belongs to another tuplet
     */
    public Tuplet(int numerator, int denominator, List<Note> notes) {
        if (numerator <= 0 || denominator <= 0) {
            throw new IllegalArgumentException("Numerator and denominator must be positive");
        }
        if (notes == null || notes.isEmpty()) {
            throw new IllegalArgumentException("Tuplet must contain at least one note");
        }
        this.numerator = numerator;
        this.denominator = denominator;
        this.notes = List.copyOf(notes);

        // Asociază fiecare notă cu acest tuplet, verificând că nu aparține deja altuia
        for (Note note : this.notes) {
            Objects.requireNonNull(note, "Note cannot be null");
            if (note.getTuplet() != null) {
                throw new IllegalStateException("Note already belongs to another tuplet: " + note);
            }
            note.setTuplet(this);
        }
    }

    public int getNumerator() {
        return numerator;
    }

    public int getDenominator() {
        return denominator;
    }

    public List<Note> getNotes() {
        return notes;
    }

    /**
     * Factorul de scalare al duratei: durata reală = durata aparentă * factor.
     * De exemplu, pentru triplet 3:2, factorul este 2/3 ≈ 0.666.
     */
    public double getDurationFactor() {
        return (double) denominator / numerator;
    }

    @Override
    public String toString() {
        return "Tuplet{" + numerator + ":" + denominator + ", notes=" + notes + "}";
    }
}