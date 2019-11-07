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
    private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();

    protected AbstractArmorLayer(RenderLayerParent<T, M> param0, A param1, A param2) {
        super(param0);
        this.innerModel = param1;
        this.outerModel = param2;
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        this.renderArmorPiece(param0, param1, param3, param4, param5, param6, param7, param8, param9, EquipmentSlot.CHEST, param2);
        this.renderArmorPiece(param0, param1, param3, param4, param5, param6, param7, param8, param9, EquipmentSlot.LEGS, param2);
        this.renderArmorPiece(param0, param1, param3, param4, param5, param6, param7, param8, param9, EquipmentSlot.FEET, param2);
        this.renderArmorPiece(param0, param1, param3, param4, param5, param6, param7, param8, param9, EquipmentSlot.HEAD, param2);
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
        int param10
    ) {
        ItemStack var0 = param2.getItemBySlot(param9);
        if (var0.getItem() instanceof ArmorItem) {
            ArmorItem var1 = (ArmorItem)var0.getItem();
            if (var1.getSlot() == param9) {
                A var2 = this.getArmorModel(param9);
                this.getParentModel().copyPropertiesTo(var2);
                var2.prepareMobModel(param2, param3, param4, param5);
                this.setPartVisibility(var2, param9);
                var2.setupAnim(param2, param3, param4, param6, param7, param8);
                boolean var3 = this.usesInnerModel(param9);
                boolean var4 = var0.hasFoil();
                if (var1 instanceof DyeableArmorItem) {
                    int var5 = ((DyeableArmorItem)var1).getColor(var0);
                    float var6 = (float)(var5 >> 16 & 0xFF) / 255.0F;
                    float var7 = (float)(var5 >> 8 & 0xFF) / 255.0F;
                    float var8 = (float)(var5 & 0xFF) / 255.0F;
                    this.renderModel(param0, param1, param10, var1, var4, var2, var3, var6, var7, var8, null);
                    this.renderModel(param0, param1, param10, var1, var4, var2, var3, 1.0F, 1.0F, 1.0F, "overlay");
                } else {
                    this.renderModel(param0, param1, param10, var1, var4, var2, var3, 1.0F, 1.0F, 1.0F, null);
                }

            }
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
        VertexConsumer var0 = ItemRenderer.getFoilBuffer(param1, RenderType.entityCutoutNoCull(this.getArmorLocation(param3, param6, param10)), false, param4);
        param5.renderToBuffer(param0, var0, param2, OverlayTexture.NO_OVERLAY, param7, param8, param9);
    }

    public A getArmorModel(EquipmentSlot param0) {
        return (A)(this.usesInnerModel(param0) ? this.innerModel : this.outerModel);
    }

    private boolean usesInnerModel(EquipmentSlot param0) {
        return param0 == EquipmentSlot.LEGS;
    }

    private ResourceLocation getArmorLocation(ArmorItem param0, boolean param1, @Nullable String param2) {
        String var0 = "textures/models/armor/" + param0.getMaterial().getName() + "_layer_" + (param1 ? 2 : 1) + (param2 == null ? "" : "_" + param2) + ".png";
        return ARMOR_LOCATION_CACHE.computeIfAbsent(var0, ResourceLocation::new);
    }

    protected abstract void setPartVisibility(A var1, EquipmentSlot var2);

    protected abstract void hideAllArmor(A var1);
}
