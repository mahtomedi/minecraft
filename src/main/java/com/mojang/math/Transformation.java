package com.mojang.math;

import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Triple;

@OnlyIn(Dist.CLIENT)
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
            Matrix4f var1 = new Matrix4f();
            var1.setIdentity();
            var1.set(0, 0, param2.x());
            var1.set(1, 1, param2.y());
            var1.set(2, 2, param2.z());
            var0.multiply(var1);
        }

        if (param3 != null) {
            var0.multiply(new Matrix4f(param3));
        }

        if (param0 != null) {
            var0.set(0, 3, param0.x());
            var0.set(1, 3, param0.y());
            var0.set(2, 3, param0.z());
        }

        return var0;
    }

    public static Pair<Matrix3f, Vector3f> toAffine(Matrix4f param0) {
        param0.multiply(1.0F / param0.get(3, 3));
        Vector3f var0 = new Vector3f(param0.get(0, 3), param0.get(1, 3), param0.get(2, 3));
        Matrix3f var1 = new Matrix3f(param0);
        return Pair.of(var1, var0);
    }

    public Matrix4f getMatrix() {
        return this.matrix.copy();
    }

    public Quaternion getLeftRotation() {
        this.ensureDecomposed();
        return this.leftRotation.copy();
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
}