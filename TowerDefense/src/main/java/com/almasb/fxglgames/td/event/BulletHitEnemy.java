package com.almasb.fxglgames.td.event;

import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxglgames.td.components.EnemyComponent;
import javafx.event.Event;
import javafx.event.EventType;

/**
 * an event that will be added to the event bus
 * whenever a bullet hits an enemy
 */
public class BulletHitEnemy extends Event {
    private Entity enemy;
    public static final EventType<BulletHitEnemy> ANY = new EventType<>(Event.ANY, "Enemy Hit");

    public Entity getEnemy(){ return enemy;}
    public BulletHitEnemy(Entity enemy) {
        super(ANY);
        this.enemy=enemy;
    }
}
