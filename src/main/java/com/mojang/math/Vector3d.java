package com.mojang.math;

public class Vector3d {
    public double x;
    public double y;
    public double z;

    public Vector3d(double param0, double param1, double param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
    }

    public void set(Vector3d param0) {
        this.x = param0.x;
        this.y = param0.y;
        this.z = param0.z;
    }

    public void set(double param0, double param1, double param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
    }

    public void scale(double param0) {
        this.x *= param0;
        this.y *= param0;
        this.z *= param0;
    }

    public void add(Vector3d param0) {
        this.x += param0.x;
        this.y += param0.y;
        this.z += param0.z;
    }
}
