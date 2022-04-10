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

import java.util.*;

import static com.example.voxelcarving.SceneConstants.RES;
import static com.example.voxelcarving.SceneConstants.SIZE;

// a container for all the voxels
public class VoxelGrid {
    private Voxel[][][] grid;

    public VoxelGrid(Voxel[][][] grid){
        this.grid = grid;
    }

    public VoxelGrid() {
        this(new Voxel[RES][RES][RES]);

        // fill in grid with empty voxels
        for (int i = 0; i < RES; i++) {
            for (int j = 0; j < RES; j++) {
                for (int k = 0; k < RES; k++) {
                    this.grid[i][j][k] = new Voxel(false, Color.BLACK);
                }
            }
        }

    }

    // turns indices in the grid into a cube in the world space
    private Box getCube(int i, int j, int k){
        Box box = new Box(SIZE, SIZE, SIZE);
        box.setTranslateX(i * SIZE - SIZE * RES/2.0);
        box.setTranslateY(j * SIZE - SIZE * RES/2.0);
        box.setTranslateZ(k * SIZE - SIZE * RES/2.0);

        PhongMaterial material = new PhongMaterial(this.grid[i][j][k].getColor());
        box.setMaterial(material);
        return box;
    }

    // creates cubes from voxels and adds them to the grid
    public void addAllToGroup(Group group) {
        List<Node> children = group.getChildren();
        children.clear();
        for (int i = 0; i < RES; i++) {
            for (int j = 0; j < RES; j++) {
                for (int k = 0; k < RES; k++) {
                    if (this.grid[i][j][k].getFilled()) {
                        children.add(this.getCube(i, j, k));
                    }
                }
            }
        }
    }

    public void correlateVoxels(ArrayList<ImagePlane> planes){
        Image[] images = new Image[planes.size()];
        Projector[] projectors = new Projector[planes.size()];
        PixelReader[] pixels = new PixelReader[planes.size()];

        for (int i = 0; i < planes.size(); i++) {
            Box plane = planes.get(i).plane;
            Image image = planes.get(i).image;
            images[i] = image;
            pixels[i] = image.getPixelReader();

            // align projector with position and rotation of image plane
            RealVector position = new ArrayRealVector(new double[]{plane.getTranslateX(), plane.getTranslateY(), plane.getTranslateZ()});
            Rotate rotate = new Rotate(plane.getRotate(), plane.getRotationAxis());
            RealMatrix rotation = MatrixUtils.createRealMatrix(new double[][]{
                    {rotate.getMxx(), rotate.getMxy(), rotate.getMxz()},
                    {rotate.getMyx(), rotate.getMyy(), rotate.getMyz()},
                    {rotate.getMzx(), rotate.getMzy(), rotate.getMzz()}});
            projectors[i] = new Projector(position, rotation,
                    image.getWidth() / 2, image.getHeight() / 2,
                    image.getWidth() / 2);
        }

        for (int i = 0; i < RES; i++) {
            for (int j = 0; j < RES; j++) {
                for (int k = 0; k < RES; k++) {
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

    // find the average colored voxel of the matched colors,
    // if the standard deviation of the matched colors is below a threshold, return it
    // otherwise, return a
    public Voxel getCorrelation(Image[] images, PixelReader[] pixels, RealVector[] worldPoints, Projector[] projectors) {
        ArrayList<RealVector> foundDescriptors = new ArrayList<RealVector>();
        for (int i = 0; i < images.length; i++){
            RealVector projectedPoint = projectors[i].projectPoint(worldPoints[i]);

            int x = (int) Math.round(projectedPoint.getEntry(0));
            int y = (int) Math.round(projectedPoint.getEntry(1));

            if (x < images[i].getWidth() && x >= 0 && y < images[i].getHeight() && y >= 0){ // in FOV of image
                foundDescriptors.add(getDescriptor(pixels[i], x, y));
            } else {
                foundDescriptors.add(new ArrayRealVector(4));
            }
        }


        if (foundDescriptors.size() < 2){
            return new Voxel(false, Color.TRANSPARENT);
        }


        RealVector avgDescriptor = new ArrayRealVector(4);
        for (int i = 0; i < foundDescriptors.size(); i++){
            avgDescriptor = avgDescriptor.add(foundDescriptors.get(i));
        }

        avgDescriptor = avgDescriptor.mapDivide(foundDescriptors.size());

        double sumDistance = 0;
        for (int i = 0; i < foundDescriptors.size(); i++){
            double distance = avgDescriptor.getDistance(foundDescriptors.get(i));
            sumDistance += distance * distance;
        }

        double stdDev = Math.sqrt(sumDistance / foundDescriptors.size());

        if (stdDev < SceneConstants.THRESHOLD && avgDescriptor.getEntry(3) > SceneConstants.MIN_TRANSPARENCY){
            return new Voxel(true, Color.color(
                    avgDescriptor.getEntry(0),
                    avgDescriptor.getEntry(1),
                    avgDescriptor.getEntry(2),
                    avgDescriptor.getEntry(3)));
        }

        return new Voxel(false, Color.TRANSPARENT);

    }

    // currently just the color of the image at the point but could use some other descriptor
    private RealVector getDescriptor(PixelReader pixels, int x, int y){
        Color col = pixels.getColor(x, y);
        return new ArrayRealVector(new double[]{col.getRed(), col.getGreen(), col.getGreen(), col.getOpacity()});
    }

    // this was an interesting idea but did not work at all, implementation might also be buggy
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
