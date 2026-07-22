package com.alin.musicnotation.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import com.alin.musicnotation.model.articulation.*;
import com.alin.musicnotation.model.dynamic.*;

public final class Measure {

    private static final int CAPACITY_IN_SIXTEENTHS = 16;
    private static final double EPSILON = 0.0001;

    private final List<Note> notes = new ArrayList<>();
    private final List<Tuplet> tuplets = new ArrayList<>();
    private final List<Slur> slurs = new ArrayList<>();
    private final List<Articulation> articulations = new ArrayList<>();
    private final List<Dynamic> dynamics = new ArrayList<>();

    public void addNote(Note note) {
        Objects.requireNonNull(note, "note must not be null");
        if (notes.contains(note)) {
            throw new IllegalStateException("Note already exists in this measure: " + note);
        }
        notes.add(note);
    }

    private static void ensureAllNotesPresent(Collection<Note> measureNotes, Collection<Note> objectNotes, String objectName) {
        for (Note note : objectNotes) {
            if (!measureNotes.contains(note)) {
                throw new IllegalStateException(objectName + " contains a note not present in the measure: " + note);
            }
        }
    }

    public void addTuplet(Tuplet tuplet) {
        Objects.requireNonNull(tuplet, "tuplet must not be null");
        if (tuplets.contains(tuplet)) {
            throw new IllegalStateException("Tuplet already added to this measure: " + tuplet);
        }
        ensureAllNotesPresent(notes, tuplet.notes(), "Tuplet");
        tuplets.add(tuplet);
    }

    public void addSlur(Slur slur) {
        Objects.requireNonNull(slur, "slur must not be null");
        if (slurs.contains(slur)) {
            throw new IllegalStateException("Slur already added to this measure: " + slur);
        }
        ensureAllNotesPresent(notes, slur.notes(), "Slur");
        slurs.add(slur);
    }

    public void addArticulation(Articulation articulation) {
        Objects.requireNonNull(articulation, "articulation must not be null");
        if (!notes.contains(articulation.note())) {
            throw new IllegalStateException("Articulation's note is not present in this measure: " + articulation);
        }
        for (Articulation existing : articulations) {
            if (existing.note() == articulation.note() && existing.type() == articulation.type()) {
                throw new IllegalStateException("Duplicate articulation on note: " + articulation);
            }
        }
        articulations.add(articulation);
    }

    public void addDynamic(Dynamic dynamic) {
        Objects.requireNonNull(dynamic, "dynamic must not be null");
        if (!notes.contains(dynamic.note())) {
            throw new IllegalStateException("Dynamic's note is not present in this measure: " + dynamic);
        }
        for (Dynamic existing : dynamics) {
            if (existing.note() == dynamic.note()) {
                throw new IllegalStateException("A note can have at most one dynamic: " + dynamic);
            }
        }
        dynamics.add(dynamic);
    }

    private double usedSixteenths() {
        double total = 0.0;
        for (Note note : notes) {
            double units = note.duration().sixteenths();
            Tuplet tuplet = note.tuplet();
            if (tuplet != null) {
                units *= tuplet.durationFactor();
            }
            total += units;
        }
        return total;
    }

    public void validate() {
        // Tuplet consistency
        for (Tuplet tuplet : tuplets) {
            for (Note note : tuplet.notes()) {
                if (note.tuplet() != tuplet) {
                    throw new IllegalStateException("Note " + note + " is not correctly associated with tuplet " + tuplet);
                }
            }
        }
        for (Note note : notes) {
            Tuplet t = note.tuplet();
            if (t != null && !tuplets.contains(t)) {
                throw new IllegalStateException("Note " + note + " belongs to tuplet " + t + " which is not registered in measure");
            }
        }

        // Slur consistency
        for (Slur slur : slurs) {
            for (Note note : slur.notes()) {
                if (!notes.contains(note)) {
                    throw new IllegalStateException("Slur note " + note + " is not present in measure");
                }
            }
        }

        // Articulation consistency
        for (Articulation art : articulations) {
            if (!notes.contains(art.note())) {
                throw new IllegalStateException("Articulation note not present in measure: " + art);
            }
        }

        // Dynamic consistency
        for (Dynamic dyn : dynamics) {
            if (!notes.contains(dyn.note())) {
                throw new IllegalStateException("Dynamic note not present in measure: " + dyn);
            }
        }

        // Duration capacity
        double used = usedSixteenths();
        if (used < -EPSILON) {
            throw new IllegalStateException("Measure has negative duration: " + used + " sixteenths");
        }
        if (used > CAPACITY_IN_SIXTEENTHS + EPSILON) {
            throw new IllegalStateException("Measure exceeds capacity: " + used + " sixteenths used, max " + CAPACITY_IN_SIXTEENTHS);
        }
    }

    public boolean isFull() { return usedSixteenths() >= CAPACITY_IN_SIXTEENTHS - EPSILON; }

    public List<Note> notes() { return List.copyOf(notes); }
    public List<Tuplet> tuplets() { return List.copyOf(tuplets); }
    public List<Slur> slurs() { return List.copyOf(slurs); }
    public List<Articulation> articulations() { return List.copyOf(articulations); }
    public List<Dynamic> dynamics() { return List.copyOf(dynamics); }

    @Override
    public String toString() {
        return "Measure{notes=" + notes + ", tuplets=" + tuplets + ", slurs=" + slurs +
                ", articulations=" + articulations + ", dynamics=" + dynamics + "}";
    }
}