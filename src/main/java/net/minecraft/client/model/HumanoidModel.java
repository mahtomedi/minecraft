package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HumanoidModel<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel {
    public ModelPart head;
    public ModelPart hat;
    public ModelPart body;
    public ModelPart rightArm;
    public ModelPart leftArm;
    public ModelPart rightLeg;
    public ModelPart leftLeg;
    public HumanoidModel.ArmPose leftArmPose = HumanoidModel.ArmPose.EMPTY;
    public HumanoidModel.ArmPose rightArmPose = HumanoidModel.ArmPose.EMPTY;
    public boolean crouching;
    public float swimAmount;

    public HumanoidModel(float param0) {
        this(RenderType::entityCutoutNoCull, param0, 0.0F, 64, 32);
    }

    protected HumanoidModel(float param0, float param1, int param2, int param3) {
        this(RenderType::entityCutoutNoCull, param0, param1, param2, param3);
    }

    public HumanoidModel(Function<ResourceLocation, RenderType> param0, float param1, float param2, int param3, int param4) {
        super(param0, true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
        this.texWidth = param3;
        this.texHeight = param4;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, param1);
        this.head.setPos(0.0F, 0.0F + param2, 0.0F);
        this.hat = new ModelPart(this, 32, 0);
        this.hat.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, param1 + 0.5F);
        this.hat.setPos(0.0F, 0.0F + param2, 0.0F);
        this.body = new ModelPart(this, 16, 16);
        this.body.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, param1);
        this.body.setPos(0.0F, 0.0F + param2, 0.0F);
        this.rightArm = new ModelPart(this, 40, 16);
        this.rightArm.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, param1);
        this.rightArm.setPos(-5.0F, 2.0F + param2, 0.0F);
        this.leftArm = new ModelPart(this, 40, 16);
        this.leftArm.mirror = true;
        this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, param1);
        this.leftArm.setPos(5.0F, 2.0F + param2, 0.0F);
        this.rightLeg = new ModelPart(this, 0, 16);
        this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param1);
        this.rightLeg.setPos(-1.9F, 12.0F + param2, 0.0F);
        this.leftLeg = new ModelPart(this, 0, 16);
        this.leftLeg.mirror = true;
        this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param1);
        this.leftLeg.setPos(1.9F, 12.0F + param2, 0.0F);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat);
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        this.swimAmount = param0.getSwimAmount(param3);
        super.prepareMobModel(param0, param1, param2, param3);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        boolean var0 = param0.getFallFlyingTicks() > 4;
        boolean var1 = param0.isVisuallySwimming();
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        if (var0) {
            this.head.xRot = (float) (-Math.PI / 4);
        } else if (this.swimAmount > 0.0F) {
            if (var1) {
                this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, (float) (-Math.PI / 4));
            } else {
                this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, param5 * (float) (Math.PI / 180.0));
            }
        } else {
            this.head.xRot = param5 * (float) (Math.PI / 180.0);
        }

        this.body.yRot = 0.0F;
        this.rightArm.z = 0.0F;
        this.rightArm.x = -5.0F;
        this.leftArm.z = 0.0F;
        this.leftArm.x = 5.0F;
        float var2 = 1.0F;
        if (var0) {
            var2 = (float)param0.getDeltaMovement().lengthSqr();
            var2 /= 0.2F;
            var2 *= var2 * var2;
        }

        if (var2 < 1.0F) {
            var2 = 1.0F;
        }

        this.rightArm.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 2.0F * param2 * 0.5F / var2;
        this.leftArm.xRot = Mth.cos(param1 * 0.6662F) * 2.0F * param2 * 0.5F / var2;
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightLeg.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2 / var2;
        this.leftLeg.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2 / var2;
        this.rightLeg.yRot = 0.0F;
        this.leftLeg.yRot = 0.0F;
        this.rightLeg.zRot = 0.0F;
        this.leftLeg.zRot = 0.0F;
        if (this.riding) {
            this.rightArm.xRot += (float) (-Math.PI / 5);
            this.leftArm.xRot += (float) (-Math.PI / 5);
            this.rightLeg.xRot = -1.4137167F;
            this.rightLeg.yRot = (float) (Math.PI / 10);
            this.rightLeg.zRot = 0.07853982F;
            this.leftLeg.xRot = -1.4137167F;
            this.leftLeg.yRot = (float) (-Math.PI / 10);
            this.leftLeg.zRot = -0.07853982F;
        }

        this.rightArm.yRot = 0.0F;
        this.leftArm.yRot = 0.0F;
        boolean var3 = param0.getMainArm() == HumanoidArm.RIGHT;
        boolean var4 = var3 ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
        if (var3 != var4) {
            this.poseLeftArm(param0);
            this.poseRightArm(param0);
        } else {
            this.poseRightArm(param0);
            this.poseLeftArm(param0);
        }

        this.setupAttackAnimation(param0, param3);
        if (this.crouching) {
            this.body.xRot = 0.5F;
            this.rightArm.xRot += 0.4F;
            this.leftArm.xRot += 0.4F;
            this.rightLeg.z = 4.0F;
            this.leftLeg.z = 4.0F;
            this.rightLeg.y = 12.2F;
            this.leftLeg.y = 12.2F;
            this.head.y = 4.2F;
            this.body.y = 3.2F;
            this.leftArm.y = 5.2F;
            this.rightArm.y = 5.2F;
        } else {
            this.body.xRot = 0.0F;
            this.rightLeg.z = 0.1F;
            this.leftLeg.z = 0.1F;
            this.rightLeg.y = 12.0F;
            this.leftLeg.y = 12.0F;
            this.head.y = 0.0F;
            this.body.y = 0.0F;
            this.leftArm.y = 2.0F;
            this.rightArm.y = 2.0F;
        }

        AnimationUtils.bobArms(this.rightArm, this.leftArm, param3);
        if (this.swimAmount > 0.0F) {
            float var5 = param1 % 26.0F;
            HumanoidArm var6 = this.getAttackArm(param0);
            float var7 = var6 == HumanoidArm.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
            float var8 = var6 == HumanoidArm.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
            if (var5 < 14.0F) {
                this.leftArm.xRot = this.rotlerpRad(var8, this.leftArm.xRot, 0.0F);
                this.rightArm.xRot = Mth.lerp(var7, this.rightArm.xRot, 0.0F);
                this.leftArm.yRot = this.rotlerpRad(var8, this.leftArm.yRot, (float) Math.PI);
                this.rightArm.yRot = Mth.lerp(var7, this.rightArm.yRot, (float) Math.PI);
                this.leftArm.zRot = this.rotlerpRad(
                    var8, this.leftArm.zRot, (float) Math.PI + 1.8707964F * this.quadraticArmUpdate(var5) / this.quadraticArmUpdate(14.0F)
                );
                this.rightArm.zRot = Mth.lerp(
                    var7, this.rightArm.zRot, (float) Math.PI - 1.8707964F * this.quadraticArmUpdate(var5) / this.quadraticArmUpdate(14.0F)
                );
            } else if (var5 >= 14.0F && var5 < 22.0F) {
                float var9 = (var5 - 14.0F) / 8.0F;
                this.leftArm.xRot = this.rotlerpRad(var8, this.leftArm.xRot, (float) (Math.PI / 2) * var9);
                this.rightArm.xRot = Mth.lerp(var7, this.rightArm.xRot, (float) (Math.PI / 2) * var9);
                this.leftArm.yRot = this.rotlerpRad(var8, this.leftArm.yRot, (float) Math.PI);
                this.rightArm.yRot = Mth.lerp(var7, this.rightArm.yRot, (float) Math.PI);
                this.leftArm.zRot = this.rotlerpRad(var8, this.leftArm.zRot, 5.012389F - 1.8707964F * var9);
                this.rightArm.zRot = Mth.lerp(var7, this.rightArm.zRot, 1.2707963F + 1.8707964F * var9);
            } else if (var5 >= 22.0F && var5 < 26.0F) {
                float var10 = (var5 - 22.0F) / 4.0F;
                this.leftArm.xRot = this.rotlerpRad(var8, this.leftArm.xRot, (float) (Math.PI / 2) - (float) (Math.PI / 2) * var10);
                this.rightArm.xRot = Mth.lerp(var7, this.rightArm.xRot, (float) (Math.PI / 2) - (float) (Math.PI / 2) * var10);
                this.leftArm.yRot = this.rotlerpRad(var8, this.leftArm.yRot, (float) Math.PI);
                this.rightArm.yRot = Mth.lerp(var7, this.rightArm.yRot, (float) Math.PI);
                this.leftArm.zRot = this.rotlerpRad(var8, this.leftArm.zRot, (float) Math.PI);
                this.rightArm.zRot = Mth.lerp(var7, this.rightArm.zRot, (float) Math.PI);
            }

            float var11 = 0.3F;
            float var12 = 0.33333334F;
            this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * Mth.cos(param1 * 0.33333334F + (float) Math.PI));
            this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * Mth.cos(param1 * 0.33333334F));
        }

        this.hat.copyFrom(this.head);
    }

    private void poseRightArm(T param0) {
        switch(this.rightArmPose) {
            case EMPTY:
                this.rightArm.yRot = 0.0F;
                break;
            case BLOCK:
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.9424779F;
                this.rightArm.yRot = (float) (-Math.PI / 6);
                break;
            case ITEM:
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float) (Math.PI / 10);
                this.rightArm.yRot = 0.0F;
                break;
            case THROW_SPEAR:
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float) Math.PI;
                this.rightArm.yRot = 0.0F;
                break;
            case BOW_AND_ARROW:
                this.rightArm.yRot = -0.1F + this.head.yRot;
                this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
                this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
                this.leftArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
                break;
            case CROSSBOW_CHARGE:
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, param0, true);
                break;
            case CROSSBOW_HOLD:
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
        }

    }

    private void poseLeftArm(T param0) {
        switch(this.leftArmPose) {
            case EMPTY:
                this.leftArm.yRot = 0.0F;
                break;
            case BLOCK:
                this.leftArm.xRot = this.leftArm.xRot * 0.5F - 0.9424779F;
                this.leftArm.yRot = (float) (Math.PI / 6);
                break;
            case ITEM:
                this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) (Math.PI / 10);
                this.leftArm.yRot = 0.0F;
                break;
            case THROW_SPEAR:
                this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) Math.PI;
                this.leftArm.yRot = 0.0F;
                break;
            case BOW_AND_ARROW:
                this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
                this.leftArm.yRot = 0.1F + this.head.yRot;
                this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
                this.leftArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
                break;
            case CROSSBOW_CHARGE:
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, param0, false);
                break;
            case CROSSBOW_HOLD:
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
        }

    }

    protected void setupAttackAnimation(T param0, float param1) {
        if (!(this.attackTime <= 0.0F)) {
            HumanoidArm var0 = this.getAttackArm(param0);
            ModelPart var1 = this.getArm(var0);
            float var2 = this.attackTime;
            this.body.yRot = Mth.sin(Mth.sqrt(var2) * (float) (Math.PI * 2)) * 0.2F;
            if (var0 == HumanoidArm.LEFT) {
                this.body.yRot *= -1.0F;
            }

            this.rightArm.z = Mth.sin(this.body.yRot) * 5.0F;
            this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0F;
            this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0F;
            this.leftArm.x = Mth.cos(this.body.yRot) * 5.0F;
            this.rightArm.yRot += this.body.yRot;
            this.leftArm.yRot += this.body.yRot;
            this.leftArm.xRot += this.body.yRot;
            var2 = 1.0F - this.attackTime;
            var2 *= var2;
            var2 *= var2;
            var2 = 1.0F - var2;
            float var3 = Mth.sin(var2 * (float) Math.PI);
            float var4 = Mth.sin(this.attackTime * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
            var1.xRot = (float)((double)var1.xRot - ((double)var3 * 1.2 + (double)var4));
            var1.yRot += this.body.yRot * 2.0F;
            var1.zRot += Mth.sin(this.attackTime * (float) Math.PI) * -0.4F;
        }
    }

    protected float rotlerpRad(float param0, float param1, float param2) {
        float var0 = (param2 - param1) % (float) (Math.PI * 2);
        if (var0 < (float) -Math.PI) {
            var0 += (float) (Math.PI * 2);
        }

        if (var0 >= (float) Math.PI) {
            var0 -= (float) (Math.PI * 2);
        }

        return param1 + param0 * var0;
    }

    private float quadraticArmUpdate(float param0) {
        return -65.0F * param0 + param0 * param0;
    }

    public void copyPropertiesTo(HumanoidModel<T> param0) {
        super.copyPropertiesTo(param0);
        param0.leftArmPose = this.leftArmPose;
        param0.rightArmPose = this.rightArmPose;
        param0.crouching = this.crouching;
        param0.head.copyFrom(this.head);
        param0.hat.copyFrom(this.hat);
        param0.body.copyFrom(this.body);
        param0.rightArm.copyFrom(this.rightArm);
        param0.leftArm.copyFrom(this.leftArm);
        param0.rightLeg.copyFrom(this.rightLeg);
        param0.leftLeg.copyFrom(this.leftLeg);
    }

    public void setAllVisible(boolean param0) {
        this.head.visible = param0;
        this.hat.visible = param0;
        this.body.visible = param0;
        this.rightArm.visible = param0;
        this.leftArm.visible = param0;
        this.rightLeg.visible = param0;
        this.leftLeg.visible = param0;
    }

    @Override
    public void translateToHand(HumanoidArm param0, PoseStack param1) {
        this.getArm(param0).translateAndRotate(param1);
    }

    protected ModelPart getArm(HumanoidArm param0) {
        return param0 == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    protected HumanoidArm getAttackArm(T param0) {
        HumanoidArm var0 = param0.getMainArm();
        return param0.swingingArm == InteractionHand.MAIN_HAND ? var0 : var0.getOpposite();
    }

    @OnlyIn(Dist.CLIENT)
    public static enum ArmPose {
        EMPTY(false),
        ITEM(false),
        BLOCK(false),
        BOW_AND_ARROW(true),
        THROW_SPEAR(false),
        CROSSBOW_CHARGE(true),
        CROSSBOW_HOLD(true);

        private final boolean twoHanded;

        private ArmPose(boolean param0) {
            this.twoHanded = param0;
        }

        public boolean isTwoHanded() {
            return this.twoHanded;
        }
    }
}
