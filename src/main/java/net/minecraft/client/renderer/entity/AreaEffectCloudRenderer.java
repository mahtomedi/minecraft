package net.minecraft.client.renderer.entity;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AreaEffectCloudRenderer extends EntityRenderer<AreaEffectCloud> {
    public AreaEffectCloudRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    @Nullable
    protected ResourceLocation getTextureLocation(AreaEffectCloud param0) {
        return null;
    }
}
