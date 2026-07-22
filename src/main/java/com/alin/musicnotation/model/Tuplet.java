package com.alin.musicnotation.model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Tuplet {
    private final int numerator;
    private final int denominator;
    private final List<Note> notes;

    public Tuplet(int numerator, int denominator, List<Note> notes) {
        if (numerator <= 0 || denominator <= 0) {
            throw new IllegalArgumentException("Numerator and denominator must be positive");
        }
        if (numerator == denominator) {
            throw new IllegalArgumentException("Invalid tuplet ratio: numerator and denominator must differ");
        }
        Objects.requireNonNull(notes, "notes list must not be null");
        if (notes.size() < 2) {
            throw new IllegalArgumentException("Tuplet must contain at least two notes");
        }
        Set<Note> uniqueCheck = new HashSet<>();
        for (Note note : notes) {
            Objects.requireNonNull(note, "Note cannot be null");
            if (note.tuplet() != null) {
                throw new IllegalStateException("Note already belongs to another tuplet: " + note);
            }
            if (!uniqueCheck.add(note)) {
                throw new IllegalArgumentException("Duplicate note in tuplet: " + note);
            }
        }
        this.numerator = numerator;
        this.denominator = denominator;
        this.notes = List.copyOf(notes);

        for (Note note : this.notes) {
            note.setTuplet(this);
        }
    }

    public int numerator() { return numerator; }
    public int denominator() { return denominator; }
    public List<Note> notes() { return notes; }
    public double durationFactor() { return (double) denominator / numerator; }

    @Override
    public String toString() {
        return "Tuplet{" + numerator + ":" + denominator + ", notes=" + notes + "}";
    }
}