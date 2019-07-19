package net.minecraft.client.renderer;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemInHandRenderer {
    private static final ResourceLocation MAP_BACKGROUND_LOCATION = new ResourceLocation("textures/map/map_background.png");
    private static final ResourceLocation UNDERWATER_LOCATION = new ResourceLocation("textures/misc/underwater.png");
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

    public void renderItem(LivingEntity param0, ItemStack param1, ItemTransforms.TransformType param2) {
        this.renderItem(param0, param1, param2, false);
    }

    public void renderItem(LivingEntity param0, ItemStack param1, ItemTransforms.TransformType param2, boolean param3) {
        if (!param1.isEmpty()) {
            Item var0 = param1.getItem();
            Block var1 = Block.byItem(var0);
            GlStateManager.pushMatrix();
            boolean var2 = this.itemRenderer.isGui3d(param1) && var1.getRenderLayer() == BlockLayer.TRANSLUCENT;
            if (var2) {
                GlStateManager.depthMask(false);
            }

            this.itemRenderer.renderWithMobState(param1, param0, param2, param3);
            if (var2) {
                GlStateManager.depthMask(true);
            }

            GlStateManager.popMatrix();
        }
    }

    private void enableLight(float param0, float param1) {
        GlStateManager.pushMatrix();
        GlStateManager.rotatef(param0, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef(param1, 0.0F, 1.0F, 0.0F);
        Lighting.turnOn();
        GlStateManager.popMatrix();
    }

    private void setLightValue() {
        AbstractClientPlayer var0 = this.minecraft.player;
        int var1 = this.minecraft.level.getLightColor(new BlockPos(var0.x, var0.y + (double)var0.getEyeHeight(), var0.z), 0);
        float var2 = (float)(var1 & 65535);
        float var3 = (float)(var1 >> 16);
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, var2, var3);
    }

    private void setPlayerBob(float param0) {
        LocalPlayer var0 = this.minecraft.player;
        float var1 = Mth.lerp(param0, var0.xBobO, var0.xBob);
        float var2 = Mth.lerp(param0, var0.yBobO, var0.yBob);
        GlStateManager.rotatef((var0.getViewXRot(param0) - var1) * 0.1F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef((var0.getViewYRot(param0) - var2) * 0.1F, 0.0F, 1.0F, 0.0F);
    }

    private float calculateMapTilt(float param0) {
        float var0 = 1.0F - param0 / 45.0F + 0.1F;
        var0 = Mth.clamp(var0, 0.0F, 1.0F);
        return -Mth.cos(var0 * (float) Math.PI) * 0.5F + 0.5F;
    }

    private void renderMapHands() {
        if (!this.minecraft.player.isInvisible()) {
            GlStateManager.disableCull();
            GlStateManager.pushMatrix();
            GlStateManager.rotatef(90.0F, 0.0F, 1.0F, 0.0F);
            this.renderMapHand(HumanoidArm.RIGHT);
            this.renderMapHand(HumanoidArm.LEFT);
            GlStateManager.popMatrix();
            GlStateManager.enableCull();
        }
    }

    private void renderMapHand(HumanoidArm param0) {
        this.minecraft.getTextureManager().bind(this.minecraft.player.getSkinTextureLocation());
        EntityRenderer<AbstractClientPlayer> var0 = this.entityRenderDispatcher.getRenderer(this.minecraft.player);
        PlayerRenderer var1 = (PlayerRenderer)var0;
        GlStateManager.pushMatrix();
        float var2 = param0 == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        GlStateManager.rotatef(92.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(45.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef(var2 * -41.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translatef(var2 * 0.3F, -1.1F, 0.45F);
        if (param0 == HumanoidArm.RIGHT) {
            var1.renderRightHand(this.minecraft.player);
        } else {
            var1.renderLeftHand(this.minecraft.player);
        }

        GlStateManager.popMatrix();
    }

    private void renderOneHandedMap(float param0, HumanoidArm param1, float param2, ItemStack param3) {
        float var0 = param1 == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        GlStateManager.translatef(var0 * 0.125F, -0.125F, 0.0F);
        if (!this.minecraft.player.isInvisible()) {
            GlStateManager.pushMatrix();
            GlStateManager.rotatef(var0 * 10.0F, 0.0F, 0.0F, 1.0F);
            this.renderPlayerArm(param0, param2, param1);
            GlStateManager.popMatrix();
        }

        GlStateManager.pushMatrix();
        GlStateManager.translatef(var0 * 0.51F, -0.08F + param0 * -1.2F, -0.75F);
        float var1 = Mth.sqrt(param2);
        float var2 = Mth.sin(var1 * (float) Math.PI);
        float var3 = -0.5F * var2;
        float var4 = 0.4F * Mth.sin(var1 * (float) (Math.PI * 2));
        float var5 = -0.3F * Mth.sin(param2 * (float) Math.PI);
        GlStateManager.translatef(var0 * var3, var4 - 0.3F * var2, var5);
        GlStateManager.rotatef(var2 * -45.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef(var0 * var2 * -30.0F, 0.0F, 1.0F, 0.0F);
        this.renderMap(param3);
        GlStateManager.popMatrix();
    }

    private void renderTwoHandedMap(float param0, float param1, float param2) {
        float var0 = Mth.sqrt(param2);
        float var1 = -0.2F * Mth.sin(param2 * (float) Math.PI);
        float var2 = -0.4F * Mth.sin(var0 * (float) Math.PI);
        GlStateManager.translatef(0.0F, -var1 / 2.0F, var2);
        float var3 = this.calculateMapTilt(param0);
        GlStateManager.translatef(0.0F, 0.04F + param1 * -1.2F + var3 * -0.5F, -0.72F);
        GlStateManager.rotatef(var3 * -85.0F, 1.0F, 0.0F, 0.0F);
        this.renderMapHands();
        float var4 = Mth.sin(var0 * (float) Math.PI);
        GlStateManager.rotatef(var4 * 20.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scalef(2.0F, 2.0F, 2.0F);
        this.renderMap(this.mainHandItem);
    }

    private void renderMap(ItemStack param0) {
        GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.scalef(0.38F, 0.38F, 0.38F);
        GlStateManager.disableLighting();
        this.minecraft.getTextureManager().bind(MAP_BACKGROUND_LOCATION);
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        GlStateManager.translatef(-0.5F, -0.5F, 0.0F);
        GlStateManager.scalef(0.0078125F, 0.0078125F, 0.0078125F);
        var1.begin(7, DefaultVertexFormat.POSITION_TEX);
        var1.vertex(-7.0, 135.0, 0.0).uv(0.0, 1.0).endVertex();
        var1.vertex(135.0, 135.0, 0.0).uv(1.0, 1.0).endVertex();
        var1.vertex(135.0, -7.0, 0.0).uv(1.0, 0.0).endVertex();
        var1.vertex(-7.0, -7.0, 0.0).uv(0.0, 0.0).endVertex();
        var0.end();
        MapItemSavedData var2 = MapItem.getOrCreateSavedData(param0, this.minecraft.level);
        if (var2 != null) {
            this.minecraft.gameRenderer.getMapRenderer().render(var2, false);
        }

        GlStateManager.enableLighting();
    }

    private void renderPlayerArm(float param0, float param1, HumanoidArm param2) {
        boolean var0 = param2 != HumanoidArm.LEFT;
        float var1 = var0 ? 1.0F : -1.0F;
        float var2 = Mth.sqrt(param1);
        float var3 = -0.3F * Mth.sin(var2 * (float) Math.PI);
        float var4 = 0.4F * Mth.sin(var2 * (float) (Math.PI * 2));
        float var5 = -0.4F * Mth.sin(param1 * (float) Math.PI);
        GlStateManager.translatef(var1 * (var3 + 0.64000005F), var4 + -0.6F + param0 * -0.6F, var5 + -0.71999997F);
        GlStateManager.rotatef(var1 * 45.0F, 0.0F, 1.0F, 0.0F);
        float var6 = Mth.sin(param1 * param1 * (float) Math.PI);
        float var7 = Mth.sin(var2 * (float) Math.PI);
        GlStateManager.rotatef(var1 * var7 * 70.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(var1 * var6 * -20.0F, 0.0F, 0.0F, 1.0F);
        AbstractClientPlayer var8 = this.minecraft.player;
        this.minecraft.getTextureManager().bind(var8.getSkinTextureLocation());
        GlStateManager.translatef(var1 * -1.0F, 3.6F, 3.5F);
        GlStateManager.rotatef(var1 * 120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotatef(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef(var1 * -135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translatef(var1 * 5.6F, 0.0F, 0.0F);
        PlayerRenderer var9 = this.entityRenderDispatcher.getRenderer(var8);
        GlStateManager.disableCull();
        if (var0) {
            var9.renderRightHand(var8);
        } else {
            var9.renderLeftHand(var8);
        }

        GlStateManager.enableCull();
    }

    private void applyEatTransform(float param0, HumanoidArm param1, ItemStack param2) {
        float var0 = (float)this.minecraft.player.getUseItemRemainingTicks() - param0 + 1.0F;
        float var1 = var0 / (float)param2.getUseDuration();
        if (var1 < 0.8F) {
            float var2 = Mth.abs(Mth.cos(var0 / 4.0F * (float) Math.PI) * 0.1F);
            GlStateManager.translatef(0.0F, var2, 0.0F);
        }

        float var3 = 1.0F - (float)Math.pow((double)var1, 27.0);
        int var4 = param1 == HumanoidArm.RIGHT ? 1 : -1;
        GlStateManager.translatef(var3 * 0.6F * (float)var4, var3 * -0.5F, var3 * 0.0F);
        GlStateManager.rotatef((float)var4 * var3 * 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(var3 * 10.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef((float)var4 * var3 * 30.0F, 0.0F, 0.0F, 1.0F);
    }

    private void applyItemArmAttackTransform(HumanoidArm param0, float param1) {
        int var0 = param0 == HumanoidArm.RIGHT ? 1 : -1;
        float var1 = Mth.sin(param1 * param1 * (float) Math.PI);
        GlStateManager.rotatef((float)var0 * (45.0F + var1 * -20.0F), 0.0F, 1.0F, 0.0F);
        float var2 = Mth.sin(Mth.sqrt(param1) * (float) Math.PI);
        GlStateManager.rotatef((float)var0 * var2 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotatef(var2 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef((float)var0 * -45.0F, 0.0F, 1.0F, 0.0F);
    }

    private void applyItemArmTransform(HumanoidArm param0, float param1) {
        int var0 = param0 == HumanoidArm.RIGHT ? 1 : -1;
        GlStateManager.translatef((float)var0 * 0.56F, -0.52F + param1 * -0.6F, -0.72F);
    }

    public void render(float param0) {
        AbstractClientPlayer var0 = this.minecraft.player;
        float var1 = var0.getAttackAnim(param0);
        InteractionHand var2 = MoreObjects.firstNonNull(var0.swingingArm, InteractionHand.MAIN_HAND);
        float var3 = Mth.lerp(param0, var0.xRotO, var0.xRot);
        float var4 = Mth.lerp(param0, var0.yRotO, var0.yRot);
        boolean var5 = true;
        boolean var6 = true;
        if (var0.isUsingItem()) {
            ItemStack var7 = var0.getUseItem();
            if (var7.getItem() == Items.BOW || var7.getItem() == Items.CROSSBOW) {
                var5 = var0.getUsedItemHand() == InteractionHand.MAIN_HAND;
                var6 = !var5;
            }

            InteractionHand var8 = var0.getUsedItemHand();
            if (var8 == InteractionHand.MAIN_HAND) {
                ItemStack var9 = var0.getOffhandItem();
                if (var9.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(var9)) {
                    var6 = false;
                }
            }
        } else {
            ItemStack var10 = var0.getMainHandItem();
            ItemStack var11 = var0.getOffhandItem();
            if (var10.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(var10)) {
                var6 = !var5;
            }

            if (var11.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(var11)) {
                var5 = !var10.isEmpty();
                var6 = !var5;
            }
        }

        this.enableLight(var3, var4);
        this.setLightValue();
        this.setPlayerBob(param0);
        GlStateManager.enableRescaleNormal();
        if (var5) {
            float var12 = var2 == InteractionHand.MAIN_HAND ? var1 : 0.0F;
            float var13 = 1.0F - Mth.lerp(param0, this.oMainHandHeight, this.mainHandHeight);
            this.renderArmWithItem(var0, param0, var3, InteractionHand.MAIN_HAND, var12, this.mainHandItem, var13);
        }

        if (var6) {
            float var14 = var2 == InteractionHand.OFF_HAND ? var1 : 0.0F;
            float var15 = 1.0F - Mth.lerp(param0, this.oOffHandHeight, this.offHandHeight);
            this.renderArmWithItem(var0, param0, var3, InteractionHand.OFF_HAND, var14, this.offHandItem, var15);
        }

        GlStateManager.disableRescaleNormal();
        Lighting.turnOff();
    }

    public void renderArmWithItem(AbstractClientPlayer param0, float param1, float param2, InteractionHand param3, float param4, ItemStack param5, float param6) {
        boolean var0 = param3 == InteractionHand.MAIN_HAND;
        HumanoidArm var1 = var0 ? param0.getMainArm() : param0.getMainArm().getOpposite();
        GlStateManager.pushMatrix();
        if (param5.isEmpty()) {
            if (var0 && !param0.isInvisible()) {
                this.renderPlayerArm(param6, param4, var1);
            }
        } else if (param5.getItem() == Items.FILLED_MAP) {
            if (var0 && this.offHandItem.isEmpty()) {
                this.renderTwoHandedMap(param2, param6, param4);
            } else {
                this.renderOneHandedMap(param6, var1, param4, param5);
            }
        } else if (param5.getItem() == Items.CROSSBOW) {
            boolean var2 = CrossbowItem.isCharged(param5);
            boolean var3 = var1 == HumanoidArm.RIGHT;
            int var4 = var3 ? 1 : -1;
            if (param0.isUsingItem() && param0.getUseItemRemainingTicks() > 0 && param0.getUsedItemHand() == param3) {
                this.applyItemArmTransform(var1, param6);
                GlStateManager.translatef((float)var4 * -0.4785682F, -0.094387F, 0.05731531F);
                GlStateManager.rotatef(-11.935F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotatef((float)var4 * 65.3F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotatef((float)var4 * -9.785F, 0.0F, 0.0F, 1.0F);
                float var5 = (float)param5.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - param1 + 1.0F);
                float var6 = var5 / (float)CrossbowItem.getChargeDuration(param5);
                if (var6 > 1.0F) {
                    var6 = 1.0F;
                }

                if (var6 > 0.1F) {
                    float var7 = Mth.sin((var5 - 0.1F) * 1.3F);
                    float var8 = var6 - 0.1F;
                    float var9 = var7 * var8;
                    GlStateManager.translatef(var9 * 0.0F, var9 * 0.004F, var9 * 0.0F);
                }

                GlStateManager.translatef(var6 * 0.0F, var6 * 0.0F, var6 * 0.04F);
                GlStateManager.scalef(1.0F, 1.0F, 1.0F + var6 * 0.2F);
                GlStateManager.rotatef((float)var4 * 45.0F, 0.0F, -1.0F, 0.0F);
            } else {
                float var10 = -0.4F * Mth.sin(Mth.sqrt(param4) * (float) Math.PI);
                float var11 = 0.2F * Mth.sin(Mth.sqrt(param4) * (float) (Math.PI * 2));
                float var12 = -0.2F * Mth.sin(param4 * (float) Math.PI);
                GlStateManager.translatef((float)var4 * var10, var11, var12);
                this.applyItemArmTransform(var1, param6);
                this.applyItemArmAttackTransform(var1, param4);
                if (var2 && param4 < 0.001F) {
                    GlStateManager.translatef((float)var4 * -0.641864F, 0.0F, 0.0F);
                    GlStateManager.rotatef((float)var4 * 10.0F, 0.0F, 1.0F, 0.0F);
                }
            }

            this.renderItem(
                param0, param5, var3 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !var3
            );
        } else {
            boolean var13 = var1 == HumanoidArm.RIGHT;
            if (param0.isUsingItem() && param0.getUseItemRemainingTicks() > 0 && param0.getUsedItemHand() == param3) {
                int var14 = var13 ? 1 : -1;
                switch(param5.getUseAnimation()) {
                    case NONE:
                        this.applyItemArmTransform(var1, param6);
                        break;
                    case EAT:
                    case DRINK:
                        this.applyEatTransform(param1, var1, param5);
                        this.applyItemArmTransform(var1, param6);
                        break;
                    case BLOCK:
                        this.applyItemArmTransform(var1, param6);
                        break;
                    case BOW:
                        this.applyItemArmTransform(var1, param6);
                        GlStateManager.translatef((float)var14 * -0.2785682F, 0.18344387F, 0.15731531F);
                        GlStateManager.rotatef(-13.935F, 1.0F, 0.0F, 0.0F);
                        GlStateManager.rotatef((float)var14 * 35.3F, 0.0F, 1.0F, 0.0F);
                        GlStateManager.rotatef((float)var14 * -9.785F, 0.0F, 0.0F, 1.0F);
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
                            GlStateManager.translatef(var19 * 0.0F, var19 * 0.004F, var19 * 0.0F);
                        }

                        GlStateManager.translatef(var16 * 0.0F, var16 * 0.0F, var16 * 0.04F);
                        GlStateManager.scalef(1.0F, 1.0F, 1.0F + var16 * 0.2F);
                        GlStateManager.rotatef((float)var14 * 45.0F, 0.0F, -1.0F, 0.0F);
                        break;
                    case SPEAR:
                        this.applyItemArmTransform(var1, param6);
                        GlStateManager.translatef((float)var14 * -0.5F, 0.7F, 0.1F);
                        GlStateManager.rotatef(-55.0F, 1.0F, 0.0F, 0.0F);
                        GlStateManager.rotatef((float)var14 * 35.3F, 0.0F, 1.0F, 0.0F);
                        GlStateManager.rotatef((float)var14 * -9.785F, 0.0F, 0.0F, 1.0F);
                        float var20 = (float)param5.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - param1 + 1.0F);
                        float var21 = var20 / 10.0F;
                        if (var21 > 1.0F) {
                            var21 = 1.0F;
                        }

                        if (var21 > 0.1F) {
                            float var22 = Mth.sin((var20 - 0.1F) * 1.3F);
                            float var23 = var21 - 0.1F;
                            float var24 = var22 * var23;
                            GlStateManager.translatef(var24 * 0.0F, var24 * 0.004F, var24 * 0.0F);
                        }

                        GlStateManager.translatef(0.0F, 0.0F, var21 * 0.2F);
                        GlStateManager.scalef(1.0F, 1.0F, 1.0F + var21 * 0.2F);
                        GlStateManager.rotatef((float)var14 * 45.0F, 0.0F, -1.0F, 0.0F);
                }
            } else if (param0.isAutoSpinAttack()) {
                this.applyItemArmTransform(var1, param6);
                int var25 = var13 ? 1 : -1;
                GlStateManager.translatef((float)var25 * -0.4F, 0.8F, 0.3F);
                GlStateManager.rotatef((float)var25 * 65.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotatef((float)var25 * -85.0F, 0.0F, 0.0F, 1.0F);
            } else {
                float var26 = -0.4F * Mth.sin(Mth.sqrt(param4) * (float) Math.PI);
                float var27 = 0.2F * Mth.sin(Mth.sqrt(param4) * (float) (Math.PI * 2));
                float var28 = -0.2F * Mth.sin(param4 * (float) Math.PI);
                int var29 = var13 ? 1 : -1;
                GlStateManager.translatef((float)var29 * var26, var27, var28);
                this.applyItemArmTransform(var1, param6);
                this.applyItemArmAttackTransform(var1, param4);
            }

            this.renderItem(
                param0, param5, var13 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !var13
            );
        }

        GlStateManager.popMatrix();
    }

    public void renderScreenEffect(float param0) {
        GlStateManager.disableAlphaTest();
        if (this.minecraft.player.isInWall()) {
            BlockState var0 = this.minecraft.level.getBlockState(new BlockPos(this.minecraft.player));
            Player var1 = this.minecraft.player;

            for(int var2 = 0; var2 < 8; ++var2) {
                double var3 = var1.x + (double)(((float)((var2 >> 0) % 2) - 0.5F) * var1.getBbWidth() * 0.8F);
                double var4 = var1.y + (double)(((float)((var2 >> 1) % 2) - 0.5F) * 0.1F);
                double var5 = var1.z + (double)(((float)((var2 >> 2) % 2) - 0.5F) * var1.getBbWidth() * 0.8F);
                BlockPos var6 = new BlockPos(var3, var4 + (double)var1.getEyeHeight(), var5);
                BlockState var7 = this.minecraft.level.getBlockState(var6);
                if (var7.isViewBlocking(this.minecraft.level, var6)) {
                    var0 = var7;
                }
            }

            if (var0.getRenderShape() != RenderShape.INVISIBLE) {
                this.renderTex(this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(var0));
            }
        }

        if (!this.minecraft.player.isSpectator()) {
            if (this.minecraft.player.isUnderLiquid(FluidTags.WATER)) {
                this.renderWater(param0);
            }

            if (this.minecraft.player.isOnFire()) {
                this.renderFire();
            }
        }

        GlStateManager.enableAlphaTest();
    }

    private void renderTex(TextureAtlasSprite param0) {
        this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        float var2 = 0.1F;
        GlStateManager.color4f(0.1F, 0.1F, 0.1F, 0.5F);
        GlStateManager.pushMatrix();
        float var3 = -1.0F;
        float var4 = 1.0F;
        float var5 = -1.0F;
        float var6 = 1.0F;
        float var7 = -0.5F;
        float var8 = param0.getU0();
        float var9 = param0.getU1();
        float var10 = param0.getV0();
        float var11 = param0.getV1();
        var1.begin(7, DefaultVertexFormat.POSITION_TEX);
        var1.vertex(-1.0, -1.0, -0.5).uv((double)var9, (double)var11).endVertex();
        var1.vertex(1.0, -1.0, -0.5).uv((double)var8, (double)var11).endVertex();
        var1.vertex(1.0, 1.0, -0.5).uv((double)var8, (double)var10).endVertex();
        var1.vertex(-1.0, 1.0, -0.5).uv((double)var9, (double)var10).endVertex();
        var0.end();
        GlStateManager.popMatrix();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderWater(float param0) {
        this.minecraft.getTextureManager().bind(UNDERWATER_LOCATION);
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        float var2 = this.minecraft.player.getBrightness();
        GlStateManager.color4f(var2, var2, var2, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        GlStateManager.pushMatrix();
        float var3 = 4.0F;
        float var4 = -1.0F;
        float var5 = 1.0F;
        float var6 = -1.0F;
        float var7 = 1.0F;
        float var8 = -0.5F;
        float var9 = -this.minecraft.player.yRot / 64.0F;
        float var10 = this.minecraft.player.xRot / 64.0F;
        var1.begin(7, DefaultVertexFormat.POSITION_TEX);
        var1.vertex(-1.0, -1.0, -0.5).uv((double)(4.0F + var9), (double)(4.0F + var10)).endVertex();
        var1.vertex(1.0, -1.0, -0.5).uv((double)(0.0F + var9), (double)(4.0F + var10)).endVertex();
        var1.vertex(1.0, 1.0, -0.5).uv((double)(0.0F + var9), (double)(0.0F + var10)).endVertex();
        var1.vertex(-1.0, 1.0, -0.5).uv((double)(4.0F + var9), (double)(0.0F + var10)).endVertex();
        var0.end();
        GlStateManager.popMatrix();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
    }

    private void renderFire() {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.9F);
        GlStateManager.depthFunc(519);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        float var2 = 1.0F;

        for(int var3 = 0; var3 < 2; ++var3) {
            GlStateManager.pushMatrix();
            TextureAtlasSprite var4 = this.minecraft.getTextureAtlas().getSprite(ModelBakery.FIRE_1);
            this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
            float var5 = var4.getU0();
            float var6 = var4.getU1();
            float var7 = var4.getV0();
            float var8 = var4.getV1();
            float var9 = -0.5F;
            float var10 = 0.5F;
            float var11 = -0.5F;
            float var12 = 0.5F;
            float var13 = -0.5F;
            GlStateManager.translatef((float)(-(var3 * 2 - 1)) * 0.24F, -0.3F, 0.0F);
            GlStateManager.rotatef((float)(var3 * 2 - 1) * 10.0F, 0.0F, 1.0F, 0.0F);
            var1.begin(7, DefaultVertexFormat.POSITION_TEX);
            var1.vertex(-0.5, -0.5, -0.5).uv((double)var6, (double)var8).endVertex();
            var1.vertex(0.5, -0.5, -0.5).uv((double)var5, (double)var8).endVertex();
            var1.vertex(0.5, 0.5, -0.5).uv((double)var5, (double)var7).endVertex();
            var1.vertex(-0.5, 0.5, -0.5).uv((double)var6, (double)var7).endVertex();
            var0.end();
            GlStateManager.popMatrix();
        }

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(515);
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
