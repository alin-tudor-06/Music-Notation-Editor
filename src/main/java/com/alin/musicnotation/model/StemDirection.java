package com.alin.musicnotation.model;

public enum StemDirection {
    UP(-1),
    DOWN(1);

    private final int sign;

    StemDirection(int sign) {
        this.sign = sign;
    }

    public int sign() {
        return sign;
    }
}