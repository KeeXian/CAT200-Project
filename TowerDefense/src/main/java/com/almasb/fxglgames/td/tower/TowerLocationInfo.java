package com.almasb.fxglgames.td.tower;

import javafx.geometry.Rectangle2D;

public class TowerLocationInfo {
    private Rectangle2D rect;
    private boolean occupied;

    public TowerLocationInfo(Rectangle2D rect){
        this.rect=rect;
        occupied=false;
    }

    public Rectangle2D getRect() {
        return rect;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }
}
