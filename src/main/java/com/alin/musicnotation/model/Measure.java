package com.alin.musicnotation.model;

import com.alin.musicnotation.model.articulation.Articulation;
import com.alin.musicnotation.model.clef.ClefType;
import com.alin.musicnotation.model.dynamic.Dynamic;
import com.alin.musicnotation.model.hairpin.Hairpin;
import com.alin.musicnotation.model.tempo.Tempo;
import com.alin.musicnotation.model.text.TextExpression;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class Measure {

    private static final double EPSILON = 0.0001;

    private final List<Note> notes = new ArrayList<>();
    private final List<Tuplet> tuplets = new ArrayList<>();
    private final List<Slur> slurs = new ArrayList<>();
    private final List<Articulation> articulations = new ArrayList<>();
    private final List<Dynamic> dynamics = new ArrayList<>();
    private final List<Hairpin> hairpins = new ArrayList<>();
    private Tempo tempo = null;
    private boolean startRepeat = false;
    private boolean endRepeat = false;
    private final List<TextExpression> textExpressions = new ArrayList<>();
    private ClefType clef = null;
    private KeySignature keySignature = null;
    private TimeSignature timeSignature = null;

    // ----- Notes -----

    public void addNote(Note note) {
        Objects.requireNonNull(note, "note must not be null");
        if (notes.contains(note)) {
            throw new IllegalStateException("Note already exists in this measure: " + note);
        }
        notes.add(note);
    }

    public void removeNote(Note note) {
        notes.remove(note);
    }

    // ----- Tuplets, Slurs, Articulations, Dynamics, Hairpins -----

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

    public void removeArticulation(Articulation articulation) {
        articulations.remove(articulation);
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

    public void removeDynamic(Dynamic dynamic) {
        dynamics.remove(dynamic);
    }

    public void addHairpin(Hairpin hairpin) {
        Objects.requireNonNull(hairpin, "hairpin must not be null");
        Note start = hairpin.startNote();
        Note end = hairpin.endNote();

        if (!notes.contains(start) || !notes.contains(end)) {
            throw new IllegalStateException("Hairpin contains a note not present in this measure: " + hairpin);
        }
        if (start == end) {
            throw new IllegalStateException("Hairpin must start and end on different notes");
        }
        int startIndex = notes.indexOf(start);
        int endIndex = notes.indexOf(end);
        if (startIndex >= endIndex) {
            throw new IllegalStateException("Hairpin start note must appear before end note in the measure");
        }
        for (Hairpin existing : hairpins) {
            if (existing.startNote() == start &&
                    existing.endNote() == end &&
                    existing.type() == hairpin.type() &&
                    existing.placement() == hairpin.placement()) {
                throw new IllegalStateException("Duplicate hairpin: " + hairpin);
            }
        }
        hairpins.add(hairpin);
    }

    // ----- Tempo -----

    public void addTempo(Tempo tempo) {
        Objects.requireNonNull(tempo, "tempo must not be null");
        if (this.tempo != null) {
            throw new IllegalStateException("Measure already has a tempo marking: " + this.tempo);
        }
        this.tempo = tempo;
    }

    public void setTempo(Tempo tempo) {
        this.tempo = tempo;
    }

    public void removeTempo() {
        this.tempo = null;
    }

    public Tempo tempo() {
        return tempo;
    }

    // ----- Repeat Signs -----

    public void addStartRepeat() {
        if (startRepeat) {
            throw new IllegalStateException("Measure already has a start repeat");
        }
        startRepeat = true;
    }

    public void addEndRepeat() {
        if (endRepeat) {
            throw new IllegalStateException("Measure already has an end repeat");
        }
        endRepeat = true;
    }

    public boolean hasStartRepeat() {
        return startRepeat;
    }

    public boolean hasEndRepeat() {
        return endRepeat;
    }

    // ----- Text Expressions -----

    public void addTextExpression(TextExpression expression) {
        Objects.requireNonNull(expression, "expression must not be null");
        for (TextExpression existing : textExpressions) {
            if (existing.text().equals(expression.text()) &&
                    existing.placement() == expression.placement()) {
                throw new IllegalStateException("Duplicate text expression: " + expression);
            }
        }
        textExpressions.add(expression);
    }

    public void removeTextExpression(TextExpression expression) {
        textExpressions.remove(expression);
    }

    public void clearTextExpressions() {
        textExpressions.clear();
    }

    public List<TextExpression> textExpressions() {
        return List.copyOf(textExpressions);
    }

    // ----- Clef -----

    public void addClef(ClefType clef) {
        this.clef = Objects.requireNonNull(clef, "clef must not be null");
    }

    public ClefType clef() {
        return clef;
    }

    // ----- Key Signature -----

    public void addKeySignature(KeySignature keySignature) {
        this.keySignature = Objects.requireNonNull(keySignature, "keySignature must not be null");
    }

    public KeySignature keySignature() {
        return keySignature;
    }

    // ----- Time Signature -----

    public void addTimeSignature(TimeSignature timeSignature) {
        this.timeSignature = Objects.requireNonNull(timeSignature, "timeSignature must not be null");
    }

    public TimeSignature timeSignature() {
        return timeSignature;
    }

    // ----- Duration -----

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

    // ----- Validation -----

    public void validate(TimeSignature currentTimeSig) {
        if (currentTimeSig == null) {
            throw new IllegalStateException("Cannot validate measure without a time signature");
        }

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

        // Hairpin consistency
        for (Hairpin hp : hairpins) {
            if (!notes.contains(hp.startNote()) || !notes.contains(hp.endNote())) {
                throw new IllegalStateException("Hairpin note not present in measure: " + hp);
            }
        }

        // Duration capacity
        double used = usedSixteenths();
        double capacity = currentTimeSig.measureCapacityInSixteenths();
        if (used < -EPSILON) {
            throw new IllegalStateException("Measure has negative duration: " + used + " sixteenths");
        }
        if (used > capacity + EPSILON) {
            throw new IllegalStateException("Measure exceeds capacity: " + used + " sixteenths used, max " + capacity + " for " + currentTimeSig);
        }
    }

    public boolean isFull(TimeSignature currentTimeSig) {
        if (currentTimeSig == null) return false;
        return usedSixteenths() >= currentTimeSig.measureCapacityInSixteenths() - EPSILON;
    }

    // ----- Getters -----

    public List<Note> notes() {
        return List.copyOf(notes);
    }

    public List<Tuplet> tuplets() {
        return List.copyOf(tuplets);
    }

    public List<Slur> slurs() {
        return List.copyOf(slurs);
    }

    public List<Articulation> articulations() {
        return List.copyOf(articulations);
    }

    public List<Dynamic> dynamics() {
        return List.copyOf(dynamics);
    }

    public List<Hairpin> hairpins() {
        return List.copyOf(hairpins);
    }

    @Override
    public String toString() {
        return "Measure{notes=" + notes +
                ", tuplets=" + tuplets +
                ", slurs=" + slurs +
                ", articulations=" + articulations +
                ", dynamics=" + dynamics +
                ", hairpins=" + hairpins +
                ", tempo=" + tempo +
                ", startRepeat=" + startRepeat +
                ", endRepeat=" + endRepeat +
                ", textExpressions=" + textExpressions +
                ", clef=" + clef +
                ", keySignature=" + keySignature +
                ", timeSignature=" + timeSignature +
                "}";
    }
}