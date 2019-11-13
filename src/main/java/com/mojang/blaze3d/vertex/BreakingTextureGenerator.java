package com.mojang.blaze3d.vertex;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreakingTextureGenerator extends DefaultedVertexConsumer {
    private final VertexConsumer delegate;
    private final Matrix4f cameraInversePose;
    private final Matrix3f normalPose;
    private float x;
    private float y;
    private float z;
    private int overlayU;
    private int overlayV;
    private int lightCoords;
    private float nx;
    private float ny;
    private float nz;

    public BreakingTextureGenerator(VertexConsumer param0, PoseStack.Pose param1) {
        this.delegate = param0;
        this.cameraInversePose = param1.pose().copy();
        this.cameraInversePose.invert();
        this.normalPose = param1.normal().copy();
        this.normalPose.invert();
        this.resetState();
    }

    private void resetState() {
        this.x = 0.0F;
        this.y = 0.0F;
        this.z = 0.0F;
        this.overlayU = 0;
        this.overlayV = 10;
        this.lightCoords = 15728880;
        this.nx = 0.0F;
        this.ny = 1.0F;
        this.nz = 0.0F;
    }

    @Override
    public void endVertex() {
        Vector3f var0 = new Vector3f(this.nx, this.ny, this.nz);
        var0.transform(this.normalPose);
        Direction var1 = Direction.getNearest(var0.x(), var0.y(), var0.z());
        Vector4f var2 = new Vector4f(this.x, this.y, this.z, 1.0F);
        var2.transform(this.cameraInversePose);
        var2.transform(Vector3f.YP.rotationDegrees(180.0F));
        var2.transform(Vector3f.XP.rotationDegrees(-90.0F));
        var2.transform(var1.getRotation());
        float var3 = -var2.x();
        float var4 = -var2.y();
        this.delegate
            .vertex((double)this.x, (double)this.y, (double)this.z)
            .color(1.0F, 1.0F, 1.0F, 1.0F)
            .uv(var3, var4)
            .overlayCoords(this.overlayU, this.overlayV)
            .uv2(this.lightCoords)
            .normal(this.nx, this.ny, this.nz)
            .endVertex();
        this.resetState();
    }

    @Override
    public VertexConsumer vertex(double param0, double param1, double param2) {
        this.x = (float)param0;
        this.y = (float)param1;
        this.z = (float)param2;
        return this;
    }

    @Override
    public VertexConsumer color(int param0, int param1, int param2, int param3) {
        return this;
    }

    @Override
    public VertexConsumer uv(float param0, float param1) {
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int param0, int param1) {
        this.overlayU = param0;
        this.overlayV = param1;
        return this;
    }

    @Override
    public VertexConsumer uv2(int param0, int param1) {
        this.lightCoords = param0 | param1 << 16;
        return this;
    }

    @Override
    public VertexConsumer normal(float param0, float param1, float param2) {
        this.nx = param0;
        this.ny = param1;
        this.nz = param2;
        return this;
    }
}
