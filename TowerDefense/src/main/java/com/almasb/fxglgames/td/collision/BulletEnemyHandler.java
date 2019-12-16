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
 * @author Almas Baimagambetov (almaslvl@gmail.com)
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
        FXGL.getEventBus().fireEvent(new BulletHitEnemy(enemy));
        Music music = FXGL.getAssetLoader().loadMusic("coin.mp3");
        if(enemy.getComponent(EnemyDataComponent.class).getHp()<=0) {
            FXGL.getAudioPlayer().stopMusic(music);
            FXGL.getAudioPlayer().playMusic(music);
            enemy.removeFromWorld();
        }
        else {
            enemy.getComponent(EnemyDataComponent.class).updateHp(bullet.getComponent(BulletComponent.class).getDamage());
            if(bullet.getComponent(BulletComponent.class).getLingerDamage()>0) {
                int x = bullet.getComponent(BulletComponent.class).getLingerDamage();
                FXGL.getGameTimer().runAtInterval(()-> {
                            if(enemy.hasComponent(EnemyDataComponent.class))
                                enemy.getComponent(EnemyDataComponent.class).updateHp(x);
                            else
                                enemy.removeFromWorld();
                        }
                        ,Duration.millis(1000),3);
            }
        }

    }

    public void setIndex(int index) {
        this.index=index;
    }
}
