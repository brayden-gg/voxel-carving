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

import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.scene.input.*;
import java.io.IOException;
import java.util.*;
import java.nio.*;
import org.opencv.core.*;
import org.opencv.imgproc.*;
import org.opencv.utils.*;
import java.awt.image.BufferedImage;


public class Main extends Application {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;

    private static final int RES = 20;
    private static final double SIZE = 100.0 / RES;

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

//        Box box = new Box(100,100,100);

        Group group = new Group();
        group.translateXProperty().set(WIDTH / 2.0);
        group.translateYProperty().set(HEIGHT / 2.0);
        group.translateZProperty().set(-100);

        PerspectiveCamera camera = new PerspectiveCamera();

        Scene scene = new Scene(group, WIDTH, HEIGHT); // true);
        scene.setFill(Color.BLUE);
        scene.setCamera(camera);

        createVoxelGrid(group, scene);
        handleMouseDrag(group, scene, primaryStage);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Voxel Carving");

        primaryStage.show();
    }

    private void createVoxelGrid(Group group, Scene scene) {
        ArrayList<Ray> im1Rays = new ArrayList<Ray>();
        for (double theta = 0; theta < Math.PI * 2; theta += 0.2){
            for(int r = 0; r < 30; r += 5){
                im1Rays.add(new Ray(r * Math.cos(theta), r * Math.sin(theta), 0, 0, 0, 1));
            }
        }

        ArrayList<Ray> im2Rays = new ArrayList<Ray>();
        for (double theta = 0; theta < Math.PI * 2; theta += 0.2){
            for(int r = 0; r < 30; r += 5){
                im2Rays.add(new Ray(-50, r * Math.sin(theta), r * Math.cos(theta), 1, 0, 0));
            }
        }






        scene.setOnMouseMoved(event -> {
            double mouseX = event.getSceneX();
            double mouseY = event.getSceneY();
            ArrayList<Ray> im3Rays = new ArrayList<Ray>();
            for (double theta = 0; theta < Math.PI * 2; theta += 0.2){
                for(int r = 0; r < 30; r += 5){
                    im3Rays.add(new Ray(
                            r * Math.cos(theta) * (Math.cos(mouseX / 100.0) + Math.sin(mouseX/ 100.0)) +  Math.sin(mouseX/ 100.0 - Math.PI/4),
                            r * Math.sin(theta),
                            r * Math.cos(theta) * (Math.cos(mouseX / 100.0) - Math.sin(mouseX / 100.0)) +  Math.cos(mouseX/ 100.0 - Math.PI/4),
                            Math.sin(mouseX / 100.0), 0, Math.cos(mouseX / 100.0)));
                }
            }

            VoxelGrid voxels = new VoxelGrid(RES, SIZE);
            voxels.castRays(im1Rays);
            voxels.castRays(im2Rays);
            voxels.castRays(im3Rays);

            voxels.addAllToGroup(group);
        });



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