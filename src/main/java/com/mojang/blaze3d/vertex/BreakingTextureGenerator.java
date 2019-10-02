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
    private int r;
    private int g;
    private int b;
    private int a;
    private int overlayU;
    private int overlayV;
    private int lightCoords;
    private float nx;
    private float ny;
    private float nz;

    public BreakingTextureGenerator(VertexConsumer param0, Matrix4f param1) {
        this.delegate = param0;
        this.cameraInversePose = param1.copy();
        this.cameraInversePose.invert();
        this.normalPose = new Matrix3f(param1);
        this.normalPose.transpose();
        this.resetState();
    }

    private void resetState() {
        this.x = 0.0F;
        this.y = 0.0F;
        this.z = 0.0F;
        this.r = this.defaultR;
        this.g = this.defaultG;
        this.b = this.defaultB;
        this.a = this.defaultA;
        this.overlayU = this.defaultOverlayU;
        this.overlayV = this.defaultOverlayV;
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
        float var3;
        float var4;
        switch(var1.getAxis()) {
            case X:
                var3 = var2.z();
                var4 = var2.y();
                break;
            case Y:
                var3 = var2.x();
                var4 = var2.z();
                break;
            case Z:
            default:
                var3 = var2.x();
                var4 = var2.y();
        }

        this.delegate
            .vertex((double)this.x, (double)this.y, (double)this.z)
            .color(this.r, this.g, this.b, this.a)
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
        if (this.defaultColorSet) {
            throw new IllegalStateException();
        } else {
            this.r = param0;
            this.g = param1;
            this.b = param2;
            this.a = param3;
            return this;
        }
    }

    @Override
    public VertexConsumer uv(float param0, float param1) {
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int param0, int param1) {
        if (this.defaultOverlayCoordsSet) {
            throw new IllegalStateException();
        } else {
            this.overlayU = param0;
            this.overlayV = param1;
            return this;
        }
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
