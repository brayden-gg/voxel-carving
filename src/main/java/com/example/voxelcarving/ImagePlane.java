package com.example.voxelcarving;

import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.apache.commons.math3.linear.*;

import java.io.FileInputStream;

public class ImagePlane {
    public Image image;
    public Box plane;
    public double r;
    public ImagePlane(String fileName, double theta, double phi){
        try {
            FileInputStream inputStream = new FileInputStream(SceneConstants.FOLDER + fileName);
            image = new Image(inputStream);
        } catch (Exception e){
            System.out.println("No image could be found at at location '" + fileName + "'\n\n");
            System.out.println(e);
        }

        double aspectRatio = image.getHeight() / image.getWidth();
        plane = new Box(SceneConstants.SIZE * SceneConstants.RES, SceneConstants.SIZE * SceneConstants.RES * aspectRatio, 1);
        PhongMaterial imageMaterial = new PhongMaterial();
        imageMaterial.setDiffuseMap(image);
        plane.setMaterial(imageMaterial);

        this.r = SceneConstants.IMAGE_DIST;

        double thetaRad = theta * Math.PI / 180;
        double phiRad = phi * Math.PI / 180;

        // standard spherical to cartesian but adjusted for javafx coordinate
        plane.setTranslateX(r * Math.sin(thetaRad) * Math.cos(phiRad));
        plane.setTranslateZ(-r * Math.cos(thetaRad) * Math.cos(phiRad));
        plane.setTranslateY(-r * Math.sin(phiRad));


        // Convert from theta and phi to axis-angle
        // used equations from:
        // https://en.wikipedia.org/wiki/Axis%E2%80%93angle_representation#Log_map_from_SO(3)_to_%F0%9D%94%B0%F0%9D%94%AC(3)
        RealMatrix rotationMat = Projector.rotationMatrix(phiRad, thetaRad, 0);
        Point3D axis = new Point3D(
                rotationMat.getEntry(2, 1) - rotationMat.getEntry(1, 2),
                rotationMat.getEntry(0, 2) - rotationMat.getEntry(2, 0),
                rotationMat.getEntry(1, 0) - rotationMat.getEntry(0, 1));

        double angle = Math.acos((rotationMat.getTrace() - 1)/2);

        plane.setRotationAxis(axis);
        plane.setRotate(-angle * 180 / Math.PI);
    }
}
