package net.minecraft.client.renderer;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemInHandRenderer {
    private static final RenderType MAP_BACKGROUND = RenderType.text(new ResourceLocation("textures/map/map_background.png"));
    private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(new ResourceLocation("textures/map/map_background_checkerboard.png"));
    private final Minecraft minecraft;
    private ItemStack mainHandItem = ItemStack.EMPTY;
    private ItemStack offHandItem = ItemStack.EMPTY;
    private float mainHandHeight;
    private float oMainHandHeight;
    private float offHandHeight;
    private float oOffHandHeight;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemRenderer itemRenderer;

    public ItemInHandRenderer(Minecraft param0) {
        this.minecraft = param0;
        this.entityRenderDispatcher = param0.getEntityRenderDispatcher();
        this.itemRenderer = param0.getItemRenderer();
    }

    public void renderItem(
        LivingEntity param0, ItemStack param1, ItemTransforms.TransformType param2, boolean param3, PoseStack param4, MultiBufferSource param5, int param6
    ) {
        if (!param1.isEmpty()) {
            this.itemRenderer
                .renderStatic(
                    param0, param1, param2, param3, param4, param5, param0.level, param6, OverlayTexture.NO_OVERLAY, param0.getId() + param2.ordinal()
                );
        }
    }

    private float calculateMapTilt(float param0) {
        float var0 = 1.0F - param0 / 45.0F + 0.1F;
        var0 = Mth.clamp(var0, 0.0F, 1.0F);
        return -Mth.cos(var0 * (float) Math.PI) * 0.5F + 0.5F;
    }

    private void renderMapHand(PoseStack param0, MultiBufferSource param1, int param2, HumanoidArm param3) {
        this.minecraft.getTextureManager().bind(this.minecraft.player.getSkinTextureLocation());
        PlayerRenderer var0 = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayer>getRenderer(this.minecraft.player);
        param0.pushPose();
        float var1 = param3 == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        param0.mulPose(Vector3f.YP.rotationDegrees(92.0F));
        param0.mulPose(Vector3f.XP.rotationDegrees(45.0F));
        param0.mulPose(Vector3f.ZP.rotationDegrees(var1 * -41.0F));
        param0.translate((double)(var1 * 0.3F), -1.1F, 0.45F);
        if (param3 == HumanoidArm.RIGHT) {
            var0.renderRightHand(param0, param1, param2, this.minecraft.player);
        } else {
            var0.renderLeftHand(param0, param1, param2, this.minecraft.player);
        }

        param0.popPose();
    }

    private void renderOneHandedMap(PoseStack param0, MultiBufferSource param1, int param2, float param3, HumanoidArm param4, float param5, ItemStack param6) {
        float var0 = param4 == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        param0.translate((double)(var0 * 0.125F), -0.125, 0.0);
        if (!this.minecraft.player.isInvisible()) {
            param0.pushPose();
            param0.mulPose(Vector3f.ZP.rotationDegrees(var0 * 10.0F));
            this.renderPlayerArm(param0, param1, param2, param3, param5, param4);
            param0.popPose();
        }

        param0.pushPose();
        param0.translate((double)(var0 * 0.51F), (double)(-0.08F + param3 * -1.2F), -0.75);
        float var1 = Mth.sqrt(param5);
        float var2 = Mth.sin(var1 * (float) Math.PI);
        float var3 = -0.5F * var2;
        float var4 = 0.4F * Mth.sin(var1 * (float) (Math.PI * 2));
        float var5 = -0.3F * Mth.sin(param5 * (float) Math.PI);
        param0.translate((double)(var0 * var3), (double)(var4 - 0.3F * var2), (double)var5);
        param0.mulPose(Vector3f.XP.rotationDegrees(var2 * -45.0F));
        param0.mulPose(Vector3f.YP.rotationDegrees(var0 * var2 * -30.0F));
        this.renderMap(param0, param1, param2, param6);
        param0.popPose();
    }

    private void renderTwoHandedMap(PoseStack param0, MultiBufferSource param1, int param2, float param3, float param4, float param5) {
        float var0 = Mth.sqrt(param5);
        float var1 = -0.2F * Mth.sin(param5 * (float) Math.PI);
        float var2 = -0.4F * Mth.sin(var0 * (float) Math.PI);
        param0.translate(0.0, (double)(-var1 / 2.0F), (double)var2);
        float var3 = this.calculateMapTilt(param3);
        param0.translate(0.0, (double)(0.04F + param4 * -1.2F + var3 * -0.5F), -0.72F);
        param0.mulPose(Vector3f.XP.rotationDegrees(var3 * -85.0F));
        if (!this.minecraft.player.isInvisible()) {
            param0.pushPose();
            param0.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            this.renderMapHand(param0, param1, param2, HumanoidArm.RIGHT);
            this.renderMapHand(param0, param1, param2, HumanoidArm.LEFT);
            param0.popPose();
        }

        float var4 = Mth.sin(var0 * (float) Math.PI);
        param0.mulPose(Vector3f.XP.rotationDegrees(var4 * 20.0F));
        param0.scale(2.0F, 2.0F, 2.0F);
        this.renderMap(param0, param1, param2, this.mainHandItem);
    }

    private void renderMap(PoseStack param0, MultiBufferSource param1, int param2, ItemStack param3) {
        param0.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        param0.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        param0.scale(0.38F, 0.38F, 0.38F);
        param0.translate(-0.5, -0.5, 0.0);
        param0.scale(0.0078125F, 0.0078125F, 0.0078125F);
        Integer var0 = MapItem.getMapId(param3);
        MapItemSavedData var1 = MapItem.getSavedData(var0, this.minecraft.level);
        VertexConsumer var2 = param1.getBuffer(var1 == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
        Matrix4f var3 = param0.last().pose();
        var2.vertex(var3, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(param2).endVertex();
        var2.vertex(var3, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(param2).endVertex();
        var2.vertex(var3, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(param2).endVertex();
        var2.vertex(var3, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(param2).endVertex();
        if (var1 != null) {
            this.minecraft.gameRenderer.getMapRenderer().render(param0, param1, var0, var1, false, param2);
        }

    }

    private void renderPlayerArm(PoseStack param0, MultiBufferSource param1, int param2, float param3, float param4, HumanoidArm param5) {
        boolean var0 = param5 != HumanoidArm.LEFT;
        float var1 = var0 ? 1.0F : -1.0F;
        float var2 = Mth.sqrt(param4);
        float var3 = -0.3F * Mth.sin(var2 * (float) Math.PI);
        float var4 = 0.4F * Mth.sin(var2 * (float) (Math.PI * 2));
        float var5 = -0.4F * Mth.sin(param4 * (float) Math.PI);
        param0.translate((double)(var1 * (var3 + 0.64000005F)), (double)(var4 + -0.6F + param3 * -0.6F), (double)(var5 + -0.71999997F));
        param0.mulPose(Vector3f.YP.rotationDegrees(var1 * 45.0F));
        float var6 = Mth.sin(param4 * param4 * (float) Math.PI);
        float var7 = Mth.sin(var2 * (float) Math.PI);
        param0.mulPose(Vector3f.YP.rotationDegrees(var1 * var7 * 70.0F));
        param0.mulPose(Vector3f.ZP.rotationDegrees(var1 * var6 * -20.0F));
        AbstractClientPlayer var8 = this.minecraft.player;
        this.minecraft.getTextureManager().bind(var8.getSkinTextureLocation());
        param0.translate((double)(var1 * -1.0F), 3.6F, 3.5);
        param0.mulPose(Vector3f.ZP.rotationDegrees(var1 * 120.0F));
        param0.mulPose(Vector3f.XP.rotationDegrees(200.0F));
        param0.mulPose(Vector3f.YP.rotationDegrees(var1 * -135.0F));
        param0.translate((double)(var1 * 5.6F), 0.0, 0.0);
        PlayerRenderer var9 = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayer>getRenderer(var8);
        if (var0) {
            var9.renderRightHand(param0, param1, param2, var8);
        } else {
            var9.renderLeftHand(param0, param1, param2, var8);
        }

    }

    private void applyEatTransform(PoseStack param0, float param1, HumanoidArm param2, ItemStack param3) {
        float var0 = (float)this.minecraft.player.getUseItemRemainingTicks() - param1 + 1.0F;
        float var1 = var0 / (float)param3.getUseDuration();
        if (var1 < 0.8F) {
            float var2 = Mth.abs(Mth.cos(var0 / 4.0F * (float) Math.PI) * 0.1F);
            param0.translate(0.0, (double)var2, 0.0);
        }

        float var3 = 1.0F - (float)Math.pow((double)var1, 27.0);
        int var4 = param2 == HumanoidArm.RIGHT ? 1 : -1;
        param0.translate((double)(var3 * 0.6F * (float)var4), (double)(var3 * -0.5F), (double)(var3 * 0.0F));
        param0.mulPose(Vector3f.YP.rotationDegrees((float)var4 * var3 * 90.0F));
        param0.mulPose(Vector3f.XP.rotationDegrees(var3 * 10.0F));
        param0.mulPose(Vector3f.ZP.rotationDegrees((float)var4 * var3 * 30.0F));
    }

    private void applyItemArmAttackTransform(PoseStack param0, HumanoidArm param1, float param2) {
        int var0 = param1 == HumanoidArm.RIGHT ? 1 : -1;
        float var1 = Mth.sin(param2 * param2 * (float) Math.PI);
        param0.mulPose(Vector3f.YP.rotationDegrees((float)var0 * (45.0F + var1 * -20.0F)));
        float var2 = Mth.sin(Mth.sqrt(param2) * (float) Math.PI);
        param0.mulPose(Vector3f.ZP.rotationDegrees((float)var0 * var2 * -20.0F));
        param0.mulPose(Vector3f.XP.rotationDegrees(var2 * -80.0F));
        param0.mulPose(Vector3f.YP.rotationDegrees((float)var0 * -45.0F));
    }

    private void applyItemArmTransform(PoseStack param0, HumanoidArm param1, float param2) {
        int var0 = param1 == HumanoidArm.RIGHT ? 1 : -1;
        param0.translate((double)((float)var0 * 0.56F), (double)(-0.52F + param2 * -0.6F), -0.72F);
    }

    public void renderHandsWithItems(float param0, PoseStack param1, MultiBufferSource.BufferSource param2, LocalPlayer param3, int param4) {
        float var0 = param3.getAttackAnim(param0);
        InteractionHand var1 = MoreObjects.firstNonNull(param3.swingingArm, InteractionHand.MAIN_HAND);
        float var2 = Mth.lerp(param0, param3.xRotO, param3.xRot);
        boolean var3 = true;
        boolean var4 = true;
        if (param3.isUsingItem()) {
            ItemStack var5 = param3.getUseItem();
            if (var5.is(Items.BOW) || var5.is(Items.CROSSBOW)) {
                var3 = param3.getUsedItemHand() == InteractionHand.MAIN_HAND;
                var4 = !var3;
            }

            InteractionHand var6 = param3.getUsedItemHand();
            if (var6 == InteractionHand.MAIN_HAND) {
                ItemStack var7 = param3.getOffhandItem();
                if (var7.is(Items.CROSSBOW) && CrossbowItem.isCharged(var7)) {
                    var4 = false;
                }
            }
        } else {
            ItemStack var8 = param3.getMainHandItem();
            ItemStack var9 = param3.getOffhandItem();
            if (var8.is(Items.CROSSBOW) && CrossbowItem.isCharged(var8)) {
                var4 = !var3;
            }

            if (var9.is(Items.CROSSBOW) && CrossbowItem.isCharged(var9)) {
                var3 = !var8.isEmpty();
                var4 = !var3;
            }
        }

        float var10 = Mth.lerp(param0, param3.xBobO, param3.xBob);
        float var11 = Mth.lerp(param0, param3.yBobO, param3.yBob);
        param1.mulPose(Vector3f.XP.rotationDegrees((param3.getViewXRot(param0) - var10) * 0.1F));
        param1.mulPose(Vector3f.YP.rotationDegrees((param3.getViewYRot(param0) - var11) * 0.1F));
        if (var3) {
            float var12 = var1 == InteractionHand.MAIN_HAND ? var0 : 0.0F;
            float var13 = 1.0F - Mth.lerp(param0, this.oMainHandHeight, this.mainHandHeight);
            this.renderArmWithItem(param3, param0, var2, InteractionHand.MAIN_HAND, var12, this.mainHandItem, var13, param1, param2, param4);
        }

        if (var4) {
            float var14 = var1 == InteractionHand.OFF_HAND ? var0 : 0.0F;
            float var15 = 1.0F - Mth.lerp(param0, this.oOffHandHeight, this.offHandHeight);
            this.renderArmWithItem(param3, param0, var2, InteractionHand.OFF_HAND, var14, this.offHandItem, var15, param1, param2, param4);
        }

        param2.endBatch();
    }

    private void renderArmWithItem(
        AbstractClientPlayer param0,
        float param1,
        float param2,
        InteractionHand param3,
        float param4,
        ItemStack param5,
        float param6,
        PoseStack param7,
        MultiBufferSource param8,
        int param9
    ) {
        if (!param0.isScoping()) {
            boolean var0 = param3 == InteractionHand.MAIN_HAND;
            HumanoidArm var1 = var0 ? param0.getMainArm() : param0.getMainArm().getOpposite();
            param7.pushPose();
            if (param5.isEmpty()) {
                if (var0 && !param0.isInvisible()) {
                    this.renderPlayerArm(param7, param8, param9, param6, param4, var1);
                }
            } else if (param5.is(Items.FILLED_MAP)) {
                if (var0 && this.offHandItem.isEmpty()) {
                    this.renderTwoHandedMap(param7, param8, param9, param2, param6, param4);
                } else {
                    this.renderOneHandedMap(param7, param8, param9, param6, var1, param4, param5);
                }
            } else if (param5.is(Items.CROSSBOW)) {
                boolean var2 = CrossbowItem.isCharged(param5);
                boolean var3 = var1 == HumanoidArm.RIGHT;
                int var4 = var3 ? 1 : -1;
                if (param0.isUsingItem() && param0.getUseItemRemainingTicks() > 0 && param0.getUsedItemHand() == param3) {
                    this.applyItemArmTransform(param7, var1, param6);
                    param7.translate((double)((float)var4 * -0.4785682F), -0.094387F, 0.05731531F);
                    param7.mulPose(Vector3f.XP.rotationDegrees(-11.935F));
                    param7.mulPose(Vector3f.YP.rotationDegrees((float)var4 * 65.3F));
                    param7.mulPose(Vector3f.ZP.rotationDegrees((float)var4 * -9.785F));
                    float var5 = (float)param5.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - param1 + 1.0F);
                    float var6 = var5 / (float)CrossbowItem.getChargeDuration(param5);
                    if (var6 > 1.0F) {
                        var6 = 1.0F;
                    }

                    if (var6 > 0.1F) {
                        float var7 = Mth.sin((var5 - 0.1F) * 1.3F);
                        float var8 = var6 - 0.1F;
                        float var9 = var7 * var8;
                        param7.translate((double)(var9 * 0.0F), (double)(var9 * 0.004F), (double)(var9 * 0.0F));
                    }

                    param7.translate((double)(var6 * 0.0F), (double)(var6 * 0.0F), (double)(var6 * 0.04F));
                    param7.scale(1.0F, 1.0F, 1.0F + var6 * 0.2F);
                    param7.mulPose(Vector3f.YN.rotationDegrees((float)var4 * 45.0F));
                } else {
                    float var10 = -0.4F * Mth.sin(Mth.sqrt(param4) * (float) Math.PI);
                    float var11 = 0.2F * Mth.sin(Mth.sqrt(param4) * (float) (Math.PI * 2));
                    float var12 = -0.2F * Mth.sin(param4 * (float) Math.PI);
                    param7.translate((double)((float)var4 * var10), (double)var11, (double)var12);
                    this.applyItemArmTransform(param7, var1, param6);
                    this.applyItemArmAttackTransform(param7, var1, param4);
                    if (var2 && param4 < 0.001F) {
                        param7.translate((double)((float)var4 * -0.641864F), 0.0, 0.0);
                        param7.mulPose(Vector3f.YP.rotationDegrees((float)var4 * 10.0F));
                    }
                }

                this.renderItem(
                    param0,
                    param5,
                    var3 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                    !var3,
                    param7,
                    param8,
                    param9
                );
            } else {
                boolean var13 = var1 == HumanoidArm.RIGHT;
                if (param0.isUsingItem() && param0.getUseItemRemainingTicks() > 0 && param0.getUsedItemHand() == param3) {
                    int var14 = var13 ? 1 : -1;
                    switch(param5.getUseAnimation()) {
                        case NONE:
                            this.applyItemArmTransform(param7, var1, param6);
                            break;
                        case EAT:
                        case DRINK:
                            this.applyEatTransform(param7, param1, var1, param5);
                            this.applyItemArmTransform(param7, var1, param6);
                            break;
                        case BLOCK:
                            this.applyItemArmTransform(param7, var1, param6);
                            break;
                        case BOW:
                            this.applyItemArmTransform(param7, var1, param6);
                            param7.translate((double)((float)var14 * -0.2785682F), 0.18344387F, 0.15731531F);
                            param7.mulPose(Vector3f.XP.rotationDegrees(-13.935F));
                            param7.mulPose(Vector3f.YP.rotationDegrees((float)var14 * 35.3F));
                            param7.mulPose(Vector3f.ZP.rotationDegrees((float)var14 * -9.785F));
                            float var15 = (float)param5.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - param1 + 1.0F);
                            float var16 = var15 / 20.0F;
                            var16 = (var16 * var16 + var16 * 2.0F) / 3.0F;
                            if (var16 > 1.0F) {
                                var16 = 1.0F;
                            }

                            if (var16 > 0.1F) {
                                float var17 = Mth.sin((var15 - 0.1F) * 1.3F);
                                float var18 = var16 - 0.1F;
                                float var19 = var17 * var18;
                                param7.translate((double)(var19 * 0.0F), (double)(var19 * 0.004F), (double)(var19 * 0.0F));
                            }

                            param7.translate((double)(var16 * 0.0F), (double)(var16 * 0.0F), (double)(var16 * 0.04F));
                            param7.scale(1.0F, 1.0F, 1.0F + var16 * 0.2F);
                            param7.mulPose(Vector3f.YN.rotationDegrees((float)var14 * 45.0F));
                            break;
                        case SPEAR:
                            this.applyItemArmTransform(param7, var1, param6);
                            param7.translate((double)((float)var14 * -0.5F), 0.7F, 0.1F);
                            param7.mulPose(Vector3f.XP.rotationDegrees(-55.0F));
                            param7.mulPose(Vector3f.YP.rotationDegrees((float)var14 * 35.3F));
                            param7.mulPose(Vector3f.ZP.rotationDegrees((float)var14 * -9.785F));
                            float var20 = (float)param5.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - param1 + 1.0F);
                            float var21 = var20 / 10.0F;
                            if (var21 > 1.0F) {
                                var21 = 1.0F;
                            }

                            if (var21 > 0.1F) {
                                float var22 = Mth.sin((var20 - 0.1F) * 1.3F);
                                float var23 = var21 - 0.1F;
                                float var24 = var22 * var23;
                                param7.translate((double)(var24 * 0.0F), (double)(var24 * 0.004F), (double)(var24 * 0.0F));
                            }

                            param7.translate(0.0, 0.0, (double)(var21 * 0.2F));
                            param7.scale(1.0F, 1.0F, 1.0F + var21 * 0.2F);
                            param7.mulPose(Vector3f.YN.rotationDegrees((float)var14 * 45.0F));
                    }
                } else if (param0.isAutoSpinAttack()) {
                    this.applyItemArmTransform(param7, var1, param6);
                    int var25 = var13 ? 1 : -1;
                    param7.translate((double)((float)var25 * -0.4F), 0.8F, 0.3F);
                    param7.mulPose(Vector3f.YP.rotationDegrees((float)var25 * 65.0F));
                    param7.mulPose(Vector3f.ZP.rotationDegrees((float)var25 * -85.0F));
                } else {
                    float var26 = -0.4F * Mth.sin(Mth.sqrt(param4) * (float) Math.PI);
                    float var27 = 0.2F * Mth.sin(Mth.sqrt(param4) * (float) (Math.PI * 2));
                    float var28 = -0.2F * Mth.sin(param4 * (float) Math.PI);
                    int var29 = var13 ? 1 : -1;
                    param7.translate((double)((float)var29 * var26), (double)var27, (double)var28);
                    this.applyItemArmTransform(param7, var1, param6);
                    this.applyItemArmAttackTransform(param7, var1, param4);
                }

                this.renderItem(
                    param0,
                    param5,
                    var13 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                    !var13,
                    param7,
                    param8,
                    param9
                );
            }

            param7.popPose();
        }
    }

    public void tick() {
        this.oMainHandHeight = this.mainHandHeight;
        this.oOffHandHeight = this.offHandHeight;
        LocalPlayer var0 = this.minecraft.player;
        ItemStack var1 = var0.getMainHandItem();
        ItemStack var2 = var0.getOffhandItem();
        if (ItemStack.matches(this.mainHandItem, var1)) {
            this.mainHandItem = var1;
        }

        if (ItemStack.matches(this.offHandItem, var2)) {
            this.offHandItem = var2;
        }

        if (var0.isHandsBusy()) {
            this.mainHandHeight = Mth.clamp(this.mainHandHeight - 0.4F, 0.0F, 1.0F);
            this.offHandHeight = Mth.clamp(this.offHandHeight - 0.4F, 0.0F, 1.0F);
        } else {
            float var3 = var0.getAttackStrengthScale(1.0F);
            this.mainHandHeight += Mth.clamp((this.mainHandItem == var1 ? var3 * var3 * var3 : 0.0F) - this.mainHandHeight, -0.4F, 0.4F);
            this.offHandHeight += Mth.clamp((float)(this.offHandItem == var2 ? 1 : 0) - this.offHandHeight, -0.4F, 0.4F);
        }

        if (this.mainHandHeight < 0.1F) {
            this.mainHandItem = var1;
        }

        if (this.offHandHeight < 0.1F) {
            this.offHandItem = var2;
        }

    }

    public void itemUsed(InteractionHand param0) {
        if (param0 == InteractionHand.MAIN_HAND) {
            this.mainHandHeight = 0.0F;
        } else {
            this.offHandHeight = 0.0F;
        }

    }
}
