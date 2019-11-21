package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpriteCoordinateExpander implements VertexConsumer {
    private final VertexConsumer delegate;
    private final TextureAtlasSprite sprite;

    public SpriteCoordinateExpander(VertexConsumer param0, TextureAtlasSprite param1) {
        this.delegate = param0;
        this.sprite = param1;
    }

    @Override
    public VertexConsumer vertex(double param0, double param1, double param2) {
        return this.delegate.vertex(param0, param1, param2);
    }

    @Override
    public VertexConsumer color(int param0, int param1, int param2, int param3) {
        return this.delegate.color(param0, param1, param2, param3);
    }

    @Override
    public VertexConsumer uv(float param0, float param1) {
        return this.delegate.uv(this.sprite.getU((double)(param0 * 16.0F)), this.sprite.getV((double)(param1 * 16.0F)));
    }

    @Override
    public VertexConsumer overlayCoords(int param0, int param1) {
        return this.delegate.overlayCoords(param0, param1);
    }

    @Override
    public VertexConsumer uv2(int param0, int param1) {
        return this.delegate.uv2(param0, param1);
    }

    @Override
    public VertexConsumer normal(float param0, float param1, float param2) {
        return this.delegate.normal(param0, param1, param2);
    }

    @Override
    public void endVertex() {
        this.delegate.endVertex();
    }

    @Override
    public void vertex(
        float param0,
        float param1,
        float param2,
        float param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        int param9,
        int param10,
        float param11,
        float param12,
        float param13
    ) {
        this.delegate
            .vertex(
                param0,
                param1,
                param2,
                param3,
                param4,
                param5,
                param6,
                this.sprite.getU((double)(param7 * 16.0F)),
                this.sprite.getV((double)(param8 * 16.0F)),
                param9,
                param10,
                param11,
                param12,
                param13
            );
    }
}
