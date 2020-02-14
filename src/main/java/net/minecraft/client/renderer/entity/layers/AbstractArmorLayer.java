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
public abstract class AbstractArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
    protected final A innerModel;
    protected final A outerModel;
    protected static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();

    protected AbstractArmorLayer(RenderLayerParent<T, M> param0, A param1, A param2) {
        super(param0);
        this.innerModel = param1;
        this.outerModel = param2;
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        this.renderArmorPiece(
            param0, param1, param3, param4, param5, param6, param7, param8, param9, EquipmentSlot.CHEST, param2, this.getArmorModel(EquipmentSlot.CHEST)
        );
        this.renderArmorPiece(
            param0, param1, param3, param4, param5, param6, param7, param8, param9, EquipmentSlot.LEGS, param2, this.getArmorModel(EquipmentSlot.LEGS)
        );
        this.renderArmorPiece(
            param0, param1, param3, param4, param5, param6, param7, param8, param9, EquipmentSlot.FEET, param2, this.getArmorModel(EquipmentSlot.FEET)
        );
        this.renderArmorPiece(
            param0, param1, param3, param4, param5, param6, param7, param8, param9, EquipmentSlot.HEAD, param2, this.getArmorModel(EquipmentSlot.HEAD)
        );
    }

    private void renderArmorPiece(
        PoseStack param0,
        MultiBufferSource param1,
        T param2,
        float param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        EquipmentSlot param9,
        int param10,
        A param11
    ) {
        ItemStack var0 = param2.getItemBySlot(param9);
        if (var0.getItem() instanceof ArmorItem) {
            ArmorItem var1 = (ArmorItem)var0.getItem();
            if (var1.getSlot() == param9) {
                this.getParentModel().copyPropertiesTo(param11);
                param11.prepareMobModel(param2, param3, param4, param5);
                this.setPartVisibility(param11, param9);
                param11.setupAnim(param2, param3, param4, param6, param7, param8);
                boolean var2 = this.usesInnerModel(param9);
                boolean var3 = var0.hasFoil();
                if (var1 instanceof DyeableArmorItem) {
                    int var4 = ((DyeableArmorItem)var1).getColor(var0);
                    float var5 = (float)(var4 >> 16 & 0xFF) / 255.0F;
                    float var6 = (float)(var4 >> 8 & 0xFF) / 255.0F;
                    float var7 = (float)(var4 & 0xFF) / 255.0F;
                    this.renderModel(param9, param0, param1, param10, var1, var3, param11, var2, var5, var6, var7, null);
                    this.renderModel(param9, param0, param1, param10, var1, var3, param11, var2, 1.0F, 1.0F, 1.0F, "overlay");
                } else {
                    this.renderModel(param9, param0, param1, param10, var1, var3, param11, var2, 1.0F, 1.0F, 1.0F, null);
                }

            }
        }
    }

    private void renderModel(
        EquipmentSlot param0,
        PoseStack param1,
        MultiBufferSource param2,
        int param3,
        ArmorItem param4,
        boolean param5,
        A param6,
        boolean param7,
        float param8,
        float param9,
        float param10,
        @Nullable String param11
    ) {
        VertexConsumer var0 = ItemRenderer.getFoilBuffer(
            param2, RenderType.entityCutoutNoCull(this.getArmorLocation(param0, param4, param7, param11)), false, param5
        );
        param6.renderToBuffer(param1, var0, param3, OverlayTexture.NO_OVERLAY, param8, param9, param10, 1.0F);
    }

    public A getArmorModel(EquipmentSlot param0) {
        return (A)(this.usesInnerModel(param0) ? this.innerModel : this.outerModel);
    }

    private boolean usesInnerModel(EquipmentSlot param0) {
        return param0 == EquipmentSlot.LEGS;
    }

    protected ResourceLocation getArmorLocation(EquipmentSlot param0, ArmorItem param1, boolean param2, @Nullable String param3) {
        String var0 = "textures/models/armor/" + param1.getMaterial().getName() + "_layer_" + (param2 ? 2 : 1) + (param3 == null ? "" : "_" + param3) + ".png";
        return ARMOR_LOCATION_CACHE.computeIfAbsent(var0, ResourceLocation::new);
    }

    protected abstract void setPartVisibility(A var1, EquipmentSlot var2);

    protected abstract void hideAllArmor(A var1);
}
