package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherArmorLayer extends EnergySwirlLayer<WitherBoss, WitherBossModel<WitherBoss>> {
    private static final ResourceLocation WITHER_ARMOR_LOCATION = new ResourceLocation("textures/entity/wither/wither_armor.png");
    private final WitherBossModel<WitherBoss> model;

    public WitherArmorLayer(RenderLayerParent<WitherBoss, WitherBossModel<WitherBoss>> param0, EntityModelSet param1) {
        super(param0);
        this.model = new WitherBossModel<>(param1.getLayer(ModelLayers.WITHER_ARMOR));
    }

    @Override
    protected float xOffset(float param0) {
        return Mth.cos(param0 * 0.02F) * 3.0F;
    }

    @Override
    protected ResourceLocation getTextureLocation() {
        return WITHER_ARMOR_LOCATION;
    }

    @Override
    protected EntityModel<WitherBoss> model() {
        return this.model;
    }
}
