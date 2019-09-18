package net.minecraft.client.renderer.entity.player;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
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

    public void render(AbstractClientPlayer param0, double param1, double param2, double param3, float param4, float param5) {
        if (!param0.isLocalPlayer() || this.entityRenderDispatcher.camera.getEntity() == param0) {
            double var0 = param2;
            if (param0.isCrouching()) {
                var0 = param2 - 0.125;
            }

            this.setModelProperties(param0);
            RenderSystem.setProfile(RenderSystem.Profile.PLAYER_SKIN);
            super.render(param0, param1, var0, param3, param4, param5);
            RenderSystem.unsetProfile(RenderSystem.Profile.PLAYER_SKIN);
        }
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

    protected void scale(AbstractClientPlayer param0, float param1) {
        float var0 = 0.9375F;
        RenderSystem.scalef(0.9375F, 0.9375F, 0.9375F);
    }

    protected void renderNameTags(AbstractClientPlayer param0, double param1, double param2, double param3, String param4, double param5) {
        if (param5 < 100.0) {
            Scoreboard var0 = param0.getScoreboard();
            Objective var1 = var0.getDisplayObjective(2);
            if (var1 != null) {
                Score var2 = var0.getOrCreatePlayerScore(param0.getScoreboardName(), var1);
                this.renderNameTag(param0, var2.getScore() + " " + var1.getDisplayName().getColoredString(), param1, param2, param3, 64);
                param2 += (double)(9.0F * 1.15F * 0.025F);
            }
        }

        super.renderNameTags(param0, param1, param2, param3, param4, param5);
    }

    public void renderRightHand(AbstractClientPlayer param0) {
        float var0 = 1.0F;
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        float var1 = 0.0625F;
        PlayerModel<AbstractClientPlayer> var2 = this.getModel();
        this.setModelProperties(param0);
        RenderSystem.enableBlend();
        var2.attackTime = 0.0F;
        var2.crouching = false;
        var2.swimAmount = 0.0F;
        var2.setupAnim(param0, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        var2.rightArm.xRot = 0.0F;
        var2.rightArm.render(0.0625F);
        var2.rightSleeve.xRot = 0.0F;
        var2.rightSleeve.render(0.0625F);
        RenderSystem.disableBlend();
    }

    public void renderLeftHand(AbstractClientPlayer param0) {
        float var0 = 1.0F;
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        float var1 = 0.0625F;
        PlayerModel<AbstractClientPlayer> var2 = this.getModel();
        this.setModelProperties(param0);
        RenderSystem.enableBlend();
        var2.crouching = false;
        var2.attackTime = 0.0F;
        var2.swimAmount = 0.0F;
        var2.setupAnim(param0, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        var2.leftArm.xRot = 0.0F;
        var2.leftArm.render(0.0625F);
        var2.leftSleeve.xRot = 0.0F;
        var2.leftSleeve.render(0.0625F);
        RenderSystem.disableBlend();
    }

    protected void setupRotations(AbstractClientPlayer param0, float param1, float param2, float param3) {
        float var0 = param0.getSwimAmount(param3);
        if (param0.isFallFlying()) {
            super.setupRotations(param0, param1, param2, param3);
            float var1 = (float)param0.getFallFlyingTicks() + param3;
            float var2 = Mth.clamp(var1 * var1 / 100.0F, 0.0F, 1.0F);
            if (!param0.isAutoSpinAttack()) {
                RenderSystem.rotatef(var2 * (-90.0F - param0.xRot), 1.0F, 0.0F, 0.0F);
            }

            Vec3 var3 = param0.getViewVector(param3);
            Vec3 var4 = param0.getDeltaMovement();
            double var5 = Entity.getHorizontalDistanceSqr(var4);
            double var6 = Entity.getHorizontalDistanceSqr(var3);
            if (var5 > 0.0 && var6 > 0.0) {
                double var7 = (var4.x * var3.x + var4.z * var3.z) / (Math.sqrt(var5) * Math.sqrt(var6));
                double var8 = var4.x * var3.z - var4.z * var3.x;
                RenderSystem.rotatef((float)(Math.signum(var8) * Math.acos(var7)) * 180.0F / (float) Math.PI, 0.0F, 1.0F, 0.0F);
            }
        } else if (var0 > 0.0F) {
            super.setupRotations(param0, param1, param2, param3);
            float var9 = param0.isInWater() ? -90.0F - param0.xRot : -90.0F;
            float var10 = Mth.lerp(var0, 0.0F, var9);
            RenderSystem.rotatef(var10, 1.0F, 0.0F, 0.0F);
            if (param0.isVisuallySwimming()) {
                RenderSystem.translatef(0.0F, -1.0F, 0.3F);
            }
        } else {
            super.setupRotations(param0, param1, param2, param3);
        }

    }
}
