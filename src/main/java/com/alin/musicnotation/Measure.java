package com.alin.musicnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents exactly one musical measure in 4/4 time.
 * <p>
 * Notes are kept in insertion order. The measure rejects any note that
 * would make its total duration exceed the 4/4 capacity.
 */
public final class Measure {

    private static final int CAPACITY_IN_SIXTEENTHS = 16;

    private final List<Note> notes = new ArrayList<>();
    private int usedSixteenths = 0;

    public void addNote(Note note) {
        Objects.requireNonNull(note, "note must not be null");

        int noteUnits = sixteenthUnits(note.duration());
        if (usedSixteenths + noteUnits > CAPACITY_IN_SIXTEENTHS) {
            throw new IllegalStateException("Note does not fit in the remaining space of the measure");
        }

        notes.add(note);
        usedSixteenths += noteUnits;
    }

    public List<Note> notes() {
        return List.copyOf(notes);
    }

    public boolean isFull() {
        return usedSixteenths == CAPACITY_IN_SIXTEENTHS;
    }

    private static int sixteenthUnits(Duration duration) {
        return CAPACITY_IN_SIXTEENTHS / duration.denominator();
    }
}