package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.PandaHoldsItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Panda;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PandaRenderer extends MobRenderer<Panda, PandaModel<Panda>> {
    private static final Map<Panda.Gene, ResourceLocation> TEXTURES = Util.make(Maps.newEnumMap(Panda.Gene.class), param0 -> {
        param0.put(Panda.Gene.NORMAL, new ResourceLocation("textures/entity/panda/panda.png"));
        param0.put(Panda.Gene.LAZY, new ResourceLocation("textures/entity/panda/lazy_panda.png"));
        param0.put(Panda.Gene.WORRIED, new ResourceLocation("textures/entity/panda/worried_panda.png"));
        param0.put(Panda.Gene.PLAYFUL, new ResourceLocation("textures/entity/panda/playful_panda.png"));
        param0.put(Panda.Gene.BROWN, new ResourceLocation("textures/entity/panda/brown_panda.png"));
        param0.put(Panda.Gene.WEAK, new ResourceLocation("textures/entity/panda/weak_panda.png"));
        param0.put(Panda.Gene.AGGRESSIVE, new ResourceLocation("textures/entity/panda/aggressive_panda.png"));
    });

    public PandaRenderer(EntityRendererProvider.Context param0) {
        super(param0, new PandaModel<>(param0.bakeLayer(ModelLayers.PANDA)), 0.9F);
        this.addLayer(new PandaHoldsItemLayer(this));
    }

    public ResourceLocation getTextureLocation(Panda param0) {
        return TEXTURES.getOrDefault(param0.getVariant(), TEXTURES.get(Panda.Gene.NORMAL));
    }

    protected void setupRotations(Panda param0, PoseStack param1, float param2, float param3, float param4) {
        super.setupRotations(param0, param1, param2, param3, param4);
        if (param0.rollCounter > 0) {
            int var0 = param0.rollCounter;
            int var1 = var0 + 1;
            float var2 = 7.0F;
            float var3 = param0.isBaby() ? 0.3F : 0.8F;
            if (var0 < 8) {
                float var4 = (float)(90 * var0) / 7.0F;
                float var5 = (float)(90 * var1) / 7.0F;
                float var6 = this.getAngle(var4, var5, var1, param4, 8.0F);
                param1.translate(0.0, (double)((var3 + 0.2F) * (var6 / 90.0F)), 0.0);
                param1.mulPose(Vector3f.XP.rotationDegrees(-var6));
            } else if (var0 < 16) {
                float var7 = ((float)var0 - 8.0F) / 7.0F;
                float var8 = 90.0F + 90.0F * var7;
                float var9 = 90.0F + 90.0F * ((float)var1 - 8.0F) / 7.0F;
                float var10 = this.getAngle(var8, var9, var1, param4, 16.0F);
                param1.translate(0.0, (double)(var3 + 0.2F + (var3 - 0.2F) * (var10 - 90.0F) / 90.0F), 0.0);
                param1.mulPose(Vector3f.XP.rotationDegrees(-var10));
            } else if ((float)var0 < 24.0F) {
                float var11 = ((float)var0 - 16.0F) / 7.0F;
                float var12 = 180.0F + 90.0F * var11;
                float var13 = 180.0F + 90.0F * ((float)var1 - 16.0F) / 7.0F;
                float var14 = this.getAngle(var12, var13, var1, param4, 24.0F);
                param1.translate(0.0, (double)(var3 + var3 * (270.0F - var14) / 90.0F), 0.0);
                param1.mulPose(Vector3f.XP.rotationDegrees(-var14));
            } else if (var0 < 32) {
                float var15 = ((float)var0 - 24.0F) / 7.0F;
                float var16 = 270.0F + 90.0F * var15;
                float var17 = 270.0F + 90.0F * ((float)var1 - 24.0F) / 7.0F;
                float var18 = this.getAngle(var16, var17, var1, param4, 32.0F);
                param1.translate(0.0, (double)(var3 * ((360.0F - var18) / 90.0F)), 0.0);
                param1.mulPose(Vector3f.XP.rotationDegrees(-var18));
            }
        }

        float var19 = param0.getSitAmount(param4);
        if (var19 > 0.0F) {
            param1.translate(0.0, (double)(0.8F * var19), 0.0);
            param1.mulPose(Vector3f.XP.rotationDegrees(Mth.lerp(var19, param0.xRot, param0.xRot + 90.0F)));
            param1.translate(0.0, (double)(-1.0F * var19), 0.0);
            if (param0.isScared()) {
                float var20 = (float)(Math.cos((double)param0.tickCount * 1.25) * Math.PI * 0.05F);
                param1.mulPose(Vector3f.YP.rotationDegrees(var20));
                if (param0.isBaby()) {
                    param1.translate(0.0, 0.8F, 0.55F);
                }
            }
        }

        float var21 = param0.getLieOnBackAmount(param4);
        if (var21 > 0.0F) {
            float var22 = param0.isBaby() ? 0.5F : 1.3F;
            param1.translate(0.0, (double)(var22 * var21), 0.0);
            param1.mulPose(Vector3f.XP.rotationDegrees(Mth.lerp(var21, param0.xRot, param0.xRot + 180.0F)));
        }

    }

    private float getAngle(float param0, float param1, int param2, float param3, float param4) {
        return (float)param2 < param4 ? Mth.lerp(param3, param0, param1) : param0;
    }
}
