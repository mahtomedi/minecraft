package com.mojang.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;

@FunctionalInterface
public interface Axis {
    Axis XN = param0 -> new Quaternionf().rotationX(-param0);
    Axis XP = param0 -> new Quaternionf().rotationX(param0);
    Axis YN = param0 -> new Quaternionf().rotationY(-param0);
    Axis YP = param0 -> new Quaternionf().rotationY(param0);
    Axis ZN = param0 -> new Quaternionf().rotationZ(-param0);
    Axis ZP = param0 -> new Quaternionf().rotationZ(param0);

    static Axis of(Vector3f param0) {
        return param1 -> new Quaternionf().rotationAxis(param1, param0);
    }

    Quaternionf rotation(float var1);

    default Quaternionf rotationDegrees(float param0) {
        return this.rotation(param0 * (float) (Math.PI / 180.0));
    }
}
