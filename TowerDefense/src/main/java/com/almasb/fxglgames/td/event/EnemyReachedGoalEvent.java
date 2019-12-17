package com.almasb.fxglgames.td.event;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * an event in which enemy has reached the goal
 */
public class EnemyReachedGoalEvent extends Event {

    public static final EventType<EnemyReachedGoalEvent> ANY
            = new EventType<>(Event.ANY, "EnemyReachedGoalEvent");

    public EnemyReachedGoalEvent() {
        super(ANY);
    }
}