package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LavaSlimeRenderer extends MobRenderer<MagmaCube, LavaSlimeModel<MagmaCube>> {
    private static final ResourceLocation MAGMACUBE_LOCATION = new ResourceLocation("textures/entity/slime/magmacube.png");

    public LavaSlimeRenderer(EntityRenderDispatcher param0) {
        super(param0, new LavaSlimeModel<>(), 0.25F);
    }

    protected ResourceLocation getTextureLocation(MagmaCube param0) {
        return MAGMACUBE_LOCATION;
    }

    protected void scale(MagmaCube param0, float param1) {
        int var0 = param0.getSize();
        float var1 = Mth.lerp(param1, param0.oSquish, param0.squish) / ((float)var0 * 0.5F + 1.0F);
        float var2 = 1.0F / (var1 + 1.0F);
        GlStateManager.scalef(var2 * (float)var0, 1.0F / var2 * (float)var0, var2 * (float)var0);
    }
}
