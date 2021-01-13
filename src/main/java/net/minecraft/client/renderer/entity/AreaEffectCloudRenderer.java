package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AreaEffectCloudRenderer extends EntityRenderer<AreaEffectCloud> {
    public AreaEffectCloudRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public ResourceLocation getTextureLocation(AreaEffectCloud param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
