package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.FrogModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FrogRenderer extends MobRenderer<Frog, FrogModel<Frog>> {
    private static final Map<Frog.Variant, ResourceLocation> TEXTURE_BY_TYPE = Util.make(Maps.newHashMap(), param0 -> {
        for(Frog.Variant var0 : Frog.Variant.values()) {
            param0.put(var0, new ResourceLocation(String.format("textures/entity/frog/%s_frog.png", var0.getName())));
        }

    });

    public FrogRenderer(EntityRendererProvider.Context param0) {
        super(param0, new FrogModel<>(param0.bakeLayer(ModelLayers.FROG)), 0.3F);
    }

    public ResourceLocation getTextureLocation(Frog param0) {
        return TEXTURE_BY_TYPE.get(param0.getVariant());
    }
}
