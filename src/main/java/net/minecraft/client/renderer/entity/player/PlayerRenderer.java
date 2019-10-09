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

    public void render(
        AbstractClientPlayer param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7
    ) {
        this.setModelProperties(param0);
        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public Vec3 getRenderOffset(AbstractClientPlayer param0, double param1, double param2, double param3, float param4) {
        return param0.isCrouching() ? new Vec3(0.0, -0.125, 0.0) : super.getRenderOffset(param0, param1, param2, param3, param4);
    }

    private void setModelProperties(AbstractClientPlayer param0) {
        PlayerModel<AbstractClientPlayer> var0 = this.getModel();
        if (param0.isSpectator()) {
            var0.setAllVisible(false);
            var0.head.visible = true;
            var0.hat.visible = true;
        } else {
            ItemStack var1 = param0.getMainHandItem();
            ItemStack var2 = param0.getOffhandItem();
            var0.setAllVisible(true);
            var0.hat.visible = param0.isModelPartShown(PlayerModelPart.HAT);
            var0.jacket.visible = param0.isModelPartShown(PlayerModelPart.JACKET);
            var0.leftPants.visible = param0.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
            var0.rightPants.visible = param0.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
            var0.leftSleeve.visible = param0.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
            var0.rightSleeve.visible = param0.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
            var0.crouching = param0.isCrouching();
            HumanoidModel.ArmPose var3 = this.getArmPose(param0, var1, var2, InteractionHand.MAIN_HAND);
            HumanoidModel.ArmPose var4 = this.getArmPose(param0, var1, var2, InteractionHand.OFF_HAND);
            if (param0.getMainArm() == HumanoidArm.RIGHT) {
                var0.rightArmPose = var3;
                var0.leftArmPose = var4;
            } else {
                var0.rightArmPose = var4;
                var0.leftArmPose = var3;
            }
        }

    }

    private HumanoidModel.ArmPose getArmPose(AbstractClientPlayer param0, ItemStack param1, ItemStack param2, InteractionHand param3) {
        HumanoidModel.ArmPose var0 = HumanoidModel.ArmPose.EMPTY;
        ItemStack var1 = param3 == InteractionHand.MAIN_HAND ? param1 : param2;
        if (!var1.isEmpty()) {
            var0 = HumanoidModel.ArmPose.ITEM;
            if (param0.getUseItemRemainingTicks() > 0) {
                UseAnim var2 = var1.getUseAnimation();
                if (var2 == UseAnim.BLOCK) {
                    var0 = HumanoidModel.ArmPose.BLOCK;
                } else if (var2 == UseAnim.BOW) {
                    var0 = HumanoidModel.ArmPose.BOW_AND_ARROW;
                } else if (var2 == UseAnim.SPEAR) {
                    var0 = HumanoidModel.ArmPose.THROW_SPEAR;
                } else if (var2 == UseAnim.CROSSBOW && param3 == param0.getUsedItemHand()) {
                    var0 = HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }
            } else {
                boolean var3 = param1.getItem() == Items.CROSSBOW;
                boolean var4 = CrossbowItem.isCharged(param1);
                boolean var5 = param2.getItem() == Items.CROSSBOW;
                boolean var6 = CrossbowItem.isCharged(param2);
                if (var3 && var4) {
                    var0 = HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }

                if (var5 && var6 && param1.getItem().getUseAnimation(param1) == UseAnim.NONE) {
                    var0 = HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }
            }
        }

        return var0;
    }

    public ResourceLocation getTextureLocation(AbstractClientPlayer param0) {
        return param0.getSkinTextureLocation();
    }

    protected void scale(AbstractClientPlayer param0, PoseStack param1, float param2) {
        float var0 = 0.9375F;
        param1.scale(0.9375F, 0.9375F, 0.9375F);
    }

    protected void renderNameTag(AbstractClientPlayer param0, String param1, PoseStack param2, MultiBufferSource param3) {
        double var0 = this.entityRenderDispatcher.distanceToSqr(param0);
        param2.pushPose();
        if (var0 < 100.0) {
            Scoreboard var1 = param0.getScoreboard();
            Objective var2 = var1.getDisplayObjective(2);
            if (var2 != null) {
                Score var3 = var1.getOrCreatePlayerScore(param0.getScoreboardName(), var2);
                super.renderNameTag(param0, var3.getScore() + " " + var2.getDisplayName().getColoredString(), param2, param3);
                param2.translate(0.0, (double)(9.0F * 1.15F * 0.025F), 0.0);
            }
        }

        super.renderNameTag(param0, param1, param2, param3);
        param2.popPose();
    }

    public void renderRightHand(PoseStack param0, MultiBufferSource param1, AbstractClientPlayer param2) {
        this.renderHand(param0, param1, param2, this.model.rightArm, this.model.rightSleeve);
    }

    public void renderLeftHand(PoseStack param0, MultiBufferSource param1, AbstractClientPlayer param2) {
        this.renderHand(param0, param1, param2, this.model.leftArm, this.model.leftSleeve);
    }

    private void renderHand(PoseStack param0, MultiBufferSource param1, AbstractClientPlayer param2, ModelPart param3, ModelPart param4) {
        float var0 = 0.0625F;
        PlayerModel<AbstractClientPlayer> var1 = this.getModel();
        this.setModelProperties(param2);
        int var2 = param2.getLightColor();
        var1.attackTime = 0.0F;
        var1.crouching = false;
        var1.swimAmount = 0.0F;
        var1.setupAnim(param2, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        param3.xRot = 0.0F;
        param3.render(param0, param1.getBuffer(RenderType.entitySolid(param2.getSkinTextureLocation())), 0.0625F, var2, OverlayTexture.NO_OVERLAY, null);
        param4.xRot = 0.0F;
        param4.render(param0, param1.getBuffer(RenderType.entityTranslucent(param2.getSkinTextureLocation())), 0.0625F, var2, OverlayTexture.NO_OVERLAY, null);
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
                double var7 = (var4.x * var3.x + var4.z * var3.z) / (Math.sqrt(var5) * Math.sqrt(var6));
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
