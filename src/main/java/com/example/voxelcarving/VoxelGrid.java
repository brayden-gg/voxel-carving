package com.example.voxelcarving;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;

import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.*;
import javafx.scene.shape.CullFace;
import javafx.scene.transform.Rotate;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.nio.ByteBuffer;
import java.util.*;

public class VoxelGrid { // conatiner for voxels
    private int res;
    private double size;
    private Voxel[][][] grid;
    private final double DISTANCE_THRESHOLD = 0.15;

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
                    this.grid[i][j][k] = new Voxel(false, Color.rgb(i * 255/res, j * 255/res, k * 255/res, 0.3));
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
        for (int i = 0; i < res; i++) {
            for (int j = 0; j < res; j++) {
                for (int k = 0; k < res; k++) {
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

    public void correlateVoxels(ArrayList<Box> planes){
        Image[] images = new Image[planes.size()];
        Projector[] projectors = new Projector[planes.size()];
        PixelReader[] pixels = new PixelReader[planes.size()];

        for (int i = 0; i < planes.size(); i++) {
            Box plane = planes.get(i);
            PhongMaterial material = (PhongMaterial) plane.getMaterial();
            Image image = material.getDiffuseMap();
            images[i] = image;
            pixels[i] = image.getPixelReader();

            double rotationAngle = plane.getRotate() / 180.0 * Math.PI;

            RealVector position = new ArrayRealVector(new double[]{plane.getTranslateX(), plane.getTranslateY(), plane.getTranslateZ()});
            Rotate rotate = new Rotate(plane.getRotate(), plane.getRotationAxis());
            RealMatrix rotation = MatrixUtils.createRealMatrix(new double[][]
                            {{rotate.getMxx(), rotate.getMxy(), rotate.getMxz()},
                            {rotate.getMyx(), rotate.getMyy(), rotate.getMyz()},
                            {rotate.getMzx(), rotate.getMzy(), rotate.getMzz()}});
            projectors[i] = new Projector(position, rotation,
                    image.getWidth() / 2, image.getHeight() / 2,
                    image.getWidth() / 2);
        }

        for (int i = 0; i < res; i++) {
            for (int j = 0; j < res; j++) {
                for (int k = 0; k < res; k++) {
                    Box cube = this.getCube(i, j, k);
                    RealVector[] worldPoints = new RealVector[images.length];

                    for (int l = 0; l < images.length; l++) {
                        worldPoints[l] = new ArrayRealVector(new double[]{
                                cube.getTranslateX(),
                                cube.getTranslateY(),
                                cube.getTranslateZ()});
                    }

                    this.grid[i][j][k] = getCorrelation(images, pixels, worldPoints, projectors);

                }
            }
        }
    }

    public Voxel getCorrelation(Image[] images, PixelReader[] pixels, RealVector[] worldPoints, Projector[] projectors) {
        ArrayList<Color> foundColors = new ArrayList<Color>();
//        ArrayList<RealVector> foundDescriptors = new ArrayList<RealVector>();
        for (int i = 0; i < images.length; i++){
            RealVector projectedPoint = projectors[i].projectPoint(worldPoints[i]);

            int x = (int) Math.round(projectedPoint.getEntry(0));
            int y = (int) Math.round(projectedPoint.getEntry(1));

            if (x < images[i].getWidth() && x >= 0 && y < images[i].getHeight() && y >= 0){ // in FOV of image
                foundColors.add(pixels[i].getColor(x, y));
//                foundDescriptors.add(getHOG(pixels[i], x, y));
            }
        }



        if (foundColors.size() < 2){
            return new Voxel(false, Color.TRANSPARENT);
        }


        RealVector avgCol = new ArrayRealVector(4);
//        RealVector avgDescriptor = new ArrayRealVector(8);
        for (int i = 0; i < foundColors.size(); i++){
            avgCol = avgCol.add(new ArrayRealVector(new double[]{
                    foundColors.get(i).getRed(),
                    foundColors.get(i).getGreen(),
                    foundColors.get(i).getBlue(),
                    foundColors.get(i).getOpacity()}));

//            avgDescriptor = avgDescriptor.add(foundDescriptors.get(i));
        }

        avgCol = avgCol.mapDivide(foundColors.size());
//        avgDescriptor = avgDescriptor.mapDivide(foundColors.size());

        double avgDistance = 0;
        for (int i = 0; i < foundColors.size(); i++){
            RealVector colVec = new ArrayRealVector(new double[]{
                    foundColors.get(i).getRed(),
                    foundColors.get(i).getGreen(),
                    foundColors.get(i).getBlue(),
                    foundColors.get(i).getOpacity()});

            avgDistance += avgCol.getDistance(colVec);
//            avgDistance += foundDescriptors.get(i).getDistance(avgDescriptor);
        }

        avgDistance /= foundColors.size();

        if (avgDistance < DISTANCE_THRESHOLD){
            return new Voxel(true, Color.color(avgCol.getEntry(0), avgCol.getEntry(1), avgCol.getEntry(2), avgCol.getEntry(3)));
        }

        return new Voxel(false, Color.TRANSPARENT);

    }

    // was an interesting idea but did not work at all
    private RealVector getHOG(PixelReader pixels, int x, int y){
        int sz = 4;
        double[] mags = new double[sz * sz];
        double[] dirs = new double[sz * sz];


        int startX = x - x % sz;
        int startY = y - y % sz;

        for (int i = 0; i < sz; i++){
            for (int j = 0; j < sz; j++){
                if (startX + i >= 1920 || startY + j >= 1080 || startX == 0 || startY == 0){
                    System.out.println("ERROR: (" + i + "," + j + ") >= (1820, 1080)");
                    continue;
                }
                double gradX = pixels.getColor(startX + i, startY + j).getBrightness() - pixels.getColor(startX + i - 1, startY + j).getBrightness();
                double gradY = pixels.getColor(startX + i, startY + j).getBrightness() - pixels.getColor(startX + i, startY + j - 1).getBrightness();
                mags[i * sz + j] = Math.sqrt(gradX * gradX + gradY * gradY);
                dirs[i * sz + j] = Math.atan2(gradY, gradX);
            }
        }


        double[] bins = {0, 0, 0, 0, 0, 0, 0, 0};


        for (int i = 0; i < mags.length; i++){
            int index = (int)Math.floor(dirs[i] / (2 * Math.PI) * 8) + 4;
            if (index >= 8 || index < 0){
                index = 0;
            }
            bins[index] += mags[i];
        }

        return new ArrayRealVector(bins);
    }



}
