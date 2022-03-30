package com.example.voxelcarving;
import javafx.scene.paint.Color;

import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.*;

import java.util.*;

public class VoxelGrid { // conatiner for voxels
    int res;
    double size;
    Voxel[][][] grid;

    public VoxelGrid(int res, double size, Voxel[][][] grid){
        this.res = res;
        this.size = size;
        this.grid = grid;
    }

    public VoxelGrid(int res, double size) {
        this(res, size, new Voxel[res][res][res]);
        for (int i = 0; i < res; i++) {
            for (int j = 0; j < res; j++) {
                for (int k = 0; k < res; k++) {
                    this.grid[i][j][k] = new Voxel(true, Color.rgb(i * 255/res, j * 255/res, k * 255/res, 0.3));
                }
            }
        }

    }

    Box getCube(int i, int j, int k){
        Box box = new Box(size, size, size);
        box.setTranslateX(i * size - size * res/2.0);
        box.setTranslateY(j * size - size * res/2.0);
        box.setTranslateZ(k * size - size * res/2.0);

        PhongMaterial material = new PhongMaterial(this.grid[i][j][k].col);
        box.setMaterial(material);
        return box;
    }

    void addAllToGroup(Group group) {
        List<Node> children = group.getChildren();
        children.clear();
        for (int i = 0; i < res; i++) {
            for (int j = 0; j < res; j++) {
                for (int k = 0; k < res; k++) {
                    if (this.grid[i][j][k].filled) {
                        children.add(this.getCube(i, j, k));
                    }
                }
            }
        }
    }

    VoxelGrid union(VoxelGrid other) {
        for (int i = 0; i < res; i++) {
            for (int j = 0; j < res; j++) {
                for (int k = 0; k < res; k++) {
                    this.grid[i][j][k].filled = this.grid[i][j][k].filled || other.grid[i][j][k].filled;
                }
            }
        }
        return this;
    }

    VoxelGrid intersect(VoxelGrid other) {
        for (int i = 0; i < res; i++) {
            for (int j = 0; j < res; j++) {
                for (int k = 0; k < res; k++) {
                    this.grid[i][j][k].filled = this.grid[i][j][k].filled && other.grid[i][j][k].filled;
                }
            }
        }
        return this;
    }

    void castRays(ArrayList<Ray> rays) { // fills in only boxes touched by rays
        for (int i = 0; i < this.res; i++) {
            for (int j = 0; j < this.res; j++) {
                for (int k = 0; k < this.res; k++) {
                    boolean hit = false;
                    Box cube = this.getCube(i, j, k);
                    for (Ray ray : rays){
                        hit = hit || ray.intersects(cube);
                    }
                    this.grid[i][j][k].filled = hit && this.grid[i][j][k].filled;
                }
            }
        }
    }
}
