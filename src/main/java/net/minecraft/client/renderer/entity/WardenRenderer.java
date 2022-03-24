package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.WardenEmissiveLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WardenRenderer extends MobRenderer<Warden, WardenModel<Warden>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/warden/warden.png");
    private static final ResourceLocation BIOLUMINESCENT_LAYER_TEXTURE = new ResourceLocation("textures/entity/warden/warden_bioluminescent_layer.png");
    private static final ResourceLocation EARS_TEXTURE = new ResourceLocation("textures/entity/warden/warden_ears.png");
    private static final ResourceLocation HEART_TEXTURE = new ResourceLocation("textures/entity/warden/warden_heart.png");
    private static final ResourceLocation PULSATING_SPOTS_TEXTURE_1 = new ResourceLocation("textures/entity/warden/warden_pulsating_spots_1.png");
    private static final ResourceLocation PULSATING_SPOTS_TEXTURE_2 = new ResourceLocation("textures/entity/warden/warden_pulsating_spots_2.png");

    public WardenRenderer(EntityRendererProvider.Context param0) {
        super(param0, new WardenModel<>(param0.bakeLayer(ModelLayers.WARDEN)), 0.5F);
        this.addLayer(new WardenEmissiveLayer<>(this, BIOLUMINESCENT_LAYER_TEXTURE, (param0x, param1, param2) -> 1.0F));
        this.addLayer(new WardenEmissiveLayer<>(this, PULSATING_SPOTS_TEXTURE_1, (param0x, param1, param2) -> Math.max(0.0F, Mth.cos(param2 * 0.045F) * 0.25F)));
        this.addLayer(
            new WardenEmissiveLayer<>(
                this, PULSATING_SPOTS_TEXTURE_2, (param0x, param1, param2) -> Math.max(0.0F, Mth.cos(param2 * 0.045F + (float) Math.PI) * 0.25F)
            )
        );
        this.addLayer(new WardenEmissiveLayer<>(this, EARS_TEXTURE, (param0x, param1, param2) -> param0x.getEarAnimation(param1)));
        this.addLayer(new WardenEmissiveLayer<>(this, HEART_TEXTURE, (param0x, param1, param2) -> param0x.getHeartAnimation(param1)));
    }

    public void render(Warden param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        if (param0.tickCount > 2 || param0.hasPose(Pose.EMERGING)) {
            super.render(param0, param1, param2, param3, param4, param5);
        }
    }

    public ResourceLocation getTextureLocation(Warden param0) {
        return TEXTURE;
    }
}