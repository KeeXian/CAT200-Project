package com.almasb.fxglgames.td.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.time.LocalTimer;
import com.almasb.fxglgames.td.TowerDefenseType;
import com.almasb.fxglgames.td.Config;
import com.almasb.fxglgames.td.enemy.EnemyDataComponent;
import com.almasb.fxglgames.td.tower.FireTowerComponent;
import com.almasb.fxglgames.td.tower.TowerDataComponent;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

import java.util.List;

/**
 * manages the changes that occured onto the tower
 */
public class TowerComponent extends Component {

    private LocalTimer shootTimer;
    private double delay;
    private double speed;
    private Rectangle2D shootRange;

    public TowerComponent(double delay, double speed, Rectangle2D shootRange){
        this.delay=delay;
        this.speed=speed;
        this.shootRange=shootRange;
    }

    //gets the local timer where the tower is spawn
    @Override
    public void onAdded() {
        shootTimer = FXGL.newLocalTimer();
        shootTimer.capture();
    }

    /**
     * the following updates the tower entity
     * after certain time, the tower shoots out a projectile
     * it also checks whether there are enemies present in its shooting range
     * if there is more than one, it shoots the one closest to it
     * tpf stands for time per frame
     */
    @Override
    public void onUpdate(double tpf) {

        if (shootTimer.elapsed(Duration.seconds(delay))) {
            if(containsType(FXGL.getGameWorld().getEntitiesInRange(shootRange))){
                FXGL.getGameWorld()
                        .getClosestEntity(entity, e -> e.isType(TowerDefenseType.ENEMY))
                        .ifPresent(nearestEnemy -> {
                            shoot(nearestEnemy);
                            shootTimer.capture();
                        });
            }
        }
    }

    /**
     * spawns a bullet with the data assigned to it
     * through the put function
     */
    private void shoot(Entity enemy) {
        Point2D position = getEntity().getPosition().add(30,0);
        Point2D direction = enemy.getPosition().add(20,20).subtract(position);
        Entity bullet;
        if(getEntity().hasComponent(FireTowerComponent.class)) {
            bullet = FXGL.spawn("Bullet", new SpawnData(position).
                    put("type",4).
                    put("damage", getEntity().getComponent(FireTowerComponent.class).getDamage()).
                    put("burn damage",getEntity().getComponent(FireTowerComponent.class).getBurnDamage()).
                    put("delay",getEntity().getComponent(FireTowerComponent.class).getAttackDelay()));
        } else {
            bullet = FXGL.spawn("Bullet", new SpawnData(position).
                    put("type",getEntity().getComponent(TowerDataComponent.class).getType()).
                    put("damage", getEntity().getComponent(TowerDataComponent.class).getDamage()).
                    put("delay",getEntity().getComponent(TowerDataComponent.class).getAttackDelay()));
        }
        bullet.addComponent(new ProjectileComponent(direction, speed));
    }

    /**
     * detects the type of entity that is within its shooting range
     * if it detects an enemy, the function returns true
     */
    public boolean containsType(List<Entity> list){
        boolean found = false;
        for(Entity entity : list){
            if(entity.isType(TowerDefenseType.ENEMY))
                found = true;
        }
        return found;
    }
}
