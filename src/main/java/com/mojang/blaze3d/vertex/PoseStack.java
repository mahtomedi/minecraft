package com.mojang.blaze3d.vertex;

import com.google.common.collect.Queues;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Deque;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PoseStack {
    private final Deque<PoseStack.Pose> poseStack = Util.make(Queues.newArrayDeque(), param0 -> {
        Matrix4f var0 = new Matrix4f();
        var0.setIdentity();
        Matrix3f var1 = new Matrix3f();
        var1.setIdentity();
        param0.add(new PoseStack.Pose(var0, var1));
    });

    public void translate(double param0, double param1, double param2) {
        Matrix4f var0 = new Matrix4f();
        var0.setIdentity();
        var0.translate(new Vector3f((float)param0, (float)param1, (float)param2));
        PoseStack.Pose var1 = this.poseStack.getLast();
        var1.pose.multiply(var0);
    }

    public void scale(float param0, float param1, float param2) {
        PoseStack.Pose var0 = this.poseStack.getLast();
        Matrix4f var1 = new Matrix4f();
        var1.setIdentity();
        var1.set(0, 0, param0);
        var1.set(1, 1, param1);
        var1.set(2, 2, param2);
        var0.pose.multiply(var1);
        if (param0 != param1 || param1 != param2) {
            float var2 = 1.0F / param0;
            float var3 = 1.0F / param1;
            float var4 = 1.0F / param2;
            float var5 = Mth.fastInvCubeRoot(var2 * var3 * var4);
            Matrix3f var6 = new Matrix3f();
            var6.set(0, 0, var5 * var2);
            var6.set(1, 1, var5 * var3);
            var6.set(2, 2, var5 * var4);
            var0.normal.mul(var6);
        }
    }

    public void mulPose(Quaternion param0) {
        PoseStack.Pose var0 = this.poseStack.getLast();
        var0.pose.multiply(param0);
        var0.normal.mul(param0);
    }

    public void pushPose() {
        PoseStack.Pose var0 = this.poseStack.getLast();
        this.poseStack.addLast(new PoseStack.Pose(var0.pose.copy(), var0.normal.copy()));
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

    @OnlyIn(Dist.CLIENT)
    public static final class Pose {
        private final Matrix4f pose;
        private final Matrix3f normal;

        private Pose(Matrix4f param0, Matrix3f param1) {
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