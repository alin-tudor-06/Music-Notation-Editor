package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.dynamic.Dynamic;
import com.alin.musicnotation.model.dynamic.DynamicPlacement;
import com.alin.musicnotation.model.dynamic.DynamicType;
import com.alin.musicnotation.model.layout.NoteLayout;
import com.alin.musicnotation.smufl.BravuraFont;
import com.alin.musicnotation.smufl.SmuflGlyph;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.Map;

public class DynamicRenderer {

    public void draw(GraphicsContext gc, Measure measure, Map<Note, NoteLayout> layoutMap,
                     double middleLineY, double dynamicFontSizeFactor,
                     double dynamicOffset, double staffLineSpacing) {

        if (measure.dynamics().isEmpty()) return;

        // Set up Bravura font once (outside the loop)
        Font bravura = BravuraFont.getFont();
        double fontSize = staffLineSpacing * dynamicFontSizeFactor;
        Font glyphFont = Font.font(bravura.getFamily(), fontSize);

        // Configure GraphicsContext once (outside the loop)
        gc.setFill(Color.BLACK);
        gc.setFont(glyphFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);

        for (Dynamic dyn : measure.dynamics()) {
            NoteLayout layout = layoutMap.get(dyn.note());
            if (layout == null) continue;

            double x = layout.x();
            double y = layout.y();

            // Calculate effective placement (exactly the same logic as before)
            DynamicPlacement effectivePlacement = dyn.placement();
            if (effectivePlacement == DynamicPlacement.AUTO) {
                effectivePlacement = DynamicPlacement.BELOW;
            }

            double symbolY = effectivePlacement == DynamicPlacement.ABOVE
                    ? y - dynamicOffset
                    : y + dynamicOffset;

            // Build the SMuFL glyph string for this dynamic
            String glyphString = glyphStringFor(dyn.type());

            gc.fillText(glyphString, x, symbolY);
        }
    }

    /**
     * Builds a SMuFL glyph string for a dynamic type.
     * For example:
     * - P -> "p" glyph
     * - MF -> "m" + "f" glyphs
     * - FF -> "f" + "f" glyphs
     * - PPP -> "p" + "p" + "p" glyphs
     */
    private String glyphStringFor(DynamicType type) {
        return switch (type) {
            case PPP -> repeatGlyph(SmuflGlyph.DYNAMIC_PIANO, 3);
            case PP -> repeatGlyph(SmuflGlyph.DYNAMIC_PIANO, 2);
            case P -> SmuflGlyph.DYNAMIC_PIANO.unicode();
            case MP -> SmuflGlyph.DYNAMIC_MEZZO.unicode() + SmuflGlyph.DYNAMIC_PIANO.unicode();
            case MF -> SmuflGlyph.DYNAMIC_MEZZO.unicode() + SmuflGlyph.DYNAMIC_FORTE.unicode();
            case F -> SmuflGlyph.DYNAMIC_FORTE.unicode();
            case FF -> repeatGlyph(SmuflGlyph.DYNAMIC_FORTE, 2);
            case FFF -> repeatGlyph(SmuflGlyph.DYNAMIC_FORTE, 3);
        };
    }

    /**
     * Repeats a SMuFL glyph multiple times.
     * Used for dynamic markings with repeated letters (pp, pp, fff, etc.).
     */
    private String repeatGlyph(SmuflGlyph glyph, int count) {
        String glyphChar = glyph.unicode();
        return switch (count) {
            case 1 -> glyphChar;
            case 2 -> glyphChar + glyphChar;
            case 3 -> glyphChar + glyphChar + glyphChar;
            default -> {
                // Fallback for any other count (not expected)
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < count; i++) {
                    sb.append(glyphChar);
                }
                yield sb.toString();
            }
        };
    }
}