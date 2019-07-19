package net.minecraft.client.model;

import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArmorStandArmorModel extends HumanoidModel<ArmorStand> {
    public ArmorStandArmorModel() {
        this(0.0F);
    }

    public ArmorStandArmorModel(float param0) {
        this(param0, 64, 32);
    }

    protected ArmorStandArmorModel(float param0, int param1, int param2) {
        super(param0, 0.0F, param1, param2);
    }

    public void setupAnim(ArmorStand param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.head.xRot = (float) (Math.PI / 180.0) * param0.getHeadPose().getX();
        this.head.yRot = (float) (Math.PI / 180.0) * param0.getHeadPose().getY();
        this.head.zRot = (float) (Math.PI / 180.0) * param0.getHeadPose().getZ();
        this.head.setPos(0.0F, 1.0F, 0.0F);
        this.body.xRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getX();
        this.body.yRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getY();
        this.body.zRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getZ();
        this.leftArm.xRot = (float) (Math.PI / 180.0) * param0.getLeftArmPose().getX();
        this.leftArm.yRot = (float) (Math.PI / 180.0) * param0.getLeftArmPose().getY();
        this.leftArm.zRot = (float) (Math.PI / 180.0) * param0.getLeftArmPose().getZ();
        this.rightArm.xRot = (float) (Math.PI / 180.0) * param0.getRightArmPose().getX();
        this.rightArm.yRot = (float) (Math.PI / 180.0) * param0.getRightArmPose().getY();
        this.rightArm.zRot = (float) (Math.PI / 180.0) * param0.getRightArmPose().getZ();
        this.leftLeg.xRot = (float) (Math.PI / 180.0) * param0.getLeftLegPose().getX();
        this.leftLeg.yRot = (float) (Math.PI / 180.0) * param0.getLeftLegPose().getY();
        this.leftLeg.zRot = (float) (Math.PI / 180.0) * param0.getLeftLegPose().getZ();
        this.leftLeg.setPos(1.9F, 11.0F, 0.0F);
        this.rightLeg.xRot = (float) (Math.PI / 180.0) * param0.getRightLegPose().getX();
        this.rightLeg.yRot = (float) (Math.PI / 180.0) * param0.getRightLegPose().getY();
        this.rightLeg.zRot = (float) (Math.PI / 180.0) * param0.getRightLegPose().getZ();
        this.rightLeg.setPos(-1.9F, 11.0F, 0.0F);
        this.hat.copyFrom(this.head);
    }
}
