package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.articulation.Articulation;
import com.alin.musicnotation.model.articulation.ArticulationPlacement;
import com.alin.musicnotation.model.articulation.ArticulationType;
import com.alin.musicnotation.model.layout.NoteLayout;
import com.alin.musicnotation.model.layout.StemDirection;
import com.alin.musicnotation.smufl.BravuraFont;
import com.alin.musicnotation.smufl.SmuflGlyph;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

import java.util.Map;

public class ArticulationRenderer {

    public void draw(GraphicsContext gc, Measure measure, Map<Note, NoteLayout> layoutMap,
                     double middleLineY, int middleLinePosition,
                     double articulationFontSizeFactor,
                     double articulationOffset,
                     double staffLineSpacing,
                     double noteHeadWidth) {

        if (measure.articulations().isEmpty()) return;

        // Set up Bravura font once (outside the loop)
        Font bravura = BravuraFont.getFont();
        double fontSize = staffLineSpacing * articulationFontSizeFactor;
        Font glyphFont = Font.font(bravura.getFamily(), fontSize);

        // Configure GraphicsContext once (outside the loop)
        gc.setFill(Color.BLACK);
        gc.setFont(glyphFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        for (Articulation art : measure.articulations()) {
            NoteLayout layout = layoutMap.get(art.note());
            if (layout == null) continue;

            double x = layout.x();
            double y = layout.y();

            // Calculate effective placement (exactly the same logic as before)
            ArticulationPlacement effectivePlacement = art.placement();
            if (effectivePlacement == ArticulationPlacement.AUTO) {
                StemDirection dir = RenderingHelpers.stemDirection(layout.position(), middleLinePosition);
                ArticulationPlacement preferred = defaultPlacement(art.type());
                if (preferred == ArticulationPlacement.ABOVE) {
                    effectivePlacement = (dir == StemDirection.UP)
                            ? ArticulationPlacement.BELOW
                            : ArticulationPlacement.ABOVE;
                } else {
                    effectivePlacement = (dir == StemDirection.DOWN)
                            ? ArticulationPlacement.ABOVE
                            : ArticulationPlacement.BELOW;
                }
            }

            // Calculate vertical position (exactly the same logic as before)
            double offset = articulationOffset;
            if (art.type() == ArticulationType.FERMATA) {
                offset *= 1.4;
            }

            double symbolY = effectivePlacement == ArticulationPlacement.ABOVE
                    ? y - offset
                    : y + offset;

            // Get the appropriate SMuFL glyph
            SmuflGlyph glyph = glyphFor(art.type(), effectivePlacement);
            if (glyph == null) {
                continue;
            }

            gc.fillText(glyph.unicode(), x, symbolY);
        }
    }

    /**
     * Returns the default placement for an articulation type when AUTO is specified.
     * This logic is unchanged from the original implementation.
     */
    private ArticulationPlacement defaultPlacement(ArticulationType type) {
        return switch (type) {
            case STACCATO, STACCATISSIMO -> ArticulationPlacement.BELOW;
            case ACCENT, MARCATO, TENUTO, FERMATA -> ArticulationPlacement.ABOVE;
        };
    }

    /**
     * Maps an ArticulationType and effective placement to the corresponding SMuFL glyph.
     */
    private SmuflGlyph glyphFor(ArticulationType type, ArticulationPlacement placement) {
        boolean above = placement == ArticulationPlacement.ABOVE;
        return switch (type) {
            case ACCENT -> above ? SmuflGlyph.ACCENT : SmuflGlyph.ACCENT_BELOW;
            case STACCATO -> above ? SmuflGlyph.STACCATO : SmuflGlyph.STACCATO_BELOW;
            case TENUTO -> above ? SmuflGlyph.TENUTO : SmuflGlyph.TENUTO_BELOW;
            case STACCATISSIMO -> above ? SmuflGlyph.STACCATISSIMO : SmuflGlyph.STACCATISSIMO_BELOW;
            case MARCATO -> above ? SmuflGlyph.MARCATO : SmuflGlyph.MARCATO_BELOW;
            case FERMATA -> above ? SmuflGlyph.FERMATA : SmuflGlyph.FERMATA_BELOW;
        };
    }
}