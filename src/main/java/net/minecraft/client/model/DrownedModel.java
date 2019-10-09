package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DrownedModel<T extends Zombie> extends ZombieModel<T> {
    public DrownedModel(float param0, float param1, int param2, int param3) {
        super(RenderType::entityCutoutNoCull, param0, param1, param2, param3);
        this.rightArm = new ModelPart(this, 32, 48);
        this.rightArm.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0);
        this.rightArm.setPos(-5.0F, 2.0F + param1, 0.0F);
        this.rightLeg = new ModelPart(this, 16, 48);
        this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0);
        this.rightLeg.setPos(-1.9F, 12.0F + param1, 0.0F);
    }

    public DrownedModel(float param0, boolean param1) {
        super(RenderType::entityCutoutNoCull, param0, 0.0F, 64, param1 ? 32 : 64);
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        this.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        ItemStack var0 = param0.getItemInHand(InteractionHand.MAIN_HAND);
        if (var0.getItem() == Items.TRIDENT && param0.isAggressive()) {
            if (param0.getMainArm() == HumanoidArm.RIGHT) {
                this.rightArmPose = HumanoidModel.ArmPose.THROW_SPEAR;
            } else {
                this.leftArmPose = HumanoidModel.ArmPose.THROW_SPEAR;
            }
        }

        super.prepareMobModel(param0, param1, param2, param3);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        super.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        if (this.leftArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) Math.PI;
            this.leftArm.yRot = 0.0F;
        }

        if (this.rightArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float) Math.PI;
            this.rightArm.yRot = 0.0F;
        }

        if (this.swimAmount > 0.0F) {
            this.rightArm.xRot = this.rotlerpRad(this.rightArm.xRot, (float) (-Math.PI * 4.0 / 5.0), this.swimAmount)
                + this.swimAmount * 0.35F * Mth.sin(0.1F * param3);
            this.leftArm.xRot = this.rotlerpRad(this.leftArm.xRot, (float) (-Math.PI * 4.0 / 5.0), this.swimAmount)
                - this.swimAmount * 0.35F * Mth.sin(0.1F * param3);
            this.rightArm.zRot = this.rotlerpRad(this.rightArm.zRot, -0.15F, this.swimAmount);
            this.leftArm.zRot = this.rotlerpRad(this.leftArm.zRot, 0.15F, this.swimAmount);
            this.leftLeg.xRot -= this.swimAmount * 0.55F * Mth.sin(0.1F * param3);
            this.rightLeg.xRot += this.swimAmount * 0.55F * Mth.sin(0.1F * param3);
            this.head.xRot = 0.0F;
        }

    }
}
