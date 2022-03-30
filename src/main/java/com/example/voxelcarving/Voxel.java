package com.example.voxelcarving;

import javafx.scene.paint.Color;

public class Voxel {
    Color col;
    boolean filled;
    public Voxel(boolean filled, Color col){
        this.filled = filled;
        this.col = col;
    }

    public Voxel(){
        this.filled = false;
        this.col = Color.WHITE;
    }
}
