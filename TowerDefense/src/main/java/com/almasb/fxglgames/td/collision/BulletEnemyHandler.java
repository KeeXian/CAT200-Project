package com.almasb.fxglgames.td.collision;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.time.Timer;
import com.almasb.fxgl.time.TimerAction;
import com.almasb.fxglgames.td.components.EnemyComponent;
import com.almasb.fxglgames.td.enemy.EnemyDataComponent;
import com.almasb.fxglgames.td.TowerDefenseType;
import com.almasb.fxglgames.td.event.BulletHitEnemy;
import com.almasb.fxglgames.td.tower.BulletComponent;
import javafx.animation.FadeTransition;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * when the BulletHitEnemy event is fired, the following
 * function in this class will be called
 */
public class BulletEnemyHandler extends CollisionHandler {

    private int index;
    public BulletEnemyHandler() {
        super(TowerDefenseType.BULLET, TowerDefenseType.ENEMY);
    }

    @Override
    protected void onCollisionBegin(Entity bullet, Entity enemy) {
        // TODO: add HP/Damage system
        bullet.removeFromWorld();
        //assigns the enemy entity to the one that is involved in the
        //BulletHitEnemy event
        FXGL.getEventBus().fireEvent(new BulletHitEnemy(enemy));
        //reduces the hp of the enemy
        enemy.getComponent(EnemyDataComponent.class).updateHp(bullet.getComponent(BulletComponent.class).getDamage());
        //checks whether the bullet is shot from a fire tower
        if (bullet.getComponent(BulletComponent.class).getLingerDamage() > 0) {
            int x = bullet.getComponent(BulletComponent.class).getLingerDamage();
            TimerAction action=FXGL.getGameTimer().runAtInterval(() -> {
                if (enemy.hasComponent(EnemyDataComponent.class))
                    enemy.getComponent(EnemyDataComponent.class).updateHp(x);
                }
                , Duration.millis(1000), 3);
        }
    }

    public void setIndex(int index) {
        this.index=index;
    }


}
