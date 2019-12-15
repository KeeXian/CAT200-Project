package com.almasb.fxglgames.td.tower;

import com.almasb.fxgl.entity.component.Component;

public class BulletComponent extends Component {
    private int damage;
    private int lingerDamage;
    private double delay;

    public BulletComponent(int damage,double delay){
        this.damage=damage;
        this.delay=delay;
        this.lingerDamage=0;
    }
    public BulletComponent(int damage, int lingerDamage, double delay){
        this.damage=damage;
        this.lingerDamage=lingerDamage;
        this.delay=delay;
    }
    public int getDamage() {
        return damage;
    }
    public int getLingerDamage(){
        return lingerDamage;
    }
    public double getDelay() {
        return delay;
    }

}
