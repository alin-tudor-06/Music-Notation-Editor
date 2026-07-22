package com.alin.musicnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Measure {

    private static final int CAPACITY_IN_SIXTEENTHS = 16;
    private static final double EPSILON = 0.0001;

    private final List<Note> notes = new ArrayList<>();
    private final List<Tuplet> tuplets = new ArrayList<>();

    public void addNote(Note note) {
        Objects.requireNonNull(note, "note must not be null");
        if (notes.contains(note)) {
            throw new IllegalStateException("Note already exists in this measure: " + note);
        }
        notes.add(note);
    }

    public void addTuplet(Tuplet tuplet) {
        Objects.requireNonNull(tuplet, "tuplet must not be null");
        if (tuplets.contains(tuplet)) {
            throw new IllegalStateException("Tuplet already added to this measure: " + tuplet);
        }
        for (Note note : tuplet.getNotes()) {
            if (!notes.contains(note)) {
                throw new IllegalStateException("Tuplet contains a note not in this measure: " + note);
            }
            if (note.getTuplet() != tuplet) {
                throw new IllegalStateException("Note " + note + " is not associated with this tuplet");
            }
        }
        tuplets.add(tuplet);
    }

    private double usedSixteenths() {
        double total = 0.0;
        for (Note note : notes) {
            double units = note.duration().sixteenths();
            Tuplet tuplet = note.getTuplet();
            if (tuplet != null) {
                units *= tuplet.getDurationFactor();
            }
            total += units;
        }
        return total;
    }

    public void validate() {
        // 1. Verifică dacă toate notele din tuplete sunt corect asociate și prezente
        for (Tuplet tuplet : tuplets) {
            for (Note note : tuplet.getNotes()) {
                if (note.getTuplet() != tuplet) {
                    throw new IllegalStateException(
                            "Note " + note + " is not correctly associated with tuplet " + tuplet
                    );
                }
                if (!notes.contains(note)) {
                    throw new IllegalStateException(
                            "Tuplet note " + note + " is not present in measure"
                    );
                }
            }
        }

        // 2. Verifică invers: pentru fiecare notă cu tuplet, tupletul să fie în listă
        for (Note note : notes) {
            Tuplet t = note.getTuplet();
            if (t != null && !tuplets.contains(t)) {
                throw new IllegalStateException(
                        "Note " + note + " belongs to tuplet " + t + " which is not registered in measure"
                );
            }
        }

        // 3. Verifică durata totală (cu toleranță)
        double used = usedSixteenths();
        if (used < -EPSILON) {
            throw new IllegalStateException(
                    "Measure has negative duration: " + used + " sixteenths"
            );
        }
        if (used > CAPACITY_IN_SIXTEENTHS + EPSILON) {
            throw new IllegalStateException(
                    "Measure exceeds capacity: " + used + " sixteenths used, max " + CAPACITY_IN_SIXTEENTHS
            );
        }
    }

    public boolean isFull() {
        return usedSixteenths() >= CAPACITY_IN_SIXTEENTHS - EPSILON;
    }

    public List<Note> notes() {
        return List.copyOf(notes);
    }

    public List<Tuplet> getTuplets() {
        return List.copyOf(tuplets);
    }

    @Override
    public String toString() {
        return "Measure{notes=" + notes + ", tuplets=" + tuplets + "}";
    }
}