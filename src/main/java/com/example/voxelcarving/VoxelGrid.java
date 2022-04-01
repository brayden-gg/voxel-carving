package com.example.voxelcarving;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.*;
import javafx.scene.shape.CullFace;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

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

    private Box getCube(int i, int j, int k){
        Box box = new Box(size, size, size);
        box.setTranslateX(i * size - size * res/2.0);
        box.setTranslateY(j * size - size * res/2.0);
        box.setTranslateZ(k * size - size * res/2.0);

        PhongMaterial material = new PhongMaterial(this.grid[i][j][k].getColor());
        box.setMaterial(material);
        return box;
    }

    public void addAllToGroup(Group group) {
        List<Node> children = group.getChildren();
        children.clear();
        for (int i = 0; i < res; i++) {
            for (int j = 0; j < res; j++) {
                for (int k = 0; k < res; k++) {
                    if (this.grid[i][j][k].getFilled()) {
                        children.add(this.getCube(i, j, k));
                    }
                }
            }
        }
    }

    public void castRays(ArrayList<Ray> rays) { // fills in only boxes touched by rays
        for (int i = 0; i < this.res; i++) {
            for (int j = 0; j < this.res; j++) {
                for (int k = 0; k < this.res; k++) {
                    boolean hit = false;
                    Box cube = this.getCube(i, j, k);
                    for (Ray ray : rays){
                        hit = hit || ray.intersects(cube);
                    }
                    this.grid[i][j][k].setFilled(hit && this.grid[i][j][k].getFilled());
                }
            }
        }
    }

    public void correlateVoxels(Box plane){
        PhongMaterial material = (PhongMaterial)plane.getMaterial();
        Image image = material.getDiffuseMap();

        PixelReader pixels = image.getPixelReader();

        double rotationAngle = plane.getRotate() / 180.0 * Math.PI;


        Projector projector = new Projector(
                plane.getTranslateX(), plane.getTranslateY(), plane.getTranslateZ(),
                0, rotationAngle, 0,
                plane.getWidth() / 2, plane.getHeight() / 2,
                300);

        for (int i = 0; i < this.res; i++) {
            for (int j = 0; j < this.res; j++) {
                for (int k = 0; k < this.res; k++) {
                    Box cube = this.getCube(i, j, k);
                    RealVector worldPoint = new ArrayRealVector(new double[]{
                            cube.getTranslateX(),
                            cube.getTranslateY(),
                            cube.getTranslateZ()});
                    RealVector projectedPoint = projector.projectPoint(worldPoint);

                    int x = (int)Math.round(projectedPoint.getEntry(0));
                    int y = (int)Math.round(projectedPoint.getEntry(1));

                    Color color;

                    if (x >= image.getWidth() || x < 0 || y >= image.getHeight() || y < 0){
                        color = Color.TRANSPARENT;
                    } else {
                        color = pixels.getColor(x, y);
                    }

                    this.grid[i][j][k].setColor(color);
                }
            }
        }
    }

}
