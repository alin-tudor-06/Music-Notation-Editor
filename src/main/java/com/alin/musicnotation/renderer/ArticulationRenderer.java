package com.alin.musicnotation.renderer;

import com.alin.musicnotation.*;
import com.alin.musicnotation.model.NoteLayout;
import com.alin.musicnotation.model.StemDirection;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.*;

public class ArticulationRenderer {

    public void draw(GraphicsContext gc, Measure measure, Map<Note, NoteLayout> layoutMap,
                     double middleLineY, int middleLinePosition,
                     double articulationFontSizeFactor,
                     double articulationOffset, double staffLineSpacing,
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
        switch (type) {
            case STACCATO:
            case STACCATISSIMO:
                return ArticulationPlacement.BELOW;
            case ACCENT:
            case MARCATO:
            case TENUTO:
            case FERMATA:
                return ArticulationPlacement.ABOVE;
        }
        return ArticulationPlacement.ABOVE;
    }

    private String articulationSymbol(ArticulationType type, ArticulationPlacement placement) {
        if (type == ArticulationType.FERMATA) {
            return placement == ArticulationPlacement.ABOVE ? "⌒" : "⌣";
        }
        switch (type) {
            case STACCATO:
                return "•";
            case STACCATISSIMO:
                return "‣";
            case ACCENT:
                return ">";
            case MARCATO:
                return "^";
            case TENUTO:
                return "-";
        }
        return "?";
    }
}