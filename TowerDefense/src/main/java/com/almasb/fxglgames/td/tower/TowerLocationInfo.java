package com.almasb.fxglgames.td.tower;

import javafx.geometry.Rectangle2D;
/*
This class save the list of valid area for the tower placement
*/
public class TowerLocationInfo {
    private Rectangle2D rect;
    private boolean occupied;

    public TowerLocationInfo(Rectangle2D rect){
        this.rect=rect;
        occupied=false;
    }

    public Rectangle2D getRect() { ///Return the area
        return rect;
    }

    public boolean isOccupied() { //Return the status of the area
        return occupied;
    }

    public void setOccupied(boolean occupied) { //Set the status of the area
        this.occupied = occupied;
    }
}
