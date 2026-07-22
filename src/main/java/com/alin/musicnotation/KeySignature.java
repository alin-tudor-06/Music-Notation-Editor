package com.alin.musicnotation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class KeySignature {

    // Order of sharps and flats (as steps)
    private static final Pitch.Step[] SHARP_ORDER = {
            Pitch.Step.F, Pitch.Step.C, Pitch.Step.G, Pitch.Step.D,
            Pitch.Step.A, Pitch.Step.E, Pitch.Step.B
    };
    private static final Pitch.Step[] FLAT_ORDER = {
            Pitch.Step.B, Pitch.Step.E, Pitch.Step.A, Pitch.Step.D,
            Pitch.Step.G, Pitch.Step.C, Pitch.Step.F
    };

    // Standard treble clef positions
    private static final Pitch[] SHARP_PITCHES = {
            Pitch.of("F#4"), Pitch.of("C#5"), Pitch.of("G#4"), Pitch.of("D#5"),
            Pitch.of("A#4"), Pitch.of("E#5"), Pitch.of("B#4")
    };
    private static final Pitch[] FLAT_PITCHES = {
            Pitch.of("Bb4"), Pitch.of("Eb5"), Pitch.of("Ab4"), Pitch.of("Db5"),
            Pitch.of("Gb4"), Pitch.of("Cb5"), Pitch.of("Fb4")
    };

    public static final KeySignature C_MAJOR = new KeySignature("C major", 0);
    public static final KeySignature G_MAJOR = new KeySignature("G major", 1);
    public static final KeySignature D_MAJOR = new KeySignature("D major", 2);
    public static final KeySignature A_MAJOR = new KeySignature("A major", 3);
    public static final KeySignature E_MAJOR = new KeySignature("E major", 4);
    public static final KeySignature B_MAJOR = new KeySignature("B major", 5);
    public static final KeySignature F_SHARP_MAJOR = new KeySignature("F# major", 6);
    public static final KeySignature C_SHARP_MAJOR = new KeySignature("C# major", 7);
    public static final KeySignature F_MAJOR = new KeySignature("F major", -1);
    public static final KeySignature B_FLAT_MAJOR = new KeySignature("Bb major", -2);
    public static final KeySignature E_FLAT_MAJOR = new KeySignature("Eb major", -3);
    public static final KeySignature A_FLAT_MAJOR = new KeySignature("Ab major", -4);
    public static final KeySignature D_FLAT_MAJOR = new KeySignature("Db major", -5);
    public static final KeySignature G_FLAT_MAJOR = new KeySignature("Gb major", -6);
    public static final KeySignature C_FLAT_MAJOR = new KeySignature("Cb major", -7);

    private final String name;
    private final int sharps;
    private final List<Pitch> alteredPitches;
    private final Map<Pitch.Step, Accidental> stepAlteration;

    private KeySignature(String name, int sharps) {
        this.name = name;
        this.sharps = sharps;
        this.alteredPitches = buildAlteredPitches(sharps);
        this.stepAlteration = buildStepMap(alteredPitches);
    }

    private static List<Pitch> buildAlteredPitches(int sharps) {
        if (sharps > 0) {
            return IntStream.range(0, sharps)
                    .mapToObj(i -> SHARP_PITCHES[i])
                    .collect(Collectors.toList());
        } else if (sharps < 0) {
            int flats = -sharps;
            return IntStream.range(0, flats)
                    .mapToObj(i -> FLAT_PITCHES[i])
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private static Map<Pitch.Step, Accidental> buildStepMap(List<Pitch> alteredPitches) {
        return alteredPitches.stream()
                .collect(Collectors.toMap(
                        p -> p.step(),
                        p -> p.accidental(),
                        (a, b) -> a
                ));
    }

    public String name() {
        return name;
    }

    public int sharps() {
        return sharps;
    }

    public List<Pitch> alteredPitches() {
        return Collections.unmodifiableList(alteredPitches);
    }

    public Accidental accidentalForStep(Pitch.Step step) {
        return stepAlteration.getOrDefault(step, Accidental.NONE);
    }

    public boolean altersStep(Pitch.Step step) {
        return stepAlteration.containsKey(step);
    }

    @Override
    public String toString() {
        return name;
    }
}