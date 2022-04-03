package com.example.voxelcarving;

import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.io.FileInputStream;

public class ImagePlane {
    public Image image;
    public Box plane;
    public double theta;
    public double phi;
    public double r;
    public ImagePlane(String fileName, double theta, double phi){
        try {
            FileInputStream inputStream = new FileInputStream("src/main/resources/" + fileName);
            image = new Image(inputStream);

            double aspectRatio = image.getWidth() / (double) image.getHeight();
            plane = new Box(SceneConstants.SIZE * SceneConstants.RES, SceneConstants.SIZE * SceneConstants.RES / aspectRatio, 1);
            PhongMaterial imageMaterial = new PhongMaterial();
            imageMaterial.setDiffuseMap(image);
            plane.setMaterial(imageMaterial);

//            RealVector defaultPosition = new ArrayRealVector(new double[]{0, 0, -SceneConstants.IMAGE_DIST});
//            RealMatrix rotationMat = Projector.rotationMatrix(0, theta, phi);
//            RealVector rotatedPosition = rotationMat.operate(defaultPosition);

            this.theta = theta;
            this.phi = phi;
            this.r = SceneConstants.IMAGE_DIST;
            Translate position = new Translate(0, 0, -100);
            Rotate yRotation = new Rotate(-theta, new Point3D(0, 1, 0));
            Rotate xRotation = new Rotate(-phi, new Point3D(1, 0, 0));


            plane.getTransforms().addAll(xRotation, yRotation, position);

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
