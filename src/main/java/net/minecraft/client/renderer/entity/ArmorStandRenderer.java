package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.ArmorStandModel;
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

    public ArmorStandRenderer(EntityRenderDispatcher param0) {
        super(param0, new ArmorStandModel(), 0.0F);
        this.addLayer(new HumanoidArmorLayer<>(this, new ArmorStandArmorModel(0.5F), new ArmorStandArmorModel(1.0F)));
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new ElytraLayer<>(this));
        this.addLayer(new CustomHeadLayer<>(this));
    }

    public ResourceLocation getTextureLocation(ArmorStand param0) {
        return DEFAULT_SKIN_LOCATION;
    }

    protected void setupRotations(ArmorStand param0, PoseStack param1, float param2, float param3, float param4) {
        param1.mulPose(Vector3f.YP.rotation(180.0F - param3, true));
        float var0 = (float)(param0.level.getGameTime() - param0.lastHit) + param4;
        if (var0 < 5.0F) {
            param1.mulPose(Vector3f.YP.rotation(Mth.sin(var0 / 1.5F * (float) Math.PI) * 3.0F, true));
        }

    }

    protected boolean shouldShowName(ArmorStand param0) {
        double var0 = this.entityRenderDispatcher.distanceToSqr(param0);
        float var1 = param0.isCrouching() ? 32.0F : 64.0F;
        return var0 >= (double)(var1 * var1) ? false : param0.isCustomNameVisible();
    }

    protected boolean isVisible(ArmorStand param0, boolean param1) {
        if (param0.isMarker()) {
            return !param0.isInvisible() && !param1;
        } else {
            return !param0.isInvisible() || param1;
        }
    }
}
