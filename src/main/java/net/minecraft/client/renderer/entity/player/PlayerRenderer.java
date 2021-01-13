package net.minecraft.client.renderer.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.Deadmau5EarsLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public PlayerRenderer(EntityRenderDispatcher param0) {
        this(param0, false);
    }

    public PlayerRenderer(EntityRenderDispatcher param0, boolean param1) {
        super(param0, new PlayerModel<>(0.0F, param1), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel(0.5F), new HumanoidModel(1.0F)));
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new ArrowLayer<>(this));
        this.addLayer(new Deadmau5EarsLayer(this));
        this.addLayer(new CapeLayer(this));
        this.addLayer(new CustomHeadLayer<>(this));
        this.addLayer(new ElytraLayer<>(this));
        this.addLayer(new ParrotOnShoulderLayer<>(this));
        this.addLayer(new SpinAttackEffectLayer<>(this));
        this.addLayer(new BeeStingerLayer<>(this));
    }

    public void render(AbstractClientPlayer param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        this.setModelProperties(param0);
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public Vec3 getRenderOffset(AbstractClientPlayer param0, float param1) {
        return param0.isCrouching() ? new Vec3(0.0, -0.125, 0.0) : super.getRenderOffset(param0, param1);
    }

    private void setModelProperties(AbstractClientPlayer param0) {
        PlayerModel<AbstractClientPlayer> var0 = this.getModel();
        if (param0.isSpectator()) {
            var0.setAllVisible(false);
            var0.head.visible = true;
            var0.hat.visible = true;
        } else {
            var0.setAllVisible(true);
            var0.hat.visible = param0.isModelPartShown(PlayerModelPart.HAT);
            var0.jacket.visible = param0.isModelPartShown(PlayerModelPart.JACKET);
            var0.leftPants.visible = param0.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
            var0.rightPants.visible = param0.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
            var0.leftSleeve.visible = param0.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
            var0.rightSleeve.visible = param0.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
            var0.crouching = param0.isCrouching();
            HumanoidModel.ArmPose var1 = getArmPose(param0, InteractionHand.MAIN_HAND);
            HumanoidModel.ArmPose var2 = getArmPose(param0, InteractionHand.OFF_HAND);
            if (var1.isTwoHanded()) {
                var2 = param0.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
            }

            if (param0.getMainArm() == HumanoidArm.RIGHT) {
                var0.rightArmPose = var1;
                var0.leftArmPose = var2;
            } else {
                var0.rightArmPose = var2;
                var0.leftArmPose = var1;
            }
        }

    }

    private static HumanoidModel.ArmPose getArmPose(AbstractClientPlayer param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (var0.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else {
            if (param0.getUsedItemHand() == param1 && param0.getUseItemRemainingTicks() > 0) {
                UseAnim var1 = var0.getUseAnimation();
                if (var1 == UseAnim.BLOCK) {
                    return HumanoidModel.ArmPose.BLOCK;
                }

                if (var1 == UseAnim.BOW) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }

                if (var1 == UseAnim.SPEAR) {
                    return HumanoidModel.ArmPose.THROW_SPEAR;
                }

                if (var1 == UseAnim.CROSSBOW && param1 == param0.getUsedItemHand()) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }
            } else if (!param0.swinging && var0.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(var0)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }

            return HumanoidModel.ArmPose.ITEM;
        }
    }

    public ResourceLocation getTextureLocation(AbstractClientPlayer param0) {
        return param0.getSkinTextureLocation();
    }

    protected void scale(AbstractClientPlayer param0, PoseStack param1, float param2) {
        float var0 = 0.9375F;
        param1.scale(0.9375F, 0.9375F, 0.9375F);
    }

    protected void renderNameTag(AbstractClientPlayer param0, Component param1, PoseStack param2, MultiBufferSource param3, int param4) {
        double var0 = this.entityRenderDispatcher.distanceToSqr(param0);
        param2.pushPose();
        if (var0 < 100.0) {
            Scoreboard var1 = param0.getScoreboard();
            Objective var2 = var1.getDisplayObjective(2);
            if (var2 != null) {
                Score var3 = var1.getOrCreatePlayerScore(param0.getScoreboardName(), var2);
                super.renderNameTag(
                    param0, new TextComponent(Integer.toString(var3.getScore())).append(" ").append(var2.getDisplayName()), param2, param3, param4
                );
                param2.translate(0.0, (double)(9.0F * 1.15F * 0.025F), 0.0);
            }
        }

        super.renderNameTag(param0, param1, param2, param3, param4);
        param2.popPose();
    }

    public void renderRightHand(PoseStack param0, MultiBufferSource param1, int param2, AbstractClientPlayer param3) {
        this.renderHand(param0, param1, param2, param3, this.model.rightArm, this.model.rightSleeve);
    }

    public void renderLeftHand(PoseStack param0, MultiBufferSource param1, int param2, AbstractClientPlayer param3) {
        this.renderHand(param0, param1, param2, param3, this.model.leftArm, this.model.leftSleeve);
    }

    private void renderHand(PoseStack param0, MultiBufferSource param1, int param2, AbstractClientPlayer param3, ModelPart param4, ModelPart param5) {
        PlayerModel<AbstractClientPlayer> var0 = this.getModel();
        this.setModelProperties(param3);
        var0.attackTime = 0.0F;
        var0.crouching = false;
        var0.swimAmount = 0.0F;
        var0.setupAnim(param3, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        param4.xRot = 0.0F;
        param4.render(param0, param1.getBuffer(RenderType.entitySolid(param3.getSkinTextureLocation())), param2, OverlayTexture.NO_OVERLAY);
        param5.xRot = 0.0F;
        param5.render(param0, param1.getBuffer(RenderType.entityTranslucent(param3.getSkinTextureLocation())), param2, OverlayTexture.NO_OVERLAY);
    }

    protected void setupRotations(AbstractClientPlayer param0, PoseStack param1, float param2, float param3, float param4) {
        float var0 = param0.getSwimAmount(param4);
        if (param0.isFallFlying()) {
            super.setupRotations(param0, param1, param2, param3, param4);
            float var1 = (float)param0.getFallFlyingTicks() + param4;
            float var2 = Mth.clamp(var1 * var1 / 100.0F, 0.0F, 1.0F);
            if (!param0.isAutoSpinAttack()) {
                param1.mulPose(Vector3f.XP.rotationDegrees(var2 * (-90.0F - param0.xRot)));
            }

            Vec3 var3 = param0.getViewVector(param4);
            Vec3 var4 = param0.getDeltaMovement();
            double var5 = Entity.getHorizontalDistanceSqr(var4);
            double var6 = Entity.getHorizontalDistanceSqr(var3);
            if (var5 > 0.0 && var6 > 0.0) {
                double var7 = (var4.x * var3.x + var4.z * var3.z) / Math.sqrt(var5 * var6);
                double var8 = var4.x * var3.z - var4.z * var3.x;
                param1.mulPose(Vector3f.YP.rotation((float)(Math.signum(var8) * Math.acos(var7))));
            }
        } else if (var0 > 0.0F) {
            super.setupRotations(param0, param1, param2, param3, param4);
            float var9 = param0.isInWater() ? -90.0F - param0.xRot : -90.0F;
            float var10 = Mth.lerp(var0, 0.0F, var9);
            param1.mulPose(Vector3f.XP.rotationDegrees(var10));
            if (param0.isVisuallySwimming()) {
                param1.translate(0.0, -1.0, 0.3F);
            }
        } else {
            super.setupRotations(param0, param1, param2, param3, param4);
        }

    }
}
