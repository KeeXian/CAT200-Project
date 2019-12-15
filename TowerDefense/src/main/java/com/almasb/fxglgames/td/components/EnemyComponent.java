package com.almasb.fxglgames.td.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxglgames.td.TowerDefenseApp;
import com.almasb.fxglgames.td.event.EnemyReachedGoalEvent;
import javafx.geometry.Point2D;

import java.util.List;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class EnemyComponent extends Component {

    private List<Point2D> waypoints;
    private Point2D nextWaypoint;

    private double speed;

    @Override
    public void onAdded() {
        waypoints = ((TowerDefenseApp) FXGL.getApp()).getWaypoints();

<<<<<<< Updated upstream
        nextWaypoint = waypoints.remove(0);
=======
    public EnemyComponent(double speed,int index,int level){
        this.speed=speed*60*2;
        animidle=new AnimationChannel(new AssetLoader().loadImage("level1sheet_small.png"),4,40,40, Duration.seconds(1),1,1);
        animwalk=new AnimationChannel(new AssetLoader().loadImage("level1sheet_small.png"),4,40,40,Duration.seconds(1),0,3);
        animidle_left=new AnimationChannel(new AssetLoader().loadImage("level1sheet_left_small.png"),4,40,40, Duration.seconds(1),1,1);
        animwalk_left=new AnimationChannel(new AssetLoader().loadImage("level1sheet_left_small.png"),4,40,40,Duration.seconds(1),0,3);
        texture=new AnimatedTexture(animidle);
>>>>>>> Stashed changes
    }

    @Override
    public void onUpdate(double tpf) {
        speed = tpf * 60 * 2;

        Point2D velocity = nextWaypoint.subtract(entity.getPosition())
                .normalize()
                .multiply(speed);

        entity.translate(velocity);

        if (nextWaypoint.distance(entity.getPosition()) < speed) {
            entity.setPosition(nextWaypoint);

            if (!waypoints.isEmpty()) {
                nextWaypoint = waypoints.remove(0);
            } else {

                FXGL.getEventBus().fireEvent(new EnemyReachedGoalEvent());
            }
        }
    }
}
