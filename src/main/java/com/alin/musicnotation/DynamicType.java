package com.alin.musicnotation;

/**
 * Standard dynamic markings.
 * TODO: Replace switch with lookup table when supporting the full dynamic set
 * (ffff, sf, sfz, rfz, fp, fz, sffz, etc.)
 */
public enum DynamicType {
    PPP,
    PP,
    P,
    MP,
    MF,
    F,
    FF,
    FFF
}