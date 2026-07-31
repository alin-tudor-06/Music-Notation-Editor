package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.*;
import com.alin.musicnotation.model.articulation.*;
import com.alin.musicnotation.model.layout.NoteLayout;
import com.alin.musicnotation.model.layout.StemDirection;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.Map;

public class ArticulationRenderer {

    private static final double ARTICULATION_FONT_SIZE_FACTOR = 1.0;

    public void draw(GraphicsContext gc, Measure measure, Map<Note, NoteLayout> layoutMap,
                     double middleLineY, int middleLinePosition,
                     double articulationFontSizeFactor,
                     double articulationOffset,
                     double staffLineSpacing,
                     double noteHeadWidth) {

        if (measure.articulations().isEmpty()) return;

        for (Articulation art : measure.articulations()) {
            NoteLayout layout = layoutMap.get(art.note());
            if (layout == null) continue;

            double x = layout.x();
            double y = layout.y();

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

            double offset = articulationOffset;
            if (art.type() == ArticulationType.FERMATA) {
                offset *= 1.4;
            }

            double symbolY = effectivePlacement == ArticulationPlacement.ABOVE
                    ? y - offset
                    : y + offset;

            String symbol = articulationSymbol(art.type(), effectivePlacement);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Serif", staffLineSpacing * articulationFontSizeFactor));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(javafx.geometry.VPos.CENTER);
            gc.fillText(symbol, x, symbolY);
        }
    }

    private ArticulationPlacement defaultPlacement(ArticulationType type) {
        return switch (type) {
            case STACCATO, STACCATISSIMO -> ArticulationPlacement.BELOW;
            case ACCENT, MARCATO, TENUTO, FERMATA -> ArticulationPlacement.ABOVE;
        };
    }

    private String articulationSymbol(ArticulationType type, ArticulationPlacement placement) {
        if (type == ArticulationType.FERMATA) {
            return placement == ArticulationPlacement.ABOVE ? "⌒" : "⌣";
        }
        return switch (type) {
            case STACCATO -> "•";
            case STACCATISSIMO -> "‣";
            case ACCENT -> ">";
            case MARCATO -> "^";
            case TENUTO -> "-";
            default -> "?";
        };
    }
}