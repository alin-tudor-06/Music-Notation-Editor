package com.alin.musicnotation.smufl;

import javafx.scene.text.Font;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility for loading the Bravura SMuFL font.
 * <p>
 * The font is loaded lazily on first access and cached thereafter.
 * If loading fails, an {@link IllegalStateException} is thrown because
 * the Bravura font is a mandatory dependency for SMuFL-based rendering.
 * </p>
 * <p>
 * The loaded font instance (at size 10) is used only to obtain the font family.
 * Renderers must create properly sized fonts using:
 * {@code Font.font(BravuraFont.getFont().getFamily(), desiredSize)}
 * </p>
 * <p>
 * Future improvement: consider adding a dedicated {@code getFamily()} method
 * to avoid unnecessary creation of a 10pt Font object.
 * </p>
 */
public final class BravuraFont {

    private static final String FONT_PATH = "/fonts/Bravura.otf";

    private static Font font;

    private BravuraFont() {
        // Utility class – no instantiation
    }

    /**
     * Returns the loaded Bravura font.
     * The font is loaded once, lazily, on first call.
     *
     * @return the Bravura font
     * @throws IllegalStateException if the font file is missing or cannot be loaded
     */
    public static Font getFont() {
        if (font == null) {
            font = loadFont();
        }
        return font;
    }

    private static Font loadFont() {
        try (InputStream is = BravuraFont.class.getResourceAsStream(FONT_PATH)) {
            if (is == null) {
                throw new IllegalStateException("Cannot find Bravura font: " + FONT_PATH);
            }

            // Temporary size (10pt) – renderers will create properly sized fonts
            // using Font.font(family, desiredSize)
            Font loaded = Font.loadFont(is, 10);

            if (loaded == null) {
                throw new IllegalStateException("Failed to load Bravura font from " + FONT_PATH);
            }

            return loaded;
        } catch (IOException e) {
            // Only IOException is checked; other exceptions (e.g. RuntimeException)
            // will propagate naturally.
            throw new IllegalStateException("Failed to load Bravura font from " + FONT_PATH, e);
        }
    }
}