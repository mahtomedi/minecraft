package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
    protected static final ResourceLocation ENCHANT_GLINT_LOCATION = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    protected final A innerModel;
    protected final A outerModel;
    private float alpha = 1.0F;
    private float red = 1.0F;
    private float green = 1.0F;
    private float blue = 1.0F;
    private boolean colorized;
    private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();

    protected AbstractArmorLayer(RenderLayerParent<T, M> param0, A param1, A param2) {
        super(param0);
        this.innerModel = param1;
        this.outerModel = param2;
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        this.renderArmorPiece(param0, param1, param2, param3, param4, param5, param6, param7, EquipmentSlot.CHEST);
        this.renderArmorPiece(param0, param1, param2, param3, param4, param5, param6, param7, EquipmentSlot.LEGS);
        this.renderArmorPiece(param0, param1, param2, param3, param4, param5, param6, param7, EquipmentSlot.FEET);
        this.renderArmorPiece(param0, param1, param2, param3, param4, param5, param6, param7, EquipmentSlot.HEAD);
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }

    private void renderArmorPiece(
        T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7, EquipmentSlot param8
    ) {
        ItemStack var0 = param0.getItemBySlot(param8);
        if (var0.getItem() instanceof ArmorItem) {
            ArmorItem var1 = (ArmorItem)var0.getItem();
            if (var1.getSlot() == param8) {
                A var2 = this.getArmorModel(param8);
                this.getParentModel().copyPropertiesTo(var2);
                var2.prepareMobModel(param0, param1, param2, param3);
                this.setPartVisibility(var2, param8);
                boolean var3 = this.usesInnerModel(param8);
                this.bindTexture(this.getArmorLocation(var1, var3));
                if (var1 instanceof DyeableArmorItem) {
                    int var4 = ((DyeableArmorItem)var1).getColor(var0);
                    float var5 = (float)(var4 >> 16 & 0xFF) / 255.0F;
                    float var6 = (float)(var4 >> 8 & 0xFF) / 255.0F;
                    float var7 = (float)(var4 & 0xFF) / 255.0F;
                    RenderSystem.color4f(this.red * var5, this.green * var6, this.blue * var7, this.alpha);
                    var2.render(param0, param1, param2, param4, param5, param6, param7);
                    this.bindTexture(this.getArmorLocation(var1, var3, "overlay"));
                }

                RenderSystem.color4f(this.red, this.green, this.blue, this.alpha);
                var2.render(param0, param1, param2, param4, param5, param6, param7);
                if (!this.colorized && var0.isEnchanted()) {
                    renderFoil(this::bindTexture, param0, var2, param1, param2, param3, param4, param5, param6, param7);
                }

            }
        }
    }

    public A getArmorModel(EquipmentSlot param0) {
        return (A)(this.usesInnerModel(param0) ? this.innerModel : this.outerModel);
    }

    private boolean usesInnerModel(EquipmentSlot param0) {
        return param0 == EquipmentSlot.LEGS;
    }

    public static <T extends Entity> void renderFoil(
        Consumer<ResourceLocation> param0,
        T param1,
        EntityModel<T> param2,
        float param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9
    ) {
        float var0 = (float)param1.tickCount + param5;
        param0.accept(ENCHANT_GLINT_LOCATION);
        GameRenderer var1 = Minecraft.getInstance().gameRenderer;
        var1.resetFogColor(true);
        RenderSystem.enableBlend();
        RenderSystem.depthFunc(514);
        RenderSystem.depthMask(false);
        float var2 = 0.5F;
        RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);

        for(int var3 = 0; var3 < 2; ++var3) {
            RenderSystem.disableLighting();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
            float var4 = 0.76F;
            RenderSystem.color4f(0.38F, 0.19F, 0.608F, 1.0F);
            RenderSystem.matrixMode(5890);
            RenderSystem.loadIdentity();
            float var5 = 0.33333334F;
            RenderSystem.scalef(0.33333334F, 0.33333334F, 0.33333334F);
            RenderSystem.rotatef(30.0F - (float)var3 * 60.0F, 0.0F, 0.0F, 1.0F);
            RenderSystem.translatef(0.0F, var0 * (0.001F + (float)var3 * 0.003F) * 20.0F, 0.0F);
            RenderSystem.matrixMode(5888);
            param2.render(param1, param3, param4, param6, param7, param8, param9);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }

        RenderSystem.matrixMode(5890);
        RenderSystem.loadIdentity();
        RenderSystem.matrixMode(5888);
        RenderSystem.enableLighting();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
        RenderSystem.disableBlend();
        var1.resetFogColor(false);
    }

    private ResourceLocation getArmorLocation(ArmorItem param0, boolean param1) {
        return this.getArmorLocation(param0, param1, null);
    }

    private ResourceLocation getArmorLocation(ArmorItem param0, boolean param1, @Nullable String param2) {
        String var0 = "textures/models/armor/" + param0.getMaterial().getName() + "_layer_" + (param1 ? 2 : 1) + (param2 == null ? "" : "_" + param2) + ".png";
        return ARMOR_LOCATION_CACHE.computeIfAbsent(var0, ResourceLocation::new);
    }

    protected abstract void setPartVisibility(A var1, EquipmentSlot var2);

    protected abstract void hideAllArmor(A var1);
}
