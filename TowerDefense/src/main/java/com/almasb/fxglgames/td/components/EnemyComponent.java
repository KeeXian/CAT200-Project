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

import java.sql.SQLOutput;
import java.util.List;

public class EnemyComponent extends Component{

    private List<Point2D> waypoints;
    private Point2D nextWaypoint;

    private AnimatedTexture texture;
    private AnimationChannel animidle,animwalk,animidle_left,animwalk_left;


    private double speed;

    //Initialise the speed and texture of the enemy entity
    public EnemyComponent(double speed,int index,int level){
        this.speed=speed*60*2;
        setTexture(index, level);
        texture=new AnimatedTexture(animidle);
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture); //Set the texture to be viewed in the game
        waypoints = ((TowerDefenseApp) FXGL.getApp()).getWaypoints(); //Get list of waypoints
        nextWaypoint = waypoints.remove(0); //Get the next waypoint an remove it from the list
    }

    @Override
    public void onUpdate(double tpf) {

        if(nextWaypoint.equals(new Point2D(700,300))){ //Change enemy animation direction
            texture.loopAnimationChannel(animwalk_left);
        }
        else if(nextWaypoint.equals(new Point2D(50,450))){ //Change enemy animation direction
            texture.loopAnimationChannel(animwalk);
        }

        if (texture.getAnimationChannel() == animidle) { //Loop the animation
            texture.loopAnimationChannel(animwalk);
        }
        else if(texture.getAnimationChannel()==animidle_left) //Loop the animation
            texture.loopAnimationChannel(animwalk_left);

        //Calculate the speed of enemy at each waypoint
        Point2D velocity = nextWaypoint.subtract(entity.getPosition())
                .normalize()
                .multiply(speed);

        entity.translate(velocity); //Enemy move with the calculated speed

        if (nextWaypoint.distance(entity.getPosition()) < speed) {
            entity.setPosition(nextWaypoint);

            if (!waypoints.isEmpty()) {
                nextWaypoint = waypoints.remove(0);
            } else {
                FXGL.getEventBus().fireEvent(new EnemyReachedGoalEvent()); //Trigger the event when enemy reach the goal
            }
        }
    }

    //Set the texture of the enemy according to the type of enemy and its level
    public void setTexture(int index,int level){
        String img1="",img2="";
        boolean valid=true;
        if(index==1){
            switch (level){
                case 1:
                    img1="orc1_small.png";
                    img2="orc1_left_small.png";
                    break;
                case 2:
                    img1="orc2_small.png";
                    img2="orc2_left_small.png";
                    break;
                case 3:
                    img1="orc3_small.png";
                    img2="orc3_left_small.png";
                    break;
                default: valid=false;
            }
        }
        else if(index==2){
            switch (level){
                case 1:
                    img1="level1sheet_small.png";
                    img2="level1sheet_left_small.png";
                    break;
                case 2:
                    img1="level2sheet_small.png";
                    img2="level2sheet_left_small.png";
                    break;
                default: valid=false;
            }
        }
        else if(index==3){
            img1="level2sheet_big.png";
            img2="level2sheet2_left_big.png";
        }
        else
            valid=false;

        if(valid){
            if(index==3){
                animidle=new AnimationChannel(new AssetLoader().loadImage(img1),4,75,80, Duration.seconds(1),1,1);
                animwalk=new AnimationChannel(new AssetLoader().loadImage(img1),4,75,80,Duration.seconds(1),0,3);
                animidle_left=new AnimationChannel(new AssetLoader().loadImage(img2),4,80,80, Duration.seconds(1),1,1);
                animwalk_left=new AnimationChannel(new AssetLoader().loadImage(img2),4,78,80,Duration.seconds(1),0,3);
            }
            else {
                animidle = new AnimationChannel(new AssetLoader().loadImage(img1), 4, 40, 40, Duration.seconds(1), 1, 1);
                animwalk = new AnimationChannel(new AssetLoader().loadImage(img1), 4, 40, 40, Duration.seconds(1), 0, 3);
                animidle_left = new AnimationChannel(new AssetLoader().loadImage(img2), 4, 40, 40, Duration.seconds(1), 1, 1);
                animwalk_left = new AnimationChannel(new AssetLoader().loadImage(img2), 4, 40, 40, Duration.seconds(1), 0, 3);
            }
        }
        else{
            System.out.println("Invalid index and level");
        }
    }

}
