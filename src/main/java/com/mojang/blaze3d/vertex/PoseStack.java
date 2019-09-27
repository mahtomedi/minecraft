package com.mojang.blaze3d.vertex;

import com.google.common.collect.Queues;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Deque;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PoseStack {
    private final Deque<Matrix4f> poseStack = Util.make(Queues.newArrayDeque(), param0 -> {
        Matrix4f var0 = new Matrix4f();
        var0.setIdentity();
        param0.add(var0);
    });

    public void translate(double param0, double param1, double param2) {
        Matrix4f var0 = new Matrix4f();
        var0.setIdentity();
        var0.translate(new Vector3f((float)param0, (float)param1, (float)param2));
        this.mulPose(var0);
    }

    public void scale(float param0, float param1, float param2) {
        Matrix4f var0 = new Matrix4f();
        var0.setIdentity();
        var0.set(0, 0, param0);
        var0.set(1, 1, param1);
        var0.set(2, 2, param2);
        this.mulPose(var0);
    }

    public void mulPose(Matrix4f param0) {
        Matrix4f var0 = this.poseStack.getLast();
        var0.multiply(param0);
    }

    public void mulPose(Quaternion param0) {
        Matrix4f var0 = this.poseStack.getLast();
        var0.multiply(param0);
    }

    public void pushPose() {
        this.poseStack.addLast(this.poseStack.getLast().copy());
    }

    public void popPose() {
        this.poseStack.removeLast();
    }

    public Matrix4f getPose() {
        return this.poseStack.getLast();
    }

    public boolean clear() {
        return this.poseStack.size() == 1;
    }
}
