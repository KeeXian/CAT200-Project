package com.almasb.fxglgames.td;

import com.almasb.fxgl.dsl.EntityBuilder;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.entity.*;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxglgames.td.components.EnemyComponent;
import com.almasb.fxglgames.td.components.TowerComponent;
import com.almasb.fxglgames.td.enemy.EnemyDataComponent;
import com.almasb.fxglgames.td.tower.BulletComponent;
import com.almasb.fxglgames.td.tower.TowerDataComponent;
import javafx.animation.FadeTransition;
import javafx.geometry.Point2D;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class TowerDefenseFactory implements EntityFactory {

    @Spawns("Enemy")
    public Entity spawnEnemy(SpawnData data) {
        EnemyDataComponent enemyDataComponent = EnemyDataComponent.makeEnemy(data.get("index"), data.get("level"));
        VBox vBox = new VBox();
        vBox.getChildren().addAll(enemyDataComponent.getHpBar(), new Rectangle(40,40,Color.RED));
        Entity newEntity = entityBuilder()
                .type(TowerDefenseType.ENEMY)
                .from(data)
                .viewWithBBox(vBox)
                .with(new CollidableComponent(true), enemyDataComponent)
                .with(new EnemyComponent(enemyDataComponent.getSpeed()))
                .build();
        newEntity.getBoundingBoxComponent().addHitBox(new HitBox("BODY", new Point2D(0, 0), BoundingShape.box(40, 80)));
        return newEntity;
    }

    @Spawns("Tower")
    public Entity spawnTower(SpawnData data) {
        TowerDataComponent towerComponent = TowerDataComponent.makeTower(data.get("index"));
//        try {
//            towerComponent = getAssetLoader()
//                    .loadKV("Tower" + data.get("index") + ".kv")
//                    .to(TowerDataComponent.class);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to parse KV file: " + e);
//        }
        Entity tower = new Entity();
        if(FXGL.getGameState().getDouble("playerGold")<towerComponent.getPrice()) {
            Text text = FXGL.getUIFactory().newText("Insufficient Funds", Color.RED, 24);
            text.setTranslateX(150);
            text.setTranslateY(20);
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(3000), text);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0);
            fadeTransition.play();
            FXGL.getGameScene().addUINode(text);
        }
        else {
            Texture texture = null;
            if(data.get("index").equals(1))
                texture = new Texture(getAssetLoader().loadImage("small_stone_tower.png"));
            else if(data.get("index").equals(2))
                texture = new Texture(getAssetLoader().loadImage("big_stone_tower.png"));
            else if(data.get("index").equals(3))
                texture = new Texture(getAssetLoader().loadImage("metal_ball_tower.png"));
            else
                texture = new Texture(getAssetLoader().loadImage("fire_tower.png"));
            texture.setFitHeight(60);
            texture.setFitWidth(60);
            tower = entityBuilder()
                    .type(TowerDefenseType.TOWER)
                    .from(data)
                    .view(texture)
                    .with(new CollidableComponent(true), towerComponent)
                    .with(new TowerComponent(towerComponent.getAttackDelay(),towerComponent.getSpeed()))
                    .build();
            FXGL.getGameState().increment("playerGold",-(towerComponent.getPrice()));
            Text text = FXGL.getUIFactory().
                    newText("-"+towerComponent.getPrice(), Color.BLACK, 10);
            text.setTranslateX(20);
            text.setTranslateY(30);
            FadeTransition fade = new FadeTransition(Duration.millis(3000),text);
            fade.setFromValue(1.0);
            fade.setToValue(0);
            fade.play();
            FXGL.getGameScene().addUINode(text);
        }
        return tower;
    }

    @Spawns("Bullet")
    public Entity spawnBullet(SpawnData data) {
            Entity bullet = entityBuilder()
                    .type(TowerDefenseType.BULLET)
                    .from(data)
                    .viewWithBBox(new Rectangle(15, 5, Color.DARKGREY))
                    .with(new CollidableComponent(true))
                    .with(new OffscreenCleanComponent(), new BulletComponent(data.get("damage"),data.get("delay")))
                    .build();
            if (data.hasKey("burn damage")){
                bullet.removeComponent(BulletComponent.class);
                bullet.addComponent(new BulletComponent(data.get("damage"),data.get("burn damage"),data.get("delay")));
            }
            return bullet;
    }
}
