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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        group.translateZProperty().set(0);

        PerspectiveCamera camera = new PerspectiveCamera();

        Scene scene = new Scene(group, SceneConstants.WIDTH, SceneConstants.HEIGHT); // true);
        scene.setFill(Color.BLUE);
        scene.setCamera(camera);

        ArrayList<ImagePlane> images = new ArrayList<ImagePlane>();
        File f = new File(SceneConstants.FOLDER);
        String[] fileNames = f.list();

        if (fileNames == null){
            throw new NullPointerException("No files were found!");
        }

        // create image planes for all the images and position them based on filenames
        for (String fileName : fileNames){
            // file names should be formatted YOURFILENAME_THETA_PHI.XYZ and use commas instead of dots for decimals
            // ex: teapot_45_22,5.png
            Pattern regex = Pattern.compile(".*_(-?\\d+,?\\d*)_(-?\\d+,?\\d*).*");
            Matcher matcher = regex.matcher(fileName);

            if (!matcher.find()){ // file is formatted incorrectly, skip it
                continue;
            }

            double theta = Double.parseDouble(matcher.group(1).replace(',', '.'));
            double phi = Double.parseDouble(matcher.group(2).replace(',', '.'));
            ImagePlane imagePlane = new ImagePlane(fileName, theta, phi);
            images.add(imagePlane);
        }


        VoxelGrid voxels = new VoxelGrid();

        calculateVoxels(group, images, voxels);
        handleMouseDrag(group, scene);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Voxel Carving");

        primaryStage.show();
    }

    // uses the images to fill in the VoxelGrid based on the correlation between images
    private void calculateVoxels(Group group, ArrayList<ImagePlane> images, VoxelGrid voxels) {
        Group voxelGroup = new Group();

        voxels.correlateVoxels(images);
        voxels.addAllToGroup(voxelGroup);

        group.getChildren().clear();

        if (SceneConstants.SHOW_IMAGES) { // optionally show the images as well
            for (ImagePlane image : images) {
                group.getChildren().add(image.plane);
            }
        }

        group.getChildren().add(voxelGroup);
    }

    // allows the user to rotate the view by dragging the mouse
    void handleMouseDrag(Group group, Scene scene){
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