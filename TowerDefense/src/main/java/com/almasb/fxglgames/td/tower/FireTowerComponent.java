package com.almasb.fxglgames.td.tower;

/**
 * contains information regarding the fire tower
 * assigned to the tower spawn through the
 * with(FireTowerComponent.class) function
 */
public class FireTowerComponent extends TowerDataComponent {
    private int burnDamage;
    public FireTowerComponent(){
        super(4,7800,0.8,5*80,6000);
        burnDamage=2;
    }

    @Override
    public void upgradeTower(){
        super.upgradeTower();
        burnDamage=burnDamage*2;
    }
    public int getBurnDamage(){
        return burnDamage;
    }
}
