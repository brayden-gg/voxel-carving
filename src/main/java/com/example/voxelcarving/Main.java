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


        Group group = new Group();
        group.translateXProperty().set(WIDTH / 2.0);
        group.translateYProperty().set(HEIGHT / 2.0);
        group.translateZProperty().set(-100);

        PerspectiveCamera camera = new PerspectiveCamera();

        Scene scene = new Scene(group, WIDTH, HEIGHT); // true);
        scene.setFill(Color.BLUE);
        scene.setCamera(camera);




        Box image = getImagePlane("kevin.PNG");
        image.setTranslateZ(100);
        group.getChildren().add(image);

        VoxelGrid voxels = new VoxelGrid(RES, SIZE);

        calculateVoxels(scene, group, image, voxels);

        scene.setOnMouseMoved(event -> {
            double mouseX = event.getSceneX() - WIDTH / 2;
            double mouseY = event.getSceneY() - WIDTH / 2;
            double theta = mouseX;
            image.setTranslateX(Math.cos(theta / 180.0 * Math.PI) * mouseY);
            image.setTranslateZ(Math.sin(-theta / 180.0 * Math.PI) * mouseY);
            image.setRotationAxis(new Point3D(0, 1, 0));
            image.setRotate(theta - 90);
            calculateVoxels(scene, group, image, voxels);
        });



        handleMouseDrag(group, scene, primaryStage);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Voxel Carving");

        primaryStage.show();
    }

    private Box getImagePlane(String fileName) {
        try {
            FileInputStream inputStream = new FileInputStream("src/main/resources/" + fileName);
            Image image = new Image(inputStream);
            double aspectRatio = image.getWidth() / (double) image.getHeight();
            Box imagePlane = new Box(SIZE * RES * aspectRatio, SIZE * RES, 1);
            PhongMaterial imageMaterial = new PhongMaterial();
            imageMaterial.setDiffuseMap(image);
            imagePlane.setMaterial(imageMaterial);
            return imagePlane;
        } catch (Exception e) {
            System.out.println("file: \n" + fileName + "\n not found!");
            return new Box(10, 10, 10);
        }

    }

    private void calculateVoxels(Scene scene, Group group, Box imagePlane, VoxelGrid voxels) {
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
        voxels.correlateVoxels(imagePlane);
        voxels.addAllToGroup(voxelGroup);



        group.getChildren().clear();
        group.getChildren().add(imagePlane);
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