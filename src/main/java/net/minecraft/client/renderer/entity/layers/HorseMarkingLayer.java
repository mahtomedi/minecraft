package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseMarkingLayer extends RenderLayer<Horse, HorseModel<Horse>> {
    private static final Map<Markings, ResourceLocation> LOCATION_BY_MARKINGS = Util.make(Maps.newEnumMap(Markings.class), param0 -> {
        param0.put(Markings.NONE, null);
        param0.put(Markings.WHITE, new ResourceLocation("textures/entity/horse/horse_markings_white.png"));
        param0.put(Markings.WHITE_FIELD, new ResourceLocation("textures/entity/horse/horse_markings_whitefield.png"));
        param0.put(Markings.WHITE_DOTS, new ResourceLocation("textures/entity/horse/horse_markings_whitedots.png"));
        param0.put(Markings.BLACK_DOTS, new ResourceLocation("textures/entity/horse/horse_markings_blackdots.png"));
    });

    public HorseMarkingLayer(RenderLayerParent<Horse, HorseModel<Horse>> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        Horse param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9
    ) {
        ResourceLocation var0 = LOCATION_BY_MARKINGS.get(param3.getMarkings());
        if (var0 != null) {
            VertexConsumer var1 = param1.getBuffer(RenderType.entityTranslucent(var0));
            this.getParentModel().renderToBuffer(param0, var1, param2, LivingEntityRenderer.getOverlayCoords(param3, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
