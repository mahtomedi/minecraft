package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HumanoidArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();
    private final A innerModel;
    private final A outerModel;

    public HumanoidArmorLayer(RenderLayerParent<T, M> param0, A param1, A param2) {
        super(param0);
        this.innerModel = param1;
        this.outerModel = param2;
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        this.renderArmorPiece(param0, param1, param3, EquipmentSlot.CHEST, param2, this.getArmorModel(EquipmentSlot.CHEST));
        this.renderArmorPiece(param0, param1, param3, EquipmentSlot.LEGS, param2, this.getArmorModel(EquipmentSlot.LEGS));
        this.renderArmorPiece(param0, param1, param3, EquipmentSlot.FEET, param2, this.getArmorModel(EquipmentSlot.FEET));
        this.renderArmorPiece(param0, param1, param3, EquipmentSlot.HEAD, param2, this.getArmorModel(EquipmentSlot.HEAD));
    }

    private void renderArmorPiece(PoseStack param0, MultiBufferSource param1, T param2, EquipmentSlot param3, int param4, A param5) {
        ItemStack var0 = param2.getItemBySlot(param3);
        if (var0.getItem() instanceof ArmorItem) {
            ArmorItem var1 = (ArmorItem)var0.getItem();
            if (var1.getSlot() == param3) {
                this.getParentModel().copyPropertiesTo(param5);
                this.setPartVisibility(param5, param3);
                boolean var2 = this.usesInnerModel(param3);
                boolean var3 = var0.hasFoil();
                if (var1 instanceof DyeableArmorItem) {
                    int var4 = ((DyeableArmorItem)var1).getColor(var0);
                    float var5 = (float)(var4 >> 16 & 0xFF) / 255.0F;
                    float var6 = (float)(var4 >> 8 & 0xFF) / 255.0F;
                    float var7 = (float)(var4 & 0xFF) / 255.0F;
                    this.renderModel(param0, param1, param4, var1, var3, param5, var2, var5, var6, var7, null);
                    this.renderModel(param0, param1, param4, var1, var3, param5, var2, 1.0F, 1.0F, 1.0F, "overlay");
                } else {
                    this.renderModel(param0, param1, param4, var1, var3, param5, var2, 1.0F, 1.0F, 1.0F, null);
                }

            }
        }
    }

    protected void setPartVisibility(A param0, EquipmentSlot param1) {
        param0.setAllVisible(false);
        switch(param1) {
            case HEAD:
                param0.head.visible = true;
                param0.hat.visible = true;
                break;
            case CHEST:
                param0.body.visible = true;
                param0.rightArm.visible = true;
                param0.leftArm.visible = true;
                break;
            case LEGS:
                param0.body.visible = true;
                param0.rightLeg.visible = true;
                param0.leftLeg.visible = true;
                break;
            case FEET:
                param0.rightLeg.visible = true;
                param0.leftLeg.visible = true;
        }

    }

    private void renderModel(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        ArmorItem param3,
        boolean param4,
        A param5,
        boolean param6,
        float param7,
        float param8,
        float param9,
        @Nullable String param10
    ) {
        VertexConsumer var0 = ItemRenderer.getArmorFoilBuffer(
            param1, RenderType.armorCutoutNoCull(this.getArmorLocation(param3, param6, param10)), false, param4
        );
        param5.renderToBuffer(param0, var0, param2, OverlayTexture.NO_OVERLAY, param7, param8, param9, 1.0F);
    }

    private A getArmorModel(EquipmentSlot param0) {
        return (A)(this.usesInnerModel(param0) ? this.innerModel : this.outerModel);
    }

    private boolean usesInnerModel(EquipmentSlot param0) {
        return param0 == EquipmentSlot.LEGS;
    }

    private ResourceLocation getArmorLocation(ArmorItem param0, boolean param1, @Nullable String param2) {
        String var0 = "textures/models/armor/" + param0.getMaterial().getName() + "_layer_" + (param1 ? 2 : 1) + (param2 == null ? "" : "_" + param2) + ".png";
        return ARMOR_LOCATION_CACHE.computeIfAbsent(var0, ResourceLocation::new);
    }
}
