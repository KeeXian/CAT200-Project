package com.almasb.fxglgames.td.tower;

public class FireTowerComponent extends TowerDataComponent {
    private int burnDamage;
    public FireTowerComponent(){
        super(4,7800,0.8,5*60,4500);
        burnDamage=2;
    }
    public int getBurnDamage(){
        return burnDamage;
    }
}
