package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MagmaCubeRenderer extends MobRenderer<MagmaCube, LavaSlimeModel<MagmaCube>> {
    private static final ResourceLocation MAGMACUBE_LOCATION = new ResourceLocation("textures/entity/slime/magmacube.png");

    public MagmaCubeRenderer(EntityRenderDispatcher param0) {
        super(param0, new LavaSlimeModel<>(), 0.25F);
    }

    protected int getBlockLightLevel(MagmaCube param0, float param1) {
        return 15;
    }

    public ResourceLocation getTextureLocation(MagmaCube param0) {
        return MAGMACUBE_LOCATION;
    }

    protected void scale(MagmaCube param0, PoseStack param1, float param2) {
        int var0 = param0.getSize();
        float var1 = Mth.lerp(param2, param0.oSquish, param0.squish) / ((float)var0 * 0.5F + 1.0F);
        float var2 = 1.0F / (var1 + 1.0F);
        param1.scale(var2 * (float)var0, 1.0F / var2 * (float)var0, var2 * (float)var0);
    }
}
