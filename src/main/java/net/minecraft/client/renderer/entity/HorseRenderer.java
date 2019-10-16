package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.client.renderer.texture.LayeredTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class HorseRenderer extends AbstractHorseRenderer<Horse, HorseModel<Horse>> {
    private static final Map<String, ResourceLocation> LAYERED_LOCATION_CACHE = Maps.newHashMap();

    public HorseRenderer(EntityRenderDispatcher param0) {
        super(param0, new HorseModel<>(0.0F), 1.1F);
        this.addLayer(new HorseArmorLayer(this));
    }

    public ResourceLocation getTextureLocation(Horse param0) {
        String var0 = param0.getLayeredTextureHashName();
        ResourceLocation var1 = LAYERED_LOCATION_CACHE.get(var0);
        if (var1 == null) {
            var1 = new ResourceLocation(var0);
            Minecraft.getInstance().getTextureManager().register(var1, new LayeredTexture(param0.getLayeredTextureLayers()));
            LAYERED_LOCATION_CACHE.put(var0, var1);
        }

        return var1;
    }
}
