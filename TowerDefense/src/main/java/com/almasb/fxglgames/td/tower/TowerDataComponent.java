package com.almasb.fxglgames.td.tower;

import com.almasb.fxgl.app.AssetLoader;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxglgames.td.components.TowerComponent;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;

import static com.almasb.fxgl.dsl.FXGL.getAssetLoader;
import static com.almasb.fxgl.dsl.FXGL.getInput;

/**
 * contains the information regarding each tower
 * is assigned to the tower whenever it is spawn
 * through the "with(TowerDataComponent.class)"
 */
public class TowerDataComponent extends Component {

    private int type;
    private int hp;
    private int level;
    protected int damage;
    protected double attackDelay;
    protected double price;
    protected double speed;
    protected double upgradeCost;
    public Label levelLabel = new Label();
    public Rectangle2D shootRange;
    private double startX;
    private double startY;
    private double rangeWidth;
    private double rangeHeight;

    public void setLabel(){
        this.levelLabel.setText("Level : " + level);
    }

    public TowerDataComponent(int damage, double price){
        hp=0;
        this.damage=damage;
        attackDelay=1.5;
        this.price=price;
        this.level=1;
        setLabel();
    }

    public TowerDataComponent(int damage, double price, double attackDelay, double speed, double upgradeCost){
        hp=0;
        this.damage=damage;
        this.attackDelay=attackDelay;
        this.price=price;
        this.speed=speed;
        this.upgradeCost = upgradeCost;
        this.level=1;
        setLabel();
    }

    public TowerDataComponent(){
        hp=0;
        damage=0;
        attackDelay=1.5;
        price=0;
        this.level=1;
        setLabel();
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
    public double getPrice() {return price;}
    public double getSpeed() {
        return speed;
    }
    public int getType() {
        return type;
    }
    public int getLevel() {
        return level;
    }
    public double getUpgradeCost() {
        return upgradeCost;
    }

    public void createShootingRange(double x, double y, double rangeWidth, double rangeHeight){
        startX = x;
        startY = y;
        this.rangeWidth=rangeWidth;
        this.rangeHeight=rangeHeight;
        shootRange=new Rectangle2D(startX, startY, this.rangeWidth, this.rangeHeight);
    }

    public static TowerDataComponent makeTower(int type) {
        TowerDataComponent newTower;
        if (type == 1) {  //small rock tower
            newTower = new TowerDataComponent(2, 1000,0.8,5*80,700);
            newTower.createShootingRange(getInput().getMouseXWorld()-300,getInput().getMouseYWorld()-300, 600,600);
        }
        else if (type == 2) { //big rock tower
            newTower = new TowerDataComponent(3, 2000, 1.0, 5 * 50, 1600);
            newTower.createShootingRange(getInput().getMouseXWorld()-250,getInput().getMouseYWorld()-250, 500,500);
        }
        else if (type == 3) { //metal ball tower
            newTower = new TowerDataComponent(6, 4000, 1.2, 5 * 40, 2550);
            newTower.createShootingRange(getInput().getMouseXWorld()-200,getInput().getMouseYWorld()-200, 400,400);
        }
        else { //fire tower
            newTower = new FireTowerComponent();
            newTower.createShootingRange(getInput().getMouseXWorld()-300,getInput().getMouseYWorld()-300, 600,600);
        }
        newTower.type=type;
        return newTower;
    }

    public void upgradeTower(){
        if(level<3) {
            level++;
            damage = 2 * damage;
            price = 1.5 * price;
            upgradeCost = 1.5 * upgradeCost;
            setLabel();
            createShootingRange(startX*1.05,startY*1.05,rangeWidth*1.05,rangeHeight*1.05);
        }
    }
}
