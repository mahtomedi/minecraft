package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
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
    private float itemUseTicks;

    public HumanoidModel() {
        this(0.0F);
    }

    public HumanoidModel(float param0) {
        this(param0, 0.0F, 64, 32);
    }

    public HumanoidModel(float param0, float param1, int param2, int param3) {
        super(true, 16.0F, 0.0F);
        this.texWidth = param2;
        this.texHeight = param3;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, param0);
        this.head.setPos(0.0F, 0.0F + param1, 0.0F);
        this.hat = new ModelPart(this, 32, 0);
        this.hat.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, param0 + 0.5F);
        this.hat.setPos(0.0F, 0.0F + param1, 0.0F);
        this.body = new ModelPart(this, 16, 16);
        this.body.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, param0);
        this.body.setPos(0.0F, 0.0F + param1, 0.0F);
        this.rightArm = new ModelPart(this, 40, 16);
        this.rightArm.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0);
        this.rightArm.setPos(-5.0F, 2.0F + param1, 0.0F);
        this.leftArm = new ModelPart(this, 40, 16);
        this.leftArm.mirror = true;
        this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0);
        this.leftArm.setPos(5.0F, 2.0F + param1, 0.0F);
        this.rightLeg = new ModelPart(this, 0, 16);
        this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0);
        this.rightLeg.setPos(-1.9F, 12.0F + param1, 0.0F);
        this.leftLeg = new ModelPart(this, 0, 16);
        this.leftLeg.mirror = true;
        this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0);
        this.leftLeg.setPos(1.9F, 12.0F + param1, 0.0F);
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
        this.itemUseTicks = (float)param0.getTicksUsingItem();
        super.prepareMobModel(param0, param1, param2, param3);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        boolean var0 = param0.getFallFlyingTicks() > 4;
        boolean var1 = param0.isVisuallySwimming();
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        if (var0) {
            this.head.xRot = (float) (-Math.PI / 4);
        } else if (this.swimAmount > 0.0F) {
            if (var1) {
                this.head.xRot = this.rotlerpRad(this.head.xRot, (float) (-Math.PI / 4), this.swimAmount);
            } else {
                this.head.xRot = this.rotlerpRad(this.head.xRot, param5 * (float) (Math.PI / 180.0), this.swimAmount);
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
        this.rightArm.zRot = 0.0F;
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
        }

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
        }

        if (this.leftArmPose == HumanoidModel.ArmPose.THROW_SPEAR
            && this.rightArmPose != HumanoidModel.ArmPose.BLOCK
            && this.rightArmPose != HumanoidModel.ArmPose.THROW_SPEAR
            && this.rightArmPose != HumanoidModel.ArmPose.BOW_AND_ARROW) {
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) Math.PI;
            this.leftArm.yRot = 0.0F;
        }

        if (this.attackTime > 0.0F) {
            HumanoidArm var3 = this.getAttackArm(param0);
            ModelPart var4 = this.getArm(var3);
            float var5 = this.attackTime;
            this.body.yRot = Mth.sin(Mth.sqrt(var5) * (float) (Math.PI * 2)) * 0.2F;
            if (var3 == HumanoidArm.LEFT) {
                this.body.yRot *= -1.0F;
            }

            this.rightArm.z = Mth.sin(this.body.yRot) * 5.0F;
            this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0F;
            this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0F;
            this.leftArm.x = Mth.cos(this.body.yRot) * 5.0F;
            this.rightArm.yRot += this.body.yRot;
            this.leftArm.yRot += this.body.yRot;
            this.leftArm.xRot += this.body.yRot;
            var5 = 1.0F - this.attackTime;
            var5 *= var5;
            var5 *= var5;
            var5 = 1.0F - var5;
            float var6 = Mth.sin(var5 * (float) Math.PI);
            float var7 = Mth.sin(this.attackTime * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
            var4.xRot = (float)((double)var4.xRot - ((double)var6 * 1.2 + (double)var7));
            var4.yRot += this.body.yRot * 2.0F;
            var4.zRot += Mth.sin(this.attackTime * (float) Math.PI) * -0.4F;
        }

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

        this.rightArm.zRot += Mth.cos(param3 * 0.09F) * 0.05F + 0.05F;
        this.leftArm.zRot -= Mth.cos(param3 * 0.09F) * 0.05F + 0.05F;
        this.rightArm.xRot += Mth.sin(param3 * 0.067F) * 0.05F;
        this.leftArm.xRot -= Mth.sin(param3 * 0.067F) * 0.05F;
        if (this.rightArmPose == HumanoidModel.ArmPose.BOW_AND_ARROW) {
            this.rightArm.yRot = -0.1F + this.head.yRot;
            this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
            this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
            this.leftArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
        } else if (this.leftArmPose == HumanoidModel.ArmPose.BOW_AND_ARROW
            && this.rightArmPose != HumanoidModel.ArmPose.THROW_SPEAR
            && this.rightArmPose != HumanoidModel.ArmPose.BLOCK) {
            this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
            this.leftArm.yRot = 0.1F + this.head.yRot;
            this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
            this.leftArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
        }

        float var8 = (float)CrossbowItem.getChargeDuration(param0.getUseItem());
        if (this.rightArmPose == HumanoidModel.ArmPose.CROSSBOW_CHARGE) {
            this.rightArm.yRot = -0.8F;
            this.rightArm.xRot = -0.97079635F;
            this.leftArm.xRot = -0.97079635F;
            float var9 = Mth.clamp(this.itemUseTicks, 0.0F, var8);
            this.leftArm.yRot = Mth.lerp(var9 / var8, 0.4F, 0.85F);
            this.leftArm.xRot = Mth.lerp(var9 / var8, this.leftArm.xRot, (float) (-Math.PI / 2));
        } else if (this.leftArmPose == HumanoidModel.ArmPose.CROSSBOW_CHARGE) {
            this.leftArm.yRot = 0.8F;
            this.rightArm.xRot = -0.97079635F;
            this.leftArm.xRot = -0.97079635F;
            float var10 = Mth.clamp(this.itemUseTicks, 0.0F, var8);
            this.rightArm.yRot = Mth.lerp(var10 / var8, -0.4F, -0.85F);
            this.rightArm.xRot = Mth.lerp(var10 / var8, this.rightArm.xRot, (float) (-Math.PI / 2));
        }

        if (this.rightArmPose == HumanoidModel.ArmPose.CROSSBOW_HOLD && this.attackTime <= 0.0F) {
            this.rightArm.yRot = -0.3F + this.head.yRot;
            this.leftArm.yRot = 0.6F + this.head.yRot;
            this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot + 0.1F;
            this.leftArm.xRot = -1.5F + this.head.xRot;
        } else if (this.leftArmPose == HumanoidModel.ArmPose.CROSSBOW_HOLD) {
            this.rightArm.yRot = -0.6F + this.head.yRot;
            this.leftArm.yRot = 0.3F + this.head.yRot;
            this.rightArm.xRot = -1.5F + this.head.xRot;
            this.leftArm.xRot = (float) (-Math.PI / 2) + this.head.xRot + 0.1F;
        }

        if (this.swimAmount > 0.0F) {
            float var11 = param1 % 26.0F;
            float var12 = this.attackTime > 0.0F ? 0.0F : this.swimAmount;
            if (var11 < 14.0F) {
                this.leftArm.xRot = this.rotlerpRad(this.leftArm.xRot, 0.0F, this.swimAmount);
                this.rightArm.xRot = Mth.lerp(var12, this.rightArm.xRot, 0.0F);
                this.leftArm.yRot = this.rotlerpRad(this.leftArm.yRot, (float) Math.PI, this.swimAmount);
                this.rightArm.yRot = Mth.lerp(var12, this.rightArm.yRot, (float) Math.PI);
                this.leftArm.zRot = this.rotlerpRad(
                    this.leftArm.zRot, (float) Math.PI + 1.8707964F * this.quadraticArmUpdate(var11) / this.quadraticArmUpdate(14.0F), this.swimAmount
                );
                this.rightArm.zRot = Mth.lerp(
                    var12, this.rightArm.zRot, (float) Math.PI - 1.8707964F * this.quadraticArmUpdate(var11) / this.quadraticArmUpdate(14.0F)
                );
            } else if (var11 >= 14.0F && var11 < 22.0F) {
                float var13 = (var11 - 14.0F) / 8.0F;
                this.leftArm.xRot = this.rotlerpRad(this.leftArm.xRot, (float) (Math.PI / 2) * var13, this.swimAmount);
                this.rightArm.xRot = Mth.lerp(var12, this.rightArm.xRot, (float) (Math.PI / 2) * var13);
                this.leftArm.yRot = this.rotlerpRad(this.leftArm.yRot, (float) Math.PI, this.swimAmount);
                this.rightArm.yRot = Mth.lerp(var12, this.rightArm.yRot, (float) Math.PI);
                this.leftArm.zRot = this.rotlerpRad(this.leftArm.zRot, 5.012389F - 1.8707964F * var13, this.swimAmount);
                this.rightArm.zRot = Mth.lerp(var12, this.rightArm.zRot, 1.2707963F + 1.8707964F * var13);
            } else if (var11 >= 22.0F && var11 < 26.0F) {
                float var14 = (var11 - 22.0F) / 4.0F;
                this.leftArm.xRot = this.rotlerpRad(this.leftArm.xRot, (float) (Math.PI / 2) - (float) (Math.PI / 2) * var14, this.swimAmount);
                this.rightArm.xRot = Mth.lerp(var12, this.rightArm.xRot, (float) (Math.PI / 2) - (float) (Math.PI / 2) * var14);
                this.leftArm.yRot = this.rotlerpRad(this.leftArm.yRot, (float) Math.PI, this.swimAmount);
                this.rightArm.yRot = Mth.lerp(var12, this.rightArm.yRot, (float) Math.PI);
                this.leftArm.zRot = this.rotlerpRad(this.leftArm.zRot, (float) Math.PI, this.swimAmount);
                this.rightArm.zRot = Mth.lerp(var12, this.rightArm.zRot, (float) Math.PI);
            }

            float var15 = 0.3F;
            float var16 = 0.33333334F;
            this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * Mth.cos(param1 * 0.33333334F + (float) Math.PI));
            this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * Mth.cos(param1 * 0.33333334F));
        }

        this.hat.copyFrom(this.head);
    }

    protected float rotlerpRad(float param0, float param1, float param2) {
        float var0 = (param1 - param0) % (float) (Math.PI * 2);
        if (var0 < (float) -Math.PI) {
            var0 += (float) (Math.PI * 2);
        }

        if (var0 >= (float) Math.PI) {
            var0 -= (float) (Math.PI * 2);
        }

        return param0 + param2 * var0;
    }

    private float quadraticArmUpdate(float param0) {
        return -65.0F * param0 + param0 * param0;
    }

    public void copyPropertiesTo(HumanoidModel<T> param0) {
        super.copyPropertiesTo(param0);
        param0.leftArmPose = this.leftArmPose;
        param0.rightArmPose = this.rightArmPose;
        param0.crouching = this.crouching;
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
    public void translateToHand(float param0, HumanoidArm param1, PoseStack param2) {
        this.getArm(param1).translateAndRotate(param2, param0);
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
        EMPTY,
        ITEM,
        BLOCK,
        BOW_AND_ARROW,
        THROW_SPEAR,
        CROSSBOW_CHARGE,
        CROSSBOW_HOLD;
    }
}
