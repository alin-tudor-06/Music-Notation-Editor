package com.alin.musicnotation.renderer;

import com.alin.musicnotation.*;
import com.alin.musicnotation.model.NoteLayout;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.*;

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
        switch (type) {
            case PPP:
                return "ppp";
            case PP:
                return "pp";
            case P:
                return "p";
            case MP:
                return "mp";
            case MF:
                return "mf";
            case F:
                return "f";
            case FF:
                return "ff";
            case FFF:
                return "fff";
        }
        return "";
    }
}