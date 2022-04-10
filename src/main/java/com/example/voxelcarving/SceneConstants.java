package com.example.voxelcarving;


// hyperparameters and customization
public final class SceneConstants {
    // change this parameter to change the folder of the dataset used
    // current options are: "plane", "teapot", "triceratops", "triceratops-alpha"
    public static final String DATASET = "plane";
    public static final String FOLDER = "src/main/resources/" + DATASET + "/";

    // will need to be adjusted depending on the dataset used
    public static final double THRESHOLD = 0.5;

    // for images with transparent background, prevents transparent voxels from being shown
    public static final double MIN_TRANSPARENCY = 0.5;
    public static final int RES = 65;
    public static final double SIZE = 100.0 / RES;

    // whether to display the images used to calculate the voxels in the scene.
    public static final boolean SHOW_IMAGES = false;

    // scene width and height
    public static final int WIDTH = 960;
    public static final int HEIGHT = 540;

    // the distance of the images from the center of the sphere they are oriented around
    public static final double IMAGE_DIST = 100;

    private SceneConstants(){

    }

}
