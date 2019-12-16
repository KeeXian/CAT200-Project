package com.almasb.fxglgames.td.components;

import com.almasb.fxgl.app.AssetLoader;
import com.almasb.fxgl.core.View;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.ViewComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxglgames.td.TowerDefenseApp;
import com.almasb.fxglgames.td.event.EnemyReachedGoalEvent;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.util.List;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class EnemyComponent extends Component{

    private List<Point2D> waypoints;
    private Point2D nextWaypoint;

    private AnimatedTexture texture;
    private AnimationChannel animidle,animwalk,animidle_left,animwalk_left;


    private double speed;

    public EnemyComponent(){
        animidle=new AnimationChannel(new AssetLoader().loadImage("level1sheet_small.png"),4,40,40, Duration.seconds(1),1,1);
        animwalk=new AnimationChannel(new AssetLoader().loadImage("level1sheet_small.png"),4,40,40,Duration.seconds(1),0,3);
        animidle_left=new AnimationChannel(new AssetLoader().loadImage("level1sheet_left_small.png"),4,40,40, Duration.seconds(1),1,1);
        animwalk_left=new AnimationChannel(new AssetLoader().loadImage("level1sheet_left_small.png"),4,40,40,Duration.seconds(1),0,3);
        texture=new AnimatedTexture(animidle);
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);
        waypoints = ((TowerDefenseApp) FXGL.getApp()).getWaypoints();
        nextWaypoint = waypoints.remove(0);
    }

    public EnemyComponent(double speed, int index){
        this.speed = speed * 60 * 2;
        if(index==3){
            animidle = new AnimationChannel(new AssetLoader().loadImage("level1sheet_small.png"), 4, 40, 40, Duration.seconds(1), 1, 1);
            animwalk = new AnimationChannel(new AssetLoader().loadImage("level1sheet_small.png"), 4, 40, 40, Duration.seconds(1), 0, 3);
            animidle_left = new AnimationChannel(new AssetLoader().loadImage("level1sheet_left_small.png"), 4, 40, 40, Duration.seconds(1), 1, 1);
            animwalk_left = new AnimationChannel(new AssetLoader().loadImage("level1sheet_left_small.png"), 4, 40, 40, Duration.seconds(1), 0, 3);
            texture = new AnimatedTexture(animidle);
            texture.setFitHeight(80);
            texture.setFitWidth(80);
        }
        else {
            animidle = new AnimationChannel(new AssetLoader().loadImage("level1sheet_small.png"), 4, 40, 40, Duration.seconds(1), 1, 1);
            animwalk = new AnimationChannel(new AssetLoader().loadImage("level1sheet_small.png"), 4, 40, 40, Duration.seconds(1), 0, 3);
            animidle_left = new AnimationChannel(new AssetLoader().loadImage("level1sheet_left_small.png"), 4, 40, 40, Duration.seconds(1), 1, 1);
            animwalk_left = new AnimationChannel(new AssetLoader().loadImage("level1sheet_left_small.png"), 4, 40, 40, Duration.seconds(1), 0, 3);
            texture = new AnimatedTexture(animidle);
        }
    }

    @Override
    public void onUpdate(double tpf) {

        if(nextWaypoint.equals(new Point2D(700,300))){
            texture.loopAnimationChannel(animwalk_left);
        }
        else if(nextWaypoint.equals(new Point2D(50,450))){
            texture.loopAnimationChannel(animwalk);
        }

        if (texture.getAnimationChannel() == animidle) {
            texture.loopAnimationChannel(animwalk);
        }
        else if(texture.getAnimationChannel()==animidle_left)
            texture.loopAnimationChannel(animwalk_left);


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
