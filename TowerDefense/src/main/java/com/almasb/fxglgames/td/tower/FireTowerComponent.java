package com.almasb.fxglgames.td.tower;

public class FireTowerComponent extends TowerDataComponent {
    private int burnDamage;
    public FireTowerComponent(){
        this.damage=4;
        burnDamage=2;
    }
    public int getBurnDamage(){
        return burnDamage;
    }
}
