package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
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

    protected ResourceLocation getTextureLocation(ArmorStand param0) {
        return DEFAULT_SKIN_LOCATION;
    }

    protected void setupRotations(ArmorStand param0, float param1, float param2, float param3) {
        RenderSystem.rotatef(180.0F - param2, 0.0F, 1.0F, 0.0F);
        float var0 = (float)(param0.level.getGameTime() - param0.lastHit) + param3;
        if (var0 < 5.0F) {
            RenderSystem.rotatef(Mth.sin(var0 / 1.5F * (float) Math.PI) * 3.0F, 0.0F, 1.0F, 0.0F);
        }

    }

    protected boolean shouldShowName(ArmorStand param0) {
        return param0.isCustomNameVisible();
    }

    public void render(ArmorStand param0, double param1, double param2, double param3, float param4, float param5) {
        if (param0.isMarker()) {
            this.onlySolidLayers = true;
        }

        super.render(param0, param1, param2, param3, param4, param5);
        if (param0.isMarker()) {
            this.onlySolidLayers = false;
        }

    }
}
