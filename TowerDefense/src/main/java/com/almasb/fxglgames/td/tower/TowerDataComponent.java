package com.almasb.fxglgames.td.tower;

import com.almasb.fxgl.entity.component.Component;

/**
 *  TODO: not a component
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class TowerDataComponent extends Component {

    private int hp;
    protected int damage;
    private double attackDelay;

    public TowerDataComponent(int damage){
        hp=0;
        this.damage=damage;
        attackDelay=1.5;
    }

    public TowerDataComponent(){
        hp=0;
        damage=0;
        attackDelay=1.5;
    }

    public int getHP() {
        return hp;
    }
    public int getDamage() {
        return damage;
    }
    public double getAttackDelay() {
        return attackDelay;
    }

    public static TowerDataComponent makeTower(int type) {
        TowerDataComponent newTower;
        if (type == 1)  //small rock tower
            newTower = new TowerDataComponent(2);
        else if (type == 2)  //big rock tower
            newTower = new TowerDataComponent(3);
        else if (type == 3)  //metal ball tower
            newTower = new TowerDataComponent(6);
        else  //fire tower
            newTower = new FireTowerComponent();
        return newTower;
    }
}
