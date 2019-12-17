package com.almasb.fxglgames.td;

import com.almasb.fxgl.app.AssetLoader;
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
import com.almasb.fxglgames.td.tower.TowerLocationInfo;
import javafx.animation.FadeTransition;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.LinkedList;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * this class acts as a factory
 * it manages the creation of the in-game items
 * these include the enemy, tower and bullet
 */
public class TowerDefenseFactory implements EntityFactory {

    @Spawns("Enemy")
    public Entity spawnEnemy(SpawnData data) {
        EnemyDataComponent enemyDataComponent = EnemyDataComponent.makeEnemy(data.get("index"), data.get("level"));
        VBox vBox = new VBox();
        vBox.getChildren().addAll(enemyDataComponent.getHpBar());
        /**
         * the following expression creates an enemy type
         * with the data provided by the game world
         * in a vBox attached with components that provide different functionalities to the entity
         */
        Entity newEntity = entityBuilder()
                .type(TowerDefenseType.ENEMY)
                .from(data)
                .viewWithBBox(vBox)
                .with(new CollidableComponent(true), enemyDataComponent)
                .with(new EnemyComponent(enemyDataComponent.getSpeed(),data.get("index"),data.get("level")))
                .build();
        /**
         * adjusts the size of hitbox
         * this hitbox will return a true boolean value when a bullet hits the hitbox
         * then the collision handler will take over
         */
        if(data.get("index").equals(3))
            newEntity.getBoundingBoxComponent().addHitBox(new HitBox("BODY", new Point2D(20, 20), BoundingShape.box(40, 40)));
        else
            newEntity.getBoundingBoxComponent().addHitBox(new HitBox("BODY", new Point2D(20, 20), BoundingShape.box(30, 30)));
        return newEntity;
    }

    @Spawns("Tower")
    public Entity spawnTower(SpawnData data) {
        TowerDataComponent towerComponent = TowerDataComponent.makeTower(data.get("index"));
        //assigns the texture of towers
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
            VBox vBox = new VBox();
            vBox.getChildren().addAll(towerComponent.levelLabel,texture);
            Entity tower = entityBuilder()
                    .type(TowerDefenseType.TOWER)
                    .at((Point2D)data.get("Position"))
                    .from(data)
                    .viewWithBBox(vBox)
                    .with(new CollidableComponent(true), towerComponent)
                    .with(new TowerComponent(towerComponent.getAttackDelay(),towerComponent.getSpeed(),towerComponent.shootRange))
                    .build();
            //decreases the player's gold when spawn a new tower
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
        return tower;
    }

    @Spawns("Bullet")
    public Entity spawnBullet(SpawnData data) {
        //sets texture of bullet based on which type of tower spawn it
        Texture texture=null;
        if(data.get("type").equals(1))
            texture=new Texture(getAssetLoader().loadImage("small stone.png"));
        else if(data.get("type").equals(2))
            texture=new Texture(getAssetLoader().loadImage("big rock.png"));
        else if(data.get("type").equals(3))
            texture=new Texture(getAssetLoader().loadImage("metal_ball.png"));
        else if(data.get("type").equals(4))
            texture=new Texture(getAssetLoader().loadImage("fire_ball.png"));
        texture.setFitWidth(20);
        texture.setFitHeight(20);
            Entity bullet = entityBuilder()
                    .type(TowerDefenseType.BULLET)
                    .from(data)
                    .viewWithBBox(texture)
                    .with(new CollidableComponent(true))  //turns on the ability to collide with other entities
                    .with(new OffscreenCleanComponent(), new BulletComponent(data.get("damage"),data.get("delay")))
                    .build();
            //if the bullet is spawn from a fireball tower
            if (data.hasKey("burn damage")){
                bullet.removeComponent(BulletComponent.class);
                bullet.addComponent(new BulletComponent(data.get("damage"),data.get("burn damage"),data.get("delay")));
            }
            return bullet;
    }

}
