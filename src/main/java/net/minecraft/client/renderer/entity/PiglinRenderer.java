package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PiglinRenderer extends HumanoidMobRenderer<Mob, PiglinModel<Mob>> {
    private static final Map<EntityType<?>, ResourceLocation> resourceLocations = ImmutableMap.of(
        EntityType.PIGLIN,
        new ResourceLocation("textures/entity/piglin/piglin.png"),
        EntityType.ZOMBIFIED_PIGLIN,
        new ResourceLocation("textures/entity/piglin/zombified_piglin.png"),
        EntityType.PIGLIN_BRUTE,
        new ResourceLocation("textures/entity/piglin/piglin_brute.png")
    );

    public PiglinRenderer(EntityRenderDispatcher param0, boolean param1) {
        super(param0, createModel(param1), 0.5F, 1.0019531F, 1.0F, 1.0019531F);
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel(0.5F), new HumanoidModel(1.02F)));
    }

    private static PiglinModel<Mob> createModel(boolean param0) {
        PiglinModel<Mob> var0 = new PiglinModel<>(0.0F, 64, 64);
        if (param0) {
            var0.earLeft.visible = false;
        }

        return var0;
    }

    @Override
    public ResourceLocation getTextureLocation(Mob param0) {
        ResourceLocation var0 = resourceLocations.get(param0.getType());
        if (var0 == null) {
            throw new IllegalArgumentException("I don't know what texture to use for " + param0.getType());
        } else {
            return var0;
        }
    }

    protected boolean isShaking(Mob param0) {
        return param0 instanceof AbstractPiglin && ((AbstractPiglin)param0).isConverting();
    }
}
