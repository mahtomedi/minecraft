package com.mojang.blaze3d.vertex;

import com.google.common.collect.Queues;
import java.util.Deque;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class PoseStack {
    private final Deque<PoseStack.Pose> poseStack = Util.make(Queues.newArrayDeque(), param0 -> {
        Matrix4f var0 = new Matrix4f();
        Matrix3f var1 = new Matrix3f();
        param0.add(new PoseStack.Pose(var0, var1));
    });

    public void translate(double param0, double param1, double param2) {
        this.translate((float)param0, (float)param1, (float)param2);
    }

    public void translate(float param0, float param1, float param2) {
        PoseStack.Pose var0 = this.poseStack.getLast();
        var0.pose.translate(param0, param1, param2);
    }

    public void scale(float param0, float param1, float param2) {
        PoseStack.Pose var0 = this.poseStack.getLast();
        var0.pose.scale(param0, param1, param2);
        if (param0 == param1 && param1 == param2) {
            if (param0 > 0.0F) {
                return;
            }

            var0.normal.scale(-1.0F);
        }

        float var1 = 1.0F / param0;
        float var2 = 1.0F / param1;
        float var3 = 1.0F / param2;
        float var4 = Mth.fastInvCubeRoot(var1 * var2 * var3);
        var0.normal.scale(var4 * var1, var4 * var2, var4 * var3);
    }

    public void mulPose(Quaternionf param0) {
        PoseStack.Pose var0 = this.poseStack.getLast();
        var0.pose.rotate(param0);
        var0.normal.rotate(param0);
    }

    public void rotateAround(Quaternionf param0, float param1, float param2, float param3) {
        PoseStack.Pose var0 = this.poseStack.getLast();
        var0.pose.rotateAround(param0, param1, param2, param3);
        var0.normal.rotate(param0);
    }

    public void pushPose() {
        PoseStack.Pose var0 = this.poseStack.getLast();
        this.poseStack.addLast(new PoseStack.Pose(new Matrix4f(var0.pose), new Matrix3f(var0.normal)));
    }

    public void popPose() {
        this.poseStack.removeLast();
    }

    public PoseStack.Pose last() {
        return this.poseStack.getLast();
    }

    public boolean clear() {
        return this.poseStack.size() == 1;
    }

    public void setIdentity() {
        PoseStack.Pose var0 = this.poseStack.getLast();
        var0.pose.identity();
        var0.normal.identity();
    }

    public void mulPoseMatrix(Matrix4f param0) {
        this.poseStack.getLast().pose.mul(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Pose {
        final Matrix4f pose;
        final Matrix3f normal;

        Pose(Matrix4f param0, Matrix3f param1) {
            this.pose = param0;
            this.normal = param1;
        }

        public Matrix4f pose() {
            return this.pose;
        }

        public Matrix3f normal() {
            return this.normal;
        }
    }
}
