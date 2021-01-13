package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkeletonModel<T extends Mob & RangedAttackMob> extends HumanoidModel<T> {
    public SkeletonModel() {
        this(0.0F, false);
    }

    public SkeletonModel(float param0, boolean param1) {
        super(param0);
        if (!param1) {
            this.rightArm = new ModelPart(this, 40, 16);
            this.rightArm.addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, param0);
            this.rightArm.setPos(-5.0F, 2.0F, 0.0F);
            this.leftArm = new ModelPart(this, 40, 16);
            this.leftArm.mirror = true;
            this.leftArm.addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, param0);
            this.leftArm.setPos(5.0F, 2.0F, 0.0F);
            this.rightLeg = new ModelPart(this, 0, 16);
            this.rightLeg.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, param0);
            this.rightLeg.setPos(-2.0F, 12.0F, 0.0F);
            this.leftLeg = new ModelPart(this, 0, 16);
            this.leftLeg.mirror = true;
            this.leftLeg.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, param0);
            this.leftLeg.setPos(2.0F, 12.0F, 0.0F);
        }

    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        this.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        ItemStack var0 = param0.getItemInHand(InteractionHand.MAIN_HAND);
        if (var0.getItem() == Items.BOW && param0.isAggressive()) {
            if (param0.getMainArm() == HumanoidArm.RIGHT) {
                this.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
            } else {
                this.leftArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
        }

        super.prepareMobModel(param0, param1, param2, param3);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        super.setupAnim(param0, param1, param2, param3, param4, param5);
        ItemStack var0 = param0.getMainHandItem();
        if (param0.isAggressive() && (var0.isEmpty() || var0.getItem() != Items.BOW)) {
            float var1 = Mth.sin(this.attackTime * (float) Math.PI);
            float var2 = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * (float) Math.PI);
            this.rightArm.zRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightArm.yRot = -(0.1F - var1 * 0.6F);
            this.leftArm.yRot = 0.1F - var1 * 0.6F;
            this.rightArm.xRot = (float) (-Math.PI / 2);
            this.leftArm.xRot = (float) (-Math.PI / 2);
            this.rightArm.xRot -= var1 * 1.2F - var2 * 0.4F;
            this.leftArm.xRot -= var1 * 1.2F - var2 * 0.4F;
            AnimationUtils.bobArms(this.rightArm, this.leftArm, param3);
        }

    }

    @Override
    public void translateToHand(HumanoidArm param0, PoseStack param1) {
        float var0 = param0 == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        ModelPart var1 = this.getArm(param0);
        var1.x += var0;
        var1.translateAndRotate(param1);
        var1.x -= var0;
    }
}
