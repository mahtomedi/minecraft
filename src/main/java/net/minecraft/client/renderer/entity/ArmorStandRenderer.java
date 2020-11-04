package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import javax.annotation.Nullable;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArmorStandRenderer extends LivingEntityRenderer<ArmorStand, ArmorStandArmorModel> {
    public static final ResourceLocation DEFAULT_SKIN_LOCATION = new ResourceLocation("textures/entity/armorstand/wood.png");

    public ArmorStandRenderer(EntityRendererProvider.Context param0) {
        super(param0, new ArmorStandModel(param0.getLayer(ModelLayers.ARMOR_STAND)), 0.0F);
        this.addLayer(
            new HumanoidArmorLayer<>(
                this,
                new ArmorStandArmorModel(param0.getLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR)),
                new ArmorStandArmorModel(param0.getLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR))
            )
        );
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new ElytraLayer<>(this, param0.getModelSet()));
        this.addLayer(new CustomHeadLayer<>(this, param0.getModelSet()));
    }

    public ResourceLocation getTextureLocation(ArmorStand param0) {
        return DEFAULT_SKIN_LOCATION;
    }

    protected void setupRotations(ArmorStand param0, PoseStack param1, float param2, float param3, float param4) {
        param1.mulPose(Vector3f.YP.rotationDegrees(180.0F - param3));
        float var0 = (float)(param0.level.getGameTime() - param0.lastHit) + param4;
        if (var0 < 5.0F) {
            param1.mulPose(Vector3f.YP.rotationDegrees(Mth.sin(var0 / 1.5F * (float) Math.PI) * 3.0F));
        }

    }

    protected boolean shouldShowName(ArmorStand param0) {
        double var0 = this.entityRenderDispatcher.distanceToSqr(param0);
        float var1 = param0.isCrouching() ? 32.0F : 64.0F;
        return var0 >= (double)(var1 * var1) ? false : param0.isCustomNameVisible();
    }

    @Nullable
    protected RenderType getRenderType(ArmorStand param0, boolean param1, boolean param2, boolean param3) {
        if (!param0.isMarker()) {
            return super.getRenderType(param0, param1, param2, param3);
        } else {
            ResourceLocation var0 = this.getTextureLocation(param0);
            if (param2) {
                return RenderType.entityTranslucent(var0, false);
            } else {
                return param1 ? RenderType.entityCutoutNoCull(var0, false) : null;
            }
        }
    }
}
