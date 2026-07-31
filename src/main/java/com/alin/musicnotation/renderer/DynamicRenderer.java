package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.layout.NoteLayout;
import com.alin.musicnotation.model.dynamic.Dynamic;
import com.alin.musicnotation.model.dynamic.DynamicPlacement;
import com.alin.musicnotation.model.dynamic.DynamicType;
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

        for (Dynamic dyn : measure.dynamics()) {
            NoteLayout layout = layoutMap.get(dyn.note());
            if (layout == null) continue;

            double x = layout.x();
            double y = layout.y();

            DynamicPlacement effectivePlacement = dyn.placement();
            if (effectivePlacement == DynamicPlacement.AUTO) {
                effectivePlacement = DynamicPlacement.BELOW;
            }

            double symbolY = effectivePlacement == DynamicPlacement.ABOVE
                    ? y - dynamicOffset
                    : y + dynamicOffset;

            String text = dynamicText(dyn.type());
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Serif", staffLineSpacing * dynamicFontSizeFactor));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(javafx.geometry.VPos.CENTER);
            gc.fillText(text, x, symbolY);
        }
    }

    private String dynamicText(DynamicType type) {
        return switch (type) {
            case PPP -> "ppp";
            case PP -> "pp";
            case P -> "p";
            case MP -> "mp";
            case MF -> "mf";
            case F -> "f";
            case FF -> "ff";
            case FFF -> "fff";
        };

    }
}