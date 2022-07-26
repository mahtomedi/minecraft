package com.mojang.math;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.Util;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4x3f;
import org.joml.Matrix4x3fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class Transformation {
    private final Matrix4f matrix;
    private boolean decomposed;
    @Nullable
    private Vector3f translation;
    @Nullable
    private Quaternionf leftRotation;
    @Nullable
    private Vector3f scale;
    @Nullable
    private Quaternionf rightRotation;
    private static final Transformation IDENTITY = Util.make(() -> {
        Transformation var0 = new Transformation(new Matrix4f());
        var0.getLeftRotation();
        return var0;
    });

    public Transformation(@Nullable Matrix4f param0) {
        if (param0 == null) {
            this.matrix = IDENTITY.matrix;
        } else {
            this.matrix = param0;
        }

    }

    public Transformation(@Nullable Vector3f param0, @Nullable Quaternionf param1, @Nullable Vector3f param2, @Nullable Quaternionf param3) {
        this.matrix = compose(param0, param1, param2, param3);
        this.translation = param0 != null ? param0 : new Vector3f();
        this.leftRotation = param1 != null ? param1 : new Quaternionf();
        this.scale = param2 != null ? param2 : new Vector3f(1.0F, 1.0F, 1.0F);
        this.rightRotation = param3 != null ? param3 : new Quaternionf();
        this.decomposed = true;
    }

    public static Transformation identity() {
        return IDENTITY;
    }

    public Transformation compose(Transformation param0) {
        Matrix4f var0 = this.getMatrix();
        var0.mul(param0.getMatrix());
        return new Transformation(var0);
    }

    @Nullable
    public Transformation inverse() {
        if (this == IDENTITY) {
            return this;
        } else {
            Matrix4f var0 = this.getMatrix().invert();
            return var0.isFinite() ? new Transformation(var0) : null;
        }
    }

    private void ensureDecomposed() {
        if (!this.decomposed) {
            Matrix4x3f var0 = MatrixUtil.toAffine(this.matrix);
            Triple<Quaternionf, Vector3f, Quaternionf> var1 = MatrixUtil.svdDecompose(new Matrix3f().set((Matrix4x3fc)var0));
            this.translation = var0.getTranslation(new Vector3f());
            this.leftRotation = new Quaternionf(var1.getLeft());
            this.scale = new Vector3f(var1.getMiddle());
            this.rightRotation = new Quaternionf(var1.getRight());
            this.decomposed = true;
        }

    }

    private static Matrix4f compose(@Nullable Vector3f param0, @Nullable Quaternionf param1, @Nullable Vector3f param2, @Nullable Quaternionf param3) {
        Matrix4f var0 = new Matrix4f();
        if (param0 != null) {
            var0.translation(param0);
        }

        if (param1 != null) {
            var0.rotate(param1);
        }

        if (param2 != null) {
            var0.scale(param2);
        }

        if (param3 != null) {
            var0.rotate(param3);
        }

        return var0;
    }

    public Matrix4f getMatrix() {
        return new Matrix4f(this.matrix);
    }

    public Vector3f getTranslation() {
        this.ensureDecomposed();
        return new Vector3f((Vector3fc)this.translation);
    }

    public Quaternionf getLeftRotation() {
        this.ensureDecomposed();
        return new Quaternionf(this.leftRotation);
    }

    public Vector3f getScale() {
        this.ensureDecomposed();
        return new Vector3f((Vector3fc)this.scale);
    }

    public Quaternionf getRightRotation() {
        this.ensureDecomposed();
        return new Quaternionf(this.rightRotation);
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
        Quaternionf var1 = this.getLeftRotation();
        Vector3f var2 = this.getScale();
        Quaternionf var3 = this.getRightRotation();
        var0.lerp(param0.getTranslation(), param1);
        var1.slerp(param0.getLeftRotation(), param1);
        var2.lerp(param0.getScale(), param1);
        var3.slerp(param0.getRightRotation(), param1);
        return new Transformation(var0, var1, var2, var3);
    }
}
