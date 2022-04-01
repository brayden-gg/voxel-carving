module com.example.voxelcarving {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires opencv;
    requires commons.math3;

    opens com.example.voxelcarving to javafx.fxml;
    exports com.example.voxelcarving;
}