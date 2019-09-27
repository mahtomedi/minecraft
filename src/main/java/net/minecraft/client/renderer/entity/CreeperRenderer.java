package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.renderer.entity.layers.CreeperPowerLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreeperRenderer extends MobRenderer<Creeper, CreeperModel<Creeper>> {
    private static final ResourceLocation CREEPER_LOCATION = new ResourceLocation("textures/entity/creeper/creeper.png");

    public CreeperRenderer(EntityRenderDispatcher param0) {
        super(param0, new CreeperModel<>(), 0.5F);
        this.addLayer(new CreeperPowerLayer(this));
    }

    protected void scale(Creeper param0, PoseStack param1, float param2) {
        float var0 = param0.getSwelling(param2);
        float var1 = 1.0F + Mth.sin(var0 * 100.0F) * var0 * 0.01F;
        var0 = Mth.clamp(var0, 0.0F, 1.0F);
        var0 *= var0;
        var0 *= var0;
        float var2 = (1.0F + var0 * 0.4F) * var1;
        float var3 = (1.0F + var0 * 0.1F) / var1;
        param1.scale(var2, var3, var2);
    }

    protected float getWhiteOverlayProgress(Creeper param0, float param1) {
        float var0 = param0.getSwelling(param1);
        return (int)(var0 * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(var0, 0.0F, 1.0F);
    }

    public ResourceLocation getTextureLocation(Creeper param0) {
        return CREEPER_LOCATION;
    }
}
