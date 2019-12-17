package com.almasb.fxglgames.td.enemy;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.ui.ProgressBar;
import javafx.scene.paint.Color;

/**
 * contains information for each type of enemy
 */
public class EnemyDataComponent extends Component {
    protected int hp;
    protected ProgressBar hpBar;
    protected double speed;
    protected double gold;
    protected int type;
    protected int score;

    public EnemyDataComponent(){
        hp=0;
        hpBar = new ProgressBar();
        hpBar.setMinValue(0);
        hpBar.setWidth(30);
        hpBar.setHeight(10);
        hpBar.setLabelVisible(false);
        hpBar.setFill(Color.GREEN.brighter());
        hpBar.setTraceFill(Color.RED.brighter());
    }

    public void setStat(int hp, double sp, int gold,int score){
        this.hp=hp;
        hpBar.setMaxValue(hp);
        hpBar.setCurrentValue(hp);
        speed=sp;
        this.gold=gold;
        this.score=score;
    }
    public int getHp(){
        return hp;
    }
    public void updateHp(int damage){
        hp -= damage;
        hpBar.setCurrentValue(hpBar.getCurrentValue()-damage);
    }
    public ProgressBar getHpBar(){ return hpBar;}
    public double getSpeed() {
        return speed;
    }
    public double getGold(){return gold;}
    public int getType() {
        return type;
    }
    public int getScore(){return score;}

    //Set the stat of the enemy
    public static EnemyDataComponent makeEnemy(int index, int lvl){
        EnemyDataComponent enemy = new EnemyDataComponent();
        enemy.type=index;
        if(index==1)
            enemy.setStat(10*lvl, 0.01, 400*lvl,lvl*300);
        else if(index==2)
            enemy.setStat(5*lvl, 0.02, 200*lvl,lvl*150);
        else if(index==3)
            enemy.setStat(1000,0.005,8000,10000);
        return enemy;
    }
}
