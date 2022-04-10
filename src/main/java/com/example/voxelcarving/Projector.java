package com.example.voxelcarving;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.transform.MatrixType;
import org.opencv.core.*;
import org.opencv.imgproc.*;
import org.opencv.utils.*;
import java.awt.image.BufferedImage;
import org.apache.commons.math3.linear.*;

public class Projector { // Camera class name was taken
    RealMatrix intrinsic;
    RealMatrix extrinsic;

    public Projector(RealVector relativePosition, RealMatrix rotation, double u, double v, double f){
        intrinsic = MatrixUtils.createRealMatrix(new double[][]{{f, 0.0, u},
                        {0.0, f, v},
                        {0.0, 0.0, 1.0}});

        rotation = rotation.transpose();
        RealVector globalPosition = rotation.operate(relativePosition).mapMultiply(-1);

        extrinsic = new Array2DRowRealMatrix(3, 4);
        extrinsic.setSubMatrix(rotation.getData(), 0, 0);
        extrinsic.setColumnVector(3, globalPosition);
    }

    // calculates the 2D projection of the point onto the image plane of the
    public RealVector projectPoint(RealVector pt) {
        pt = pt.append(1);
        RealMatrix projectionMatrix = this.getProjectionMatrix();
        RealVector projectedPoint = projectionMatrix.operate(pt);
        double kx = projectedPoint.getEntry(0);
        double ky = projectedPoint.getEntry(1);
        double k = projectedPoint.getEntry(2);
        return new ArrayRealVector(new double[]{kx / k, ky / k});
    }

    // returns the camera projection matrix
    public RealMatrix getProjectionMatrix() {
        return intrinsic.multiply(extrinsic);
    }

    // returns a rotation matrix based on euler angles
    public static RealMatrix rotationMatrix(double alpha, double beta, double gamma) {
        double sinx = Math.sin(alpha);
        double siny = Math.sin(beta);
        double sinz = Math.sin(gamma);
        double cosx = Math.cos(alpha);
        double cosy = Math.cos(beta);
        double cosz = Math.cos(gamma);

        RealMatrix rotX = MatrixUtils.createRealMatrix(new double[][]{{1.0, 0.0, 0.0},
                        {0.0, cosx, -sinx},
                        {0.0, sinx, cosx}});
        RealMatrix rotY = MatrixUtils.createRealMatrix(new double[][]{{cosy, 0.0, siny},
                        {0.0, 1.0, 0.0},
                        {-siny, 0.0, cosy}});
        RealMatrix rotZ = MatrixUtils.createRealMatrix(new double[][]{{cosz, -sinz, 0.0},
                        {sinz, cosz, 0.0},
                        {0.0,  0.0, 1.0}});

        return rotX.multiply(rotY).multiply(rotZ);
    }
}