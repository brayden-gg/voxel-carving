package com.example.voxelcarving;

import javafx.scene.paint.Color;

public class Voxel {
    private Color col;
    private boolean filled;
    public Voxel(boolean filled, Color col){
        this.filled = filled;
        this.col = col;
    }

    public Voxel(){
        this.filled = false;
        this.col = Color.WHITE;
    }

    public void setFilled(boolean filled){
        this.filled = filled;
    }

    public void setColor(Color col){
        this.col = col;
    }

    public boolean getFilled(){
        return this.filled;
    }

    public Color getColor(){
        return this.col;
    }

}
