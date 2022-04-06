package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.AllayModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AllayRenderer extends MobRenderer<Allay, AllayModel> {
    private static final ResourceLocation ALLAY_TEXTURE = new ResourceLocation("textures/entity/allay/allay.png");
    private static final int BRIGHTNESS_LEVEL_TRANSITION_DURATION = 60;
    private static final int MIN_BRIGHTNESS_LEVEL = 5;

    public AllayRenderer(EntityRendererProvider.Context param0) {
        super(param0, new AllayModel(param0.bakeLayer(ModelLayers.ALLAY)), 0.4F);
        this.addLayer(new ItemInHandLayer<>(this));
    }

    public ResourceLocation getTextureLocation(Allay param0) {
        return ALLAY_TEXTURE;
    }

    protected int getBlockLightLevel(Allay param0, BlockPos param1) {
        long var0 = param0.getLevel().getGameTime() + (long)Math.abs(param0.getUUID().hashCode());
        float var1 = (float)Math.abs(var0 % 120L - 60L);
        float var2 = var1 / 60.0F;
        return (int)Mth.lerp(var2, 5.0F, 15.0F);
    }
}
