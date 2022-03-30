package com.example.voxelcarving;

import javafx.scene.shape.Box;

public class Ray {
    // TODO: use actual vectors later
//    PVector dir;
//    PVector off;
    double dirx, diry, dirz, offx, offy, offz;
//    public Ray(PVector off, PVector dir){
//        this.dir = dir;
//        this.off = off;
//    }

    public Ray(double x0, double y0, double z0, double x1, double y1, double z1){
//        this(new PVector(x0, y0, z0), new PVector(x1, y1, z1));

        this.offx = x0;
        this.offy = y0;
        this.offz = z0;
        this.dirx = x1;
        this.diry = y1;
        this.dirz = z1;

    }

    // https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-box-intersection
    boolean intersects(Box c){
        double cs = c.getHeight();
        double cx = c.getTranslateX() - cs / 2;
        double cy = c.getTranslateY() - cs / 2;
        double cz = c.getTranslateZ() - cs / 2;


        double tmin = (cx - offx) / dirx;
        double tmax = (cx + cs - offx) / dirx;

        if (tmin > tmax) {
            double temp = tmax;
            tmax = tmin;
            tmin = temp;
        }

        double tymin = (cy - offy) / diry;
        double tymax = (cy + cs - offy) / diry;

        if (tymin > tymax) {
            double temp = tymax;
            tymax = tymin;
            tymin = temp;
        }

        if ((tmin > tymax) || (tymin > tmax)) {
            return false;
        }

        if (tymin > tmin) {
            tmin = tymin;
        }

        if (tymax < tmax) {
            tmax = tymax;
        }

        double tzmin = (cz - offz) / dirz;
        double tzmax = (cz + cs - offz) / dirz;

        if (tzmin > tzmax) {
            double temp = tzmax;
            tzmax = tzmin;
            tzmin = temp;
        }

        if ((tmin > tzmax) || (tzmin > tmax))
            return false;

        if (tzmin > tmin) {
            tmin = tzmin;
        }

        if (tzmax < tmax) {
            tmax = tzmax;
        }

        return true;
    }
}
