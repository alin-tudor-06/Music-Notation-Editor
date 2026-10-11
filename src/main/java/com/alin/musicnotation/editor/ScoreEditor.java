package com.alin.musicnotation.editor;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.Score;
import com.alin.musicnotation.model.TimeSignature;

public final class ScoreEditor {

    private ScoreEditor() {}

    /**
     * Adaugă o notă inteligent:
     * 1. Încearcă în măsura pe care s-a dat click (folosind time signature-ul ei efectiv).
     * 2. Dacă nu încape, încearcă în ultima măsură.
     * 3. Dacă nici acolo, creează o măsură nouă (moștenind time signature-ul curent).
     */
    public static void addNoteSmart(Score score, Measure clickedMeasure, Note note) {
        TimeSignature ts = getEffectiveTimeSignature(score, clickedMeasure);
        double capacity = ts.measureCapacityInSixteenths();
        double cost = computeCost(note);

        // 1. Măsura pe care s-a dat click
        if (computeUsed(clickedMeasure) + cost <= capacity + 1e-6) {
            clickedMeasure.addNote(note);
            return;
        }

        // 2. Ultima măsură
        if (!score.measures().isEmpty()) {
            Measure last = score.measures().get(score.measures().size() - 1);
            TimeSignature lastTs = getEffectiveTimeSignature(score, last);
            if (computeUsed(last) + cost <= lastTs.measureCapacityInSixteenths() + 1e-6) {
                last.addNote(note);
                return;
            }
        }

        // 3. Măsură nouă – moștenește time signature-ul efectiv al ultimei măsuri
        Measure newM = new Measure();
        TimeSignature inherited = !score.measures().isEmpty()
                ? getEffectiveTimeSignature(score, score.measures().get(score.measures().size() - 1))
                : score.initialTimeSignature();
        // Nu adăugăm timeSignature explicit dacă e deja cea implicită din Score
        // (altfel ar apărea un <time> redundant la export).
        if (!inherited.equals(score.initialTimeSignature())) {
            newM.addTimeSignature(inherited);
        }
        newM.addNote(note);
        score.addMeasure(newM);
    }

    /**
     * Returnează time signature-ul efectiv la momentul măsurii `target`.
     * Urmărește schimbările din măsurile anterioare.
     */
    public static TimeSignature getEffectiveTimeSignature(Score score, Measure target) {
        TimeSignature current = score.initialTimeSignature();
        for (Measure m : score.measures()) {
            if (m.timeSignature() != null) {
                current = m.timeSignature();
            }
            if (m == target) return current;
        }
        return current;
    }

    /** Șterge nota din score. */
    public static boolean removeNote(Score score, Note note) {
        for (Measure m : score.measures()) {
            if (m.notes().contains(note)) {
                m.removeNote(note);
                return true;
            }
        }
        return false;
    }

    /** Calculează cât din măsură e ocupat (în șaisprezecimi). */
    public static double computeUsed(Measure m) {
        double total = 0;
        for (Note n : m.notes()) {
            total += computeCost(n);
        }
        return total;
    }

    private static double computeCost(Note n) {
        double u = n.duration().sixteenths();
        if (n.tuplet() != null) u *= n.tuplet().durationFactor();
        return u;
    }
}