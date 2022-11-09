package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PiglinRenderer extends HumanoidMobRenderer<Mob, PiglinModel<Mob>> {
    private static final Map<EntityType<?>, ResourceLocation> TEXTURES = ImmutableMap.of(
        EntityType.PIGLIN,
        new ResourceLocation("textures/entity/piglin/piglin.png"),
        EntityType.ZOMBIFIED_PIGLIN,
        new ResourceLocation("textures/entity/piglin/zombified_piglin.png"),
        EntityType.PIGLIN_BRUTE,
        new ResourceLocation("textures/entity/piglin/piglin_brute.png")
    );
    private static final float PIGLIN_CUSTOM_HEAD_SCALE = 1.0019531F;

    public PiglinRenderer(
        EntityRendererProvider.Context param0, ModelLayerLocation param1, ModelLayerLocation param2, ModelLayerLocation param3, boolean param4
    ) {
        super(param0, createModel(param0.getModelSet(), param1, param4), 0.5F, 1.0019531F, 1.0F, 1.0019531F);
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel(param0.bakeLayer(param2)), new HumanoidModel(param0.bakeLayer(param3))));
    }

    private static PiglinModel<Mob> createModel(EntityModelSet param0, ModelLayerLocation param1, boolean param2) {
        PiglinModel<Mob> var0 = new PiglinModel<>(param0.bakeLayer(param1));
        if (param2) {
            var0.rightEar.visible = false;
        }

        return var0;
    }

    public ResourceLocation getTextureLocation(Mob param0) {
        ResourceLocation var0 = TEXTURES.get(param0.getType());
        if (var0 == null) {
            throw new IllegalArgumentException("I don't know what texture to use for " + param0.getType());
        } else {
            return var0;
        }
    }

    protected boolean isShaking(Mob param0) {
        return super.isShaking(param0) || param0 instanceof AbstractPiglin && ((AbstractPiglin)param0).isConverting();
    }
}
