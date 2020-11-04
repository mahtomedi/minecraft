package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SlimeRenderer extends MobRenderer<Slime, SlimeModel<Slime>> {
    private static final ResourceLocation SLIME_LOCATION = new ResourceLocation("textures/entity/slime/slime.png");

    public SlimeRenderer(EntityRendererProvider.Context param0) {
        super(param0, new SlimeModel<>(param0.getLayer(ModelLayers.SLIME)), 0.25F);
        this.addLayer(new SlimeOuterLayer<>(this, param0.getModelSet()));
    }

    public void render(Slime param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        this.shadowRadius = 0.25F * (float)param0.getSize();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected void scale(Slime param0, PoseStack param1, float param2) {
        float var0 = 0.999F;
        param1.scale(0.999F, 0.999F, 0.999F);
        param1.translate(0.0, 0.001F, 0.0);
        float var1 = (float)param0.getSize();
        float var2 = Mth.lerp(param2, param0.oSquish, param0.squish) / (var1 * 0.5F + 1.0F);
        float var3 = 1.0F / (var2 + 1.0F);
        param1.scale(var3 * var1, 1.0F / var3 * var1, var3 * var1);
    }

    public ResourceLocation getTextureLocation(Slime param0) {
        return SLIME_LOCATION;
    }
}
