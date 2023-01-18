package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MagmaCubeRenderer extends MobRenderer<MagmaCube, LavaSlimeModel<MagmaCube>> {
    private static final ResourceLocation MAGMACUBE_LOCATION = new ResourceLocation("textures/entity/slime/magmacube.png");

    public MagmaCubeRenderer(EntityRendererProvider.Context param0) {
        super(param0, new LavaSlimeModel<>(param0.bakeLayer(ModelLayers.MAGMA_CUBE)), 0.25F);
    }

    protected int getBlockLightLevel(MagmaCube param0, BlockPos param1) {
        return 15;
    }

    public ResourceLocation getTextureLocation(MagmaCube param0) {
        return MAGMACUBE_LOCATION;
    }

    public void render(MagmaCube param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        this.shadowRadius = 0.25F * (float)param0.getSize();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected void scale(MagmaCube param0, PoseStack param1, float param2) {
        int var0 = param0.getSize();
        float var1 = Mth.lerp(param2, param0.oSquish, param0.squish) / ((float)var0 * 0.5F + 1.0F);
        float var2 = 1.0F / (var1 + 1.0F);
        param1.scale(var2 * (float)var0, 1.0F / var2 * (float)var0, var2 * (float)var0);
    }
}
