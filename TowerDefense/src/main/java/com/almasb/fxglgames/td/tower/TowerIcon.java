package com.almasb.fxglgames.td.tower;

import com.almasb.fxgl.texture.Texture;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.getAssetLoader;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class TowerIcon extends Pane {

    public TowerIcon(Texture texture) {
            getChildren().add(texture);
    }
}
