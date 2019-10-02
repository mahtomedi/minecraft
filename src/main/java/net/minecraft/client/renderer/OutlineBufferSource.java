package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OutlineBufferSource implements MultiBufferSource {
    private final MultiBufferSource.BufferSource bufferSource;
    private final MultiBufferSource.BufferSource outlineBufferSource = MultiBufferSource.immediate(new BufferBuilder(256));
    private int teamR = 255;
    private int teamG = 255;
    private int teamB = 255;
    private int teamA = 255;

    public OutlineBufferSource(MultiBufferSource.BufferSource param0) {
        this.bufferSource = param0;
    }

    @Override
    public VertexConsumer getBuffer(RenderType param0) {
        VertexConsumer var0 = this.bufferSource.getBuffer(param0);
        Optional<ResourceLocation> var1 = param0.outlineTexture();
        if (var1.isPresent()) {
            VertexConsumer var2 = this.outlineBufferSource.getBuffer(RenderType.OUTLINE(var1.get()));
            OutlineBufferSource.EntityOutlineGenerator var3 = new OutlineBufferSource.EntityOutlineGenerator(
                var2, this.teamR, this.teamG, this.teamB, this.teamA
            );
            return new VertexMultiConsumer(ImmutableList.of(var3, var0));
        } else {
            return var0;
        }
    }

    public void setColor(int param0, int param1, int param2, int param3) {
        this.teamR = param0;
        this.teamG = param1;
        this.teamB = param2;
        this.teamA = param3;
    }

    public void endOutlineBatch() {
        this.outlineBufferSource.endBatch();
    }

    @OnlyIn(Dist.CLIENT)
    static class EntityOutlineGenerator extends DefaultedVertexConsumer {
        private final VertexConsumer delegate;
        private double x;
        private double y;
        private double z;
        private float u;
        private float v;

        private EntityOutlineGenerator(VertexConsumer param0, int param1, int param2, int param3, int param4) {
            this.delegate = param0;
            super.defaultColor(param1, param2, param3, param4);
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
            this.x = param0;
            this.y = param1;
            this.z = param2;
            return this;
        }

        @Override
        public VertexConsumer color(int param0, int param1, int param2, int param3) {
            return this;
        }

        @Override
        public VertexConsumer uv(float param0, float param1) {
            this.u = param0;
            this.v = param1;
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
            this.delegate.vertex(this.x, this.y, this.z).color(this.defaultR, this.defaultG, this.defaultB, this.defaultA).uv(this.u, this.v).endVertex();
        }
    }
}
