package com.almasb.fxglgames.td.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.time.LocalTimer;
import com.almasb.fxglgames.td.TowerDefenseApp;
import com.almasb.fxglgames.td.TowerDefenseType;
import com.almasb.fxglgames.td.Config;
import com.almasb.fxglgames.td.enemy.EnemyDataComponent;
import com.almasb.fxglgames.td.tower.FireTowerComponent;
import com.almasb.fxglgames.td.tower.TowerDataComponent;
import javafx.geometry.Point2D;
import javafx.util.Duration;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class TowerComponent extends Component {

    private LocalTimer shootTimer;

    @Override
    public void onAdded() {
        TowerDefenseApp.score.set( TowerDefenseApp.score.get()-1);
        shootTimer = FXGL.newLocalTimer();
        shootTimer.capture();
    }

    @Override
    public void onUpdate(double tpf) {

        if (shootTimer.elapsed(Duration.seconds(0.5))) {
            FXGL.getGameWorld()
                    .getClosestEntity(entity, e -> e.isType(TowerDefenseType.ENEMY))
                    .ifPresent(nearestEnemy -> {
                        shoot(nearestEnemy);
                        shootTimer.capture();
                    });
        }
    }

    private void shoot(Entity enemy) {
        Point2D position = getEntity().getPosition();
        Point2D direction = enemy.getPosition().add(10,30).subtract(position);
        Entity bullet;
        if(getEntity().hasComponent(FireTowerComponent.class)) {
            bullet = FXGL.spawn("Bullet", new SpawnData(position).
                    put("damage", getEntity().getComponent(FireTowerComponent.class).getDamage()).
                    put("burn damage",getEntity().getComponent(FireTowerComponent.class).getBurnDamage()));
        } else {
            bullet = FXGL.spawn("Bullet", new SpawnData(position).
                    put("damage", getEntity().getComponent(TowerDataComponent.class).getDamage()));
        }
        bullet.addComponent(new ProjectileComponent(direction, Config.BULLET_SPEED));
    }
}
