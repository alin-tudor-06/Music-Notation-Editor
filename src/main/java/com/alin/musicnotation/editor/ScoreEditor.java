package com.alin.musicnotation.editor;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.Score;
import com.alin.musicnotation.model.TimeSignature;

public final class ScoreEditor {

    private ScoreEditor() {}

    /**
     * Adaugă o notă inteligent:
     * 1. Încearcă în măsura pe care s-a dat click.
     * 2. Dacă nu încape, încearcă în ultima măsură.
     * 3. Dacă nici acolo, creează o măsură nouă.
     */
    public static void addNoteSmart(Score score, Measure clickedMeasure, Note note) {
        TimeSignature ts = score.initialTimeSignature();
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
            if (computeUsed(last) + cost <= capacity + 1e-6) {
                last.addNote(note);
                return;
            }
        }

        // 3. Măsură nouă
        Measure newM = new Measure();
        newM.addNote(note);
        score.addMeasure(newM);
    }

    /** Șterge nota din score. Returnează true dacă a fost găsită. */
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