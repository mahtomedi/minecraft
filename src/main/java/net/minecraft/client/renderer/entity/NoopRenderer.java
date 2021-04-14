package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NoopRenderer<T extends Entity> extends EntityRenderer<T> {
    public NoopRenderer(EntityRendererProvider.Context param0) {
        super(param0);
    }

    @Override
    public ResourceLocation getTextureLocation(T param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
