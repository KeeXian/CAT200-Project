package com.almasb.fxglgames.td.tower;

public class FireTowerComponent extends TowerDataComponent {
    private int burnDamage;
    public FireTowerComponent(){
        this.damage=4;
        burnDamage=2;
        this.price=7800;
        this.attackDelay=0.8;
        this.speed=5*60;
    }
    public int getBurnDamage(){
        return burnDamage;
    }
}
