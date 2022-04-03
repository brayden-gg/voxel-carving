package com.example.voxelcarving;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.scene.input.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Main extends Application {

    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        boolean is3DSupported = Platform.isSupported(ConditionalFeature.SCENE3D);
        if(!is3DSupported) {
            System.out.println("Sorry, 3D is not supported in JavaFX on this platform.");
            return;
        }


        Group group = new Group();
        group.translateXProperty().set(SceneConstants.WIDTH / 2.0);
        group.translateYProperty().set(SceneConstants.HEIGHT / 2.0);
        group.translateZProperty().set(SceneConstants.IMAGE_DIST);

        PerspectiveCamera camera = new PerspectiveCamera();

        Scene scene = new Scene(group, SceneConstants.WIDTH, SceneConstants.HEIGHT); // true);
        scene.setFill(Color.BLUE);
        scene.setCamera(camera);


        ArrayList<ImagePlane> images = new ArrayList<ImagePlane>();
        ImagePlane leftImage = new ImagePlane(SceneConstants.FOLDER + "/left.png", -90, 0);
//        leftImage.setTranslateX(-100);
//        leftImage.setRotationAxis(new Point3D(0, 1, 0));
//        leftImage.setRotate(90);
        images.add(leftImage);

        ImagePlane frontLeftImage = new ImagePlane(SceneConstants.FOLDER + "/front-left.png", -45, 0);
//        frontLeftImage.setTranslateX(-100 * Math.sqrt(2) / 2);
//        frontLeftImage.setTranslateZ(-100 * Math.sqrt(2) / 2);
//        frontLeftImage.setRotationAxis(new Point3D(0, 1, 0));
//        frontLeftImage.setRotate(45);
        images.add(frontLeftImage);

        ImagePlane frontImage = new ImagePlane(SceneConstants.FOLDER + "/front.png", 0, 0);
//        frontImage.setTranslateZ(-100);
        images.add(frontImage);

        ImagePlane frontRightImage = new ImagePlane(SceneConstants.FOLDER + "/front-right.png", 45, 0);
//        frontRightImage.setTranslateX(100 * Math.sqrt(2) / 2);
//        frontRightImage.setTranslateZ(-100 * Math.sqrt(2) / 2);
//        frontRightImage.setRotationAxis(new Point3D(0, 1, 0));
//        frontRightImage.setRotate(-45);
        images.add(frontRightImage);

        ImagePlane rightImage = new ImagePlane(SceneConstants.FOLDER + "/right.png", 90, 0);
//        rightImage.setTranslateX(100);
//        rightImage.setRotationAxis(new Point3D(0, 1, 0));
//        rightImage.setRotate(-90);
        images.add(rightImage);

        ImagePlane frontTopImage = new ImagePlane(SceneConstants.FOLDER + "/front-top.png", 0, 22.5);
//        frontTopImage.setTranslateZ(-100 * Math.cos(Math.PI / 8));
//        frontTopImage.setTranslateY(-100 * Math.sin(Math.PI / 8));
//        frontTopImage.setRotationAxis(new Point3D(1, 0, 0));
//        frontTopImage.setRotate(-22.5);
        images.add(frontTopImage);

        ImagePlane topImage = new ImagePlane(SceneConstants.FOLDER + "/top.png", 0, 45);
//        topImage.setTranslateZ(-100 * Math.cos(Math.PI / 4));
//        topImage.setTranslateY(-100 * Math.sin(Math.PI / 4));
//        topImage.setRotationAxis(new Point3D(1, 0, 0));
//        topImage.setRotate(-45);
        images.add(topImage);


        VoxelGrid voxels = new VoxelGrid(SceneConstants.RES, SceneConstants.SIZE);

        calculateVoxels(scene, group, images, voxels);

//        scene.setOnMouseMoved(event -> {
//            double mouseX = event.getSceneX() - WIDTH / 2;
//            double mouseY = event.getSceneY() - WIDTH / 2;
//            image.setTranslateX(mouseX);
//            image.setTranslateZ(mouseY);
//            System.out.println(mouseX + ", " + mouseY);
////            double theta = mouseX;
////            image.setTranslateX(Math.cos(theta / 180.0 * Math.PI) * mouseY);
////            image.setTranslateZ(Math.sin(-theta / 180.0 * Math.PI) * mouseY);
////            image.setRotationAxis(new Point3D(0, 1, 0));
////            image.setRotate(theta - 90);
//            calculateVoxels(scene, group, image, voxels);
//        });



        handleMouseDrag(group, scene, primaryStage);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Voxel Carving");

        primaryStage.show();
    }



    private void calculateVoxels(Scene scene, Group group, ArrayList<ImagePlane> images, VoxelGrid voxels) {
        Group voxelGroup = new Group();
//        ArrayList<Ray> im1Rays = new ArrayList<Ray>();
//        for (double theta = 0; theta < Math.PI * 2; theta += 0.2){
//            for(int r = 0; r < 30; r += 5){
//                im1Rays.add(new Ray(r * Math.cos(theta), r * Math.sin(theta), 0, 0, 0, 1));
//            }
//        }
//
//        ArrayList<Ray> im2Rays = new ArrayList<Ray>();
//        for (double theta = 0; theta < Math.PI * 2; theta += 0.2){
//            for(int r = 0; r < 30; r += 5){
//                im2Rays.add(new Ray(-50, r * Math.sin(theta), r * Math.cos(theta), 1, 0, 0));
//            }
//        }


//        voxels.castRays(im1Rays);
//        voxels.castRays(im2Rays);
        voxels.correlateVoxels(images);
        voxels.addAllToGroup(voxelGroup);



        group.getChildren().clear();

        for (ImagePlane image : images) {
            group.getChildren().add(image.plane);
        }
        group.getChildren().add(voxelGroup);

    }

    void handleMouseDrag(Group group, Scene scene, Stage stage){
        Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
        Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
        group.getTransforms().addAll(xRotate, yRotate);
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });

        scene.setOnMouseDragged(event -> {
            angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
            angleY.set(anchorAngleY + (anchorX - event.getSceneX()));
        });

        scene.addEventHandler(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            group.translateZProperty().set(group.getTranslateZ() + delta);
        });
    }
}