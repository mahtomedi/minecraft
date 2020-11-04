package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.MushroomCowMushroomLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MushroomCowRenderer extends MobRenderer<MushroomCow, CowModel<MushroomCow>> {
    private static final Map<MushroomCow.MushroomType, ResourceLocation> TEXTURES = Util.make(Maps.newHashMap(), param0 -> {
        param0.put(MushroomCow.MushroomType.BROWN, new ResourceLocation("textures/entity/cow/brown_mooshroom.png"));
        param0.put(MushroomCow.MushroomType.RED, new ResourceLocation("textures/entity/cow/red_mooshroom.png"));
    });

    public MushroomCowRenderer(EntityRendererProvider.Context param0) {
        super(param0, new CowModel<>(param0.getLayer(ModelLayers.MOOSHROOM)), 0.7F);
        this.addLayer(new MushroomCowMushroomLayer<>(this));
    }

    public ResourceLocation getTextureLocation(MushroomCow param0) {
        return TEXTURES.get(param0.getMushroomType());
    }
}
