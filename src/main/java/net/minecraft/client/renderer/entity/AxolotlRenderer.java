package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Locale;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.AxolotlModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AxolotlRenderer extends MobRenderer<Axolotl, AxolotlModel<Axolotl>> {
    private static final Map<Axolotl.Variant, ResourceLocation> TEXTURE_BY_TYPE = Util.make(Maps.newHashMap(), param0 -> {
        for(Axolotl.Variant var0 : Axolotl.Variant.BY_ID) {
            param0.put(var0, new ResourceLocation(String.format(Locale.ROOT, "textures/entity/axolotl/axolotl_%s.png", var0.getName())));
        }

    });

    public AxolotlRenderer(EntityRendererProvider.Context param0) {
        super(param0, new AxolotlModel<>(param0.bakeLayer(ModelLayers.AXOLOTL)), 0.5F);
    }

    public ResourceLocation getTextureLocation(Axolotl param0) {
        return TEXTURE_BY_TYPE.get(param0.getVariant());
    }
}
