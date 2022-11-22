package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.client.renderer.entity.layers.HorseMarkingLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class HorseRenderer extends AbstractHorseRenderer<Horse, HorseModel<Horse>> {
    private static final Map<Variant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(Variant.class), param0 -> {
        param0.put(Variant.WHITE, new ResourceLocation("textures/entity/horse/horse_white.png"));
        param0.put(Variant.CREAMY, new ResourceLocation("textures/entity/horse/horse_creamy.png"));
        param0.put(Variant.CHESTNUT, new ResourceLocation("textures/entity/horse/horse_chestnut.png"));
        param0.put(Variant.BROWN, new ResourceLocation("textures/entity/horse/horse_brown.png"));
        param0.put(Variant.BLACK, new ResourceLocation("textures/entity/horse/horse_black.png"));
        param0.put(Variant.GRAY, new ResourceLocation("textures/entity/horse/horse_gray.png"));
        param0.put(Variant.DARK_BROWN, new ResourceLocation("textures/entity/horse/horse_darkbrown.png"));
    });

    public HorseRenderer(EntityRendererProvider.Context param0) {
        super(param0, new HorseModel<>(param0.bakeLayer(ModelLayers.HORSE)), 1.1F);
        this.addLayer(new HorseMarkingLayer(this));
        this.addLayer(new HorseArmorLayer(this, param0.getModelSet()));
    }

    public ResourceLocation getTextureLocation(Horse param0) {
        return LOCATION_BY_VARIANT.get(param0.getVariant());
    }
}
