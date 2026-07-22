package com.alin.musicnotation;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Slur {
    private final List<Note> notes;

    public Slur(List<Note> notes) {
        Objects.requireNonNull(notes, "notes list must not be null");
        if (notes.size() < 2) {
            throw new IllegalArgumentException("Slur must contain at least two notes");
        }
        Set<Note> uniqueCheck = new HashSet<>();
        for (Note note : notes) {
            Objects.requireNonNull(note, "Note cannot be null");
            if (note.isRest()) {
                throw new IllegalArgumentException("Slur cannot contain rests: " + note);
            }
            if (!uniqueCheck.add(note)) {
                throw new IllegalArgumentException("Duplicate note in slur: " + note);
            }
        }
        this.notes = List.copyOf(notes);
    }

    public List<Note> notes() {
        return notes;
    }

    public Note firstNote() {
        return notes.get(0);
    }

    public Note lastNote() {
        return notes.get(notes.size() - 1);
    }

    @Override
    public String toString() {
        return "Slur{notes=" + notes + "}";
    }
}