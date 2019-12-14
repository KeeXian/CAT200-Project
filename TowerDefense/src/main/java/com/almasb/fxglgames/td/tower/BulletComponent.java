package com.almasb.fxglgames.td.tower;

import com.almasb.fxgl.entity.component.Component;

public class BulletComponent extends Component {
    private int damage;
    private int lingerDamage;
    public BulletComponent(int damage){
        this.damage=damage;
        this.lingerDamage=0;
    }
    public BulletComponent(int damage, int lingerDamage){
        this.damage=damage;
        this.lingerDamage=lingerDamage;
    }
    public int getDamage() {
        return damage;
    }
    public int getLingerDamage(){
        return lingerDamage;
    }
}
