package com.mojang.math;

import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.Util;
import org.apache.commons.lang3.tuple.Triple;

public final class Transformation {
    private final Matrix4f matrix;
    private boolean decomposed;
    @Nullable
    private Vector3f translation;
    @Nullable
    private Quaternion leftRotation;
    @Nullable
    private Vector3f scale;
    @Nullable
    private Quaternion rightRotation;
    private static final Transformation IDENTITY = Util.make(() -> {
        Matrix4f var0 = new Matrix4f();
        var0.setIdentity();
        Transformation var1 = new Transformation(var0);
        var1.getLeftRotation();
        return var1;
    });

    public Transformation(@Nullable Matrix4f param0) {
        if (param0 == null) {
            this.matrix = IDENTITY.matrix;
        } else {
            this.matrix = param0;
        }

    }

    public Transformation(@Nullable Vector3f param0, @Nullable Quaternion param1, @Nullable Vector3f param2, @Nullable Quaternion param3) {
        this.matrix = compose(param0, param1, param2, param3);
        this.translation = param0 != null ? param0 : new Vector3f();
        this.leftRotation = param1 != null ? param1 : Quaternion.ONE.copy();
        this.scale = param2 != null ? param2 : new Vector3f(1.0F, 1.0F, 1.0F);
        this.rightRotation = param3 != null ? param3 : Quaternion.ONE.copy();
        this.decomposed = true;
    }

    public static Transformation identity() {
        return IDENTITY;
    }

    public Transformation compose(Transformation param0) {
        Matrix4f var0 = this.getMatrix();
        var0.multiply(param0.getMatrix());
        return new Transformation(var0);
    }

    @Nullable
    public Transformation inverse() {
        if (this == IDENTITY) {
            return this;
        } else {
            Matrix4f var0 = this.getMatrix();
            return var0.invert() ? new Transformation(var0) : null;
        }
    }

    private void ensureDecomposed() {
        if (!this.decomposed) {
            Pair<Matrix3f, Vector3f> var0 = toAffine(this.matrix);
            Triple<Quaternion, Vector3f, Quaternion> var1 = var0.getFirst().svdDecompose();
            this.translation = var0.getSecond();
            this.leftRotation = var1.getLeft();
            this.scale = var1.getMiddle();
            this.rightRotation = var1.getRight();
            this.decomposed = true;
        }

    }

    private static Matrix4f compose(@Nullable Vector3f param0, @Nullable Quaternion param1, @Nullable Vector3f param2, @Nullable Quaternion param3) {
        Matrix4f var0 = new Matrix4f();
        var0.setIdentity();
        if (param1 != null) {
            var0.multiply(new Matrix4f(param1));
        }

        if (param2 != null) {
            var0.multiply(Matrix4f.createScaleMatrix(param2.x(), param2.y(), param2.z()));
        }

        if (param3 != null) {
            var0.multiply(new Matrix4f(param3));
        }

        if (param0 != null) {
            var0.m03 = param0.x();
            var0.m13 = param0.y();
            var0.m23 = param0.z();
        }

        return var0;
    }

    public static Pair<Matrix3f, Vector3f> toAffine(Matrix4f param0) {
        param0.multiply(1.0F / param0.m33);
        Vector3f var0 = new Vector3f(param0.m03, param0.m13, param0.m23);
        Matrix3f var1 = new Matrix3f(param0);
        return Pair.of(var1, var0);
    }

    public Matrix4f getMatrix() {
        return this.matrix.copy();
    }

    public Vector3f getTranslation() {
        this.ensureDecomposed();
        return this.translation.copy();
    }

    public Quaternion getLeftRotation() {
        this.ensureDecomposed();
        return this.leftRotation.copy();
    }

    public Vector3f getScale() {
        this.ensureDecomposed();
        return this.scale.copy();
    }

    public Quaternion getRightRotation() {
        this.ensureDecomposed();
        return this.rightRotation.copy();
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            Transformation var0 = (Transformation)param0;
            return Objects.equals(this.matrix, var0.matrix);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.matrix);
    }

    public Transformation slerp(Transformation param0, float param1) {
        Vector3f var0 = this.getTranslation();
        Quaternion var1 = this.getLeftRotation();
        Vector3f var2 = this.getScale();
        Quaternion var3 = this.getRightRotation();
        var0.lerp(param0.getTranslation(), param1);
        var1.slerp(param0.getLeftRotation(), param1);
        var2.lerp(param0.getScale(), param1);
        var3.slerp(param0.getRightRotation(), param1);
        return new Transformation(var0, var1, var2, var3);
    }
}
