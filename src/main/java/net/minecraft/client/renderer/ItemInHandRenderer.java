package net.minecraft.client.renderer;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
        LivingEntity param0, ItemStack param1, ItemTransforms.TransformType param2, boolean param3, PoseStack param4, MultiBufferSource param5
    ) {
        if (!param1.isEmpty()) {
            this.itemRenderer.renderStatic(param0, param1, param2, param3, param4, param5, param0.level, param0.getLightColor(), OverlayTexture.NO_OVERLAY);
        }
    }

    private float calculateMapTilt(float param0) {
        float var0 = 1.0F - param0 / 45.0F + 0.1F;
        var0 = Mth.clamp(var0, 0.0F, 1.0F);
        return -Mth.cos(var0 * (float) Math.PI) * 0.5F + 0.5F;
    }

    private void renderMapHand(PoseStack param0, MultiBufferSource param1, HumanoidArm param2) {
        this.minecraft.getTextureManager().bind(this.minecraft.player.getSkinTextureLocation());
        PlayerRenderer var0 = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayer>getRenderer(this.minecraft.player);
        param0.pushPose();
        float var1 = param2 == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        param0.mulPose(Vector3f.YP.rotationDegrees(92.0F));
        param0.mulPose(Vector3f.XP.rotationDegrees(45.0F));
        param0.mulPose(Vector3f.ZP.rotationDegrees(var1 * -41.0F));
        param0.translate((double)(var1 * 0.3F), -1.1F, 0.45F);
        if (param2 == HumanoidArm.RIGHT) {
            var0.renderRightHand(param0, param1, this.minecraft.player);
        } else {
            var0.renderLeftHand(param0, param1, this.minecraft.player);
        }

        param0.popPose();
    }

    private void renderOneHandedMap(PoseStack param0, MultiBufferSource param1, int param2, float param3, HumanoidArm param4, float param5, ItemStack param6) {
        float var0 = param4 == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        param0.translate((double)(var0 * 0.125F), -0.125, 0.0);
        if (!this.minecraft.player.isInvisible()) {
            param0.pushPose();
            param0.mulPose(Vector3f.ZP.rotationDegrees(var0 * 10.0F));
            this.renderPlayerArm(param0, param1, param3, param5, param4);
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
            this.renderMapHand(param0, param1, HumanoidArm.RIGHT);
            this.renderMapHand(param0, param1, HumanoidArm.LEFT);
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
        VertexConsumer var0 = param1.getBuffer(RenderType.text(MapRenderer.MAP_BACKGROUND_LOCATION));
        Matrix4f var1 = param0.getPose();
        var0.vertex(var1, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(param2).endVertex();
        var0.vertex(var1, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(param2).endVertex();
        var0.vertex(var1, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(param2).endVertex();
        var0.vertex(var1, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(param2).endVertex();
        MapItemSavedData var2 = MapItem.getOrCreateSavedData(param3, this.minecraft.level);
        if (var2 != null) {
            this.minecraft.gameRenderer.getMapRenderer().render(param0, param1, var2, false, param2);
        }

    }

    private void renderPlayerArm(PoseStack param0, MultiBufferSource param1, float param2, float param3, HumanoidArm param4) {
        boolean var0 = param4 != HumanoidArm.LEFT;
        float var1 = var0 ? 1.0F : -1.0F;
        float var2 = Mth.sqrt(param3);
        float var3 = -0.3F * Mth.sin(var2 * (float) Math.PI);
        float var4 = 0.4F * Mth.sin(var2 * (float) (Math.PI * 2));
        float var5 = -0.4F * Mth.sin(param3 * (float) Math.PI);
        param0.translate((double)(var1 * (var3 + 0.64000005F)), (double)(var4 + -0.6F + param2 * -0.6F), (double)(var5 + -0.71999997F));
        param0.mulPose(Vector3f.YP.rotationDegrees(var1 * 45.0F));
        float var6 = Mth.sin(param3 * param3 * (float) Math.PI);
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
            var9.renderRightHand(param0, param1, var8);
        } else {
            var9.renderLeftHand(param0, param1, var8);
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

    public void renderHandsWithItems(float param0, PoseStack param1, MultiBufferSource.BufferSource param2) {
        LocalPlayer var0 = this.minecraft.player;
        float var1 = var0.getAttackAnim(param0);
        InteractionHand var2 = MoreObjects.firstNonNull(var0.swingingArm, InteractionHand.MAIN_HAND);
        float var3 = Mth.lerp(param0, var0.xRotO, var0.xRot);
        boolean var4 = true;
        boolean var5 = true;
        if (var0.isUsingItem()) {
            ItemStack var6 = var0.getUseItem();
            if (var6.getItem() == Items.BOW || var6.getItem() == Items.CROSSBOW) {
                var4 = var0.getUsedItemHand() == InteractionHand.MAIN_HAND;
                var5 = !var4;
            }

            InteractionHand var7 = var0.getUsedItemHand();
            if (var7 == InteractionHand.MAIN_HAND) {
                ItemStack var8 = var0.getOffhandItem();
                if (var8.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(var8)) {
                    var5 = false;
                }
            }
        } else {
            ItemStack var9 = var0.getMainHandItem();
            ItemStack var10 = var0.getOffhandItem();
            if (var9.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(var9)) {
                var5 = !var4;
            }

            if (var10.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(var10)) {
                var4 = !var9.isEmpty();
                var5 = !var4;
            }
        }

        float var11 = Mth.lerp(param0, var0.xBobO, var0.xBob);
        float var12 = Mth.lerp(param0, var0.yBobO, var0.yBob);
        param1.mulPose(Vector3f.XP.rotationDegrees((var0.getViewXRot(param0) - var11) * 0.1F));
        param1.mulPose(Vector3f.YP.rotationDegrees((var0.getViewYRot(param0) - var12) * 0.1F));
        if (var4) {
            float var13 = var2 == InteractionHand.MAIN_HAND ? var1 : 0.0F;
            float var14 = 1.0F - Mth.lerp(param0, this.oMainHandHeight, this.mainHandHeight);
            this.renderArmWithItem(var0, param0, var3, InteractionHand.MAIN_HAND, var13, this.mainHandItem, var14, param1, param2);
        }

        if (var5) {
            float var15 = var2 == InteractionHand.OFF_HAND ? var1 : 0.0F;
            float var16 = 1.0F - Mth.lerp(param0, this.oOffHandHeight, this.offHandHeight);
            this.renderArmWithItem(var0, param0, var3, InteractionHand.OFF_HAND, var15, this.offHandItem, var16, param1, param2);
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
        MultiBufferSource param8
    ) {
        boolean var0 = param3 == InteractionHand.MAIN_HAND;
        HumanoidArm var1 = var0 ? param0.getMainArm() : param0.getMainArm().getOpposite();
        param7.pushPose();
        if (param5.isEmpty()) {
            if (var0 && !param0.isInvisible()) {
                this.renderPlayerArm(param7, param8, param6, param4, var1);
            }
        } else if (param5.getItem() == Items.FILLED_MAP) {
            int var2 = param0.getLightColor();
            if (var0 && this.offHandItem.isEmpty()) {
                this.renderTwoHandedMap(param7, param8, var2, param2, param6, param4);
            } else {
                this.renderOneHandedMap(param7, param8, var2, param6, var1, param4, param5);
            }
        } else if (param5.getItem() == Items.CROSSBOW) {
            boolean var3 = CrossbowItem.isCharged(param5);
            boolean var4 = var1 == HumanoidArm.RIGHT;
            int var5 = var4 ? 1 : -1;
            if (param0.isUsingItem() && param0.getUseItemRemainingTicks() > 0 && param0.getUsedItemHand() == param3) {
                this.applyItemArmTransform(param7, var1, param6);
                param7.translate((double)((float)var5 * -0.4785682F), -0.094387F, 0.05731531F);
                param7.mulPose(Vector3f.XP.rotationDegrees(-11.935F));
                param7.mulPose(Vector3f.YP.rotationDegrees((float)var5 * 65.3F));
                param7.mulPose(Vector3f.ZP.rotationDegrees((float)var5 * -9.785F));
                float var6 = (float)param5.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - param1 + 1.0F);
                float var7 = var6 / (float)CrossbowItem.getChargeDuration(param5);
                if (var7 > 1.0F) {
                    var7 = 1.0F;
                }

                if (var7 > 0.1F) {
                    float var8 = Mth.sin((var6 - 0.1F) * 1.3F);
                    float var9 = var7 - 0.1F;
                    float var10 = var8 * var9;
                    param7.translate((double)(var10 * 0.0F), (double)(var10 * 0.004F), (double)(var10 * 0.0F));
                }

                param7.translate((double)(var7 * 0.0F), (double)(var7 * 0.0F), (double)(var7 * 0.04F));
                param7.scale(1.0F, 1.0F, 1.0F + var7 * 0.2F);
                param7.mulPose(Vector3f.YN.rotationDegrees((float)var5 * 45.0F));
            } else {
                float var11 = -0.4F * Mth.sin(Mth.sqrt(param4) * (float) Math.PI);
                float var12 = 0.2F * Mth.sin(Mth.sqrt(param4) * (float) (Math.PI * 2));
                float var13 = -0.2F * Mth.sin(param4 * (float) Math.PI);
                param7.translate((double)((float)var5 * var11), (double)var12, (double)var13);
                this.applyItemArmTransform(param7, var1, param6);
                this.applyItemArmAttackTransform(param7, var1, param4);
                if (var3 && param4 < 0.001F) {
                    param7.translate((double)((float)var5 * -0.641864F), 0.0, 0.0);
                    param7.mulPose(Vector3f.YP.rotationDegrees((float)var5 * 10.0F));
                }
            }

            this.renderItem(
                param0,
                param5,
                var4 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                !var4,
                param7,
                param8
            );
        } else {
            boolean var14 = var1 == HumanoidArm.RIGHT;
            if (param0.isUsingItem() && param0.getUseItemRemainingTicks() > 0 && param0.getUsedItemHand() == param3) {
                int var15 = var14 ? 1 : -1;
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
                        param7.translate((double)((float)var15 * -0.2785682F), 0.18344387F, 0.15731531F);
                        param7.mulPose(Vector3f.XP.rotationDegrees(-13.935F));
                        param7.mulPose(Vector3f.YP.rotationDegrees((float)var15 * 35.3F));
                        param7.mulPose(Vector3f.ZP.rotationDegrees((float)var15 * -9.785F));
                        float var16 = (float)param5.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - param1 + 1.0F);
                        float var17 = var16 / 20.0F;
                        var17 = (var17 * var17 + var17 * 2.0F) / 3.0F;
                        if (var17 > 1.0F) {
                            var17 = 1.0F;
                        }

                        if (var17 > 0.1F) {
                            float var18 = Mth.sin((var16 - 0.1F) * 1.3F);
                            float var19 = var17 - 0.1F;
                            float var20 = var18 * var19;
                            param7.translate((double)(var20 * 0.0F), (double)(var20 * 0.004F), (double)(var20 * 0.0F));
                        }

                        param7.translate((double)(var17 * 0.0F), (double)(var17 * 0.0F), (double)(var17 * 0.04F));
                        param7.scale(1.0F, 1.0F, 1.0F + var17 * 0.2F);
                        param7.mulPose(Vector3f.YN.rotationDegrees((float)var15 * 45.0F));
                        break;
                    case SPEAR:
                        this.applyItemArmTransform(param7, var1, param6);
                        param7.translate((double)((float)var15 * -0.5F), 0.7F, 0.1F);
                        param7.mulPose(Vector3f.XP.rotationDegrees(-55.0F));
                        param7.mulPose(Vector3f.YP.rotationDegrees((float)var15 * 35.3F));
                        param7.mulPose(Vector3f.ZP.rotationDegrees((float)var15 * -9.785F));
                        float var21 = (float)param5.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - param1 + 1.0F);
                        float var22 = var21 / 10.0F;
                        if (var22 > 1.0F) {
                            var22 = 1.0F;
                        }

                        if (var22 > 0.1F) {
                            float var23 = Mth.sin((var21 - 0.1F) * 1.3F);
                            float var24 = var22 - 0.1F;
                            float var25 = var23 * var24;
                            param7.translate((double)(var25 * 0.0F), (double)(var25 * 0.004F), (double)(var25 * 0.0F));
                        }

                        param7.translate(0.0, 0.0, (double)(var22 * 0.2F));
                        param7.scale(1.0F, 1.0F, 1.0F + var22 * 0.2F);
                        param7.mulPose(Vector3f.YN.rotationDegrees((float)var15 * 45.0F));
                }
            } else if (param0.isAutoSpinAttack()) {
                this.applyItemArmTransform(param7, var1, param6);
                int var26 = var14 ? 1 : -1;
                param7.translate((double)((float)var26 * -0.4F), 0.8F, 0.3F);
                param7.mulPose(Vector3f.YP.rotationDegrees((float)var26 * 65.0F));
                param7.mulPose(Vector3f.ZP.rotationDegrees((float)var26 * -85.0F));
            } else {
                float var27 = -0.4F * Mth.sin(Mth.sqrt(param4) * (float) Math.PI);
                float var28 = 0.2F * Mth.sin(Mth.sqrt(param4) * (float) (Math.PI * 2));
                float var29 = -0.2F * Mth.sin(param4 * (float) Math.PI);
                int var30 = var14 ? 1 : -1;
                param7.translate((double)((float)var30 * var27), (double)var28, (double)var29);
                this.applyItemArmTransform(param7, var1, param6);
                this.applyItemArmAttackTransform(param7, var1, param4);
            }

            this.renderItem(
                param0,
                param5,
                var14 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                !var14,
                param7,
                param8
            );
        }

        param7.popPose();
    }

    public void tick() {
        this.oMainHandHeight = this.mainHandHeight;
        this.oOffHandHeight = this.offHandHeight;
        LocalPlayer var0 = this.minecraft.player;
        ItemStack var1 = var0.getMainHandItem();
        ItemStack var2 = var0.getOffhandItem();
        if (var0.isHandsBusy()) {
            this.mainHandHeight = Mth.clamp(this.mainHandHeight - 0.4F, 0.0F, 1.0F);
            this.offHandHeight = Mth.clamp(this.offHandHeight - 0.4F, 0.0F, 1.0F);
        } else {
            float var3 = var0.getAttackStrengthScale(1.0F);
            this.mainHandHeight += Mth.clamp((Objects.equals(this.mainHandItem, var1) ? var3 * var3 * var3 : 0.0F) - this.mainHandHeight, -0.4F, 0.4F);
            this.offHandHeight += Mth.clamp((float)(Objects.equals(this.offHandItem, var2) ? 1 : 0) - this.offHandHeight, -0.4F, 0.4F);
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
