package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VertexMultiConsumer implements VertexConsumer {
    private final Iterable<VertexConsumer> delegates;

    public VertexMultiConsumer(ImmutableList<VertexConsumer> param0) {
        for(int var0 = 0; var0 < param0.size(); ++var0) {
            for(int var1 = var0 + 1; var1 < param0.size(); ++var1) {
                if (param0.get(var0) == param0.get(var1)) {
                    throw new IllegalArgumentException("Duplicate delegates");
                }
            }
        }

        this.delegates = param0;
    }

    @Override
    public VertexConsumer vertex(double param0, double param1, double param2) {
        this.delegates.forEach(param3 -> param3.vertex(param0, param1, param2));
        return this;
    }

    @Override
    public VertexConsumer color(int param0, int param1, int param2, int param3) {
        this.delegates.forEach(param4 -> param4.color(param0, param1, param2, param3));
        return this;
    }

    @Override
    public VertexConsumer uv(float param0, float param1) {
        this.delegates.forEach(param2 -> param2.uv(param0, param1));
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int param0, int param1) {
        this.delegates.forEach(param2 -> param2.uv2(param0, param1));
        return this;
    }

    @Override
    public VertexConsumer uv2(int param0, int param1) {
        this.delegates.forEach(param2 -> param2.uv2(param0, param1));
        return this;
    }

    @Override
    public VertexConsumer normal(float param0, float param1, float param2) {
        this.delegates.forEach(param3 -> param3.normal(param0, param1, param2));
        return this;
    }

    @Override
    public void endVertex() {
        this.delegates.forEach(VertexConsumer::endVertex);
    }

    @Override
    public void defaultOverlayCoords(int param0, int param1) {
        this.delegates.forEach(param2 -> param2.defaultOverlayCoords(param0, param1));
    }

    @Override
    public void unsetDefaultOverlayCoords() {
        this.delegates.forEach(VertexConsumer::unsetDefaultOverlayCoords);
    }
}
