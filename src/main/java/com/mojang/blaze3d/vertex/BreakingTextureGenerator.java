package com.mojang.blaze3d.vertex;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreakingTextureGenerator extends DefaultedVertexConsumer {
    private final VertexConsumer delegate;
    private final double camX;
    private final double camY;
    private final double camZ;
    private double x;
    private double y;
    private double z;
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

    public BreakingTextureGenerator(VertexConsumer param0, double param1, double param2, double param3) {
        this.delegate = param0;
        this.camX = param1;
        this.camY = param2;
        this.camZ = param3;
        this.resetState();
    }

    private void resetState() {
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
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
        Direction var0 = Direction.getNearest(this.nx, this.ny, this.nz);
        double var1 = this.x + this.camX;
        double var2 = this.y + this.camY;
        double var3 = this.z + this.camZ;
        double var4;
        double var5;
        switch(var0.getAxis()) {
            case X:
                var4 = var3;
                var5 = var2;
                break;
            case Y:
                var4 = var1;
                var5 = var3;
                break;
            case Z:
            default:
                var4 = var1;
                var5 = var2;
        }

        float var10 = (float)(Mth.frac(var4 / 256.0) * 256.0);
        float var11 = (float)(Mth.frac(var5 / 256.0) * 256.0);
        this.delegate
            .vertex(this.x, this.y, this.z)
            .color(this.r, this.g, this.b, this.a)
            .uv(var10, var11)
            .overlayCoords(this.overlayU, this.overlayV)
            .uv2(this.lightCoords)
            .normal(this.nx, this.ny, this.nz)
            .endVertex();
        this.resetState();
    }

    @Override
    public VertexConsumer vertex(double param0, double param1, double param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
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
