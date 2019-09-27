package com.mojang.blaze3d.vertex;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityOutlineGenerator extends DefaultedVertexConsumer {
    private final VertexConsumer delegate;

    public EntityOutlineGenerator(VertexConsumer param0) {
        this.delegate = param0;
    }

    public void setColor(int param0, int param1, int param2, int param3) {
        super.defaultColor(param0, param1, param2, param3);
    }

    @Override
    public void defaultColor(int param0, int param1, int param2, int param3) {
    }

    @Override
    public void defaultOverlayCoords(int param0, int param1) {
    }

    @Override
    public void unsetDefaultOverlayCoords() {
    }

    @Override
    public VertexConsumer vertex(double param0, double param1, double param2) {
        this.delegate.vertex(param0, param1, param2).color(this.defaultR, this.defaultG, this.defaultB, this.defaultA).endVertex();
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
        return this;
    }

    @Override
    public VertexConsumer uv2(int param0, int param1) {
        return this;
    }

    @Override
    public VertexConsumer normal(float param0, float param1, float param2) {
        return this;
    }

    @Override
    public void endVertex() {
    }
}
