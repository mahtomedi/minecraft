package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArmorStandModel extends ArmorStandArmorModel {
    private final ModelPart bodyStick1;
    private final ModelPart bodyStick2;
    private final ModelPart shoulderStick;
    private final ModelPart basePlate;

    public ArmorStandModel() {
        this(0.0F);
    }

    public ArmorStandModel(float param0) {
        super(param0, 64, 64);
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F, param0);
        this.head.setPos(0.0F, 0.0F, 0.0F);
        this.body = new ModelPart(this, 0, 26);
        this.body.addBox(-6.0F, 0.0F, -1.5F, 12.0F, 3.0F, 3.0F, param0);
        this.body.setPos(0.0F, 0.0F, 0.0F);
        this.rightArm = new ModelPart(this, 24, 0);
        this.rightArm.addBox(-2.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, param0);
        this.rightArm.setPos(-5.0F, 2.0F, 0.0F);
        this.leftArm = new ModelPart(this, 32, 16);
        this.leftArm.mirror = true;
        this.leftArm.addBox(0.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, param0);
        this.leftArm.setPos(5.0F, 2.0F, 0.0F);
        this.rightLeg = new ModelPart(this, 8, 0);
        this.rightLeg.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F, param0);
        this.rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        this.leftLeg = new ModelPart(this, 40, 16);
        this.leftLeg.mirror = true;
        this.leftLeg.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F, param0);
        this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
        this.bodyStick1 = new ModelPart(this, 16, 0);
        this.bodyStick1.addBox(-3.0F, 3.0F, -1.0F, 2.0F, 7.0F, 2.0F, param0);
        this.bodyStick1.setPos(0.0F, 0.0F, 0.0F);
        this.bodyStick1.visible = true;
        this.bodyStick2 = new ModelPart(this, 48, 16);
        this.bodyStick2.addBox(1.0F, 3.0F, -1.0F, 2.0F, 7.0F, 2.0F, param0);
        this.bodyStick2.setPos(0.0F, 0.0F, 0.0F);
        this.shoulderStick = new ModelPart(this, 0, 48);
        this.shoulderStick.addBox(-4.0F, 10.0F, -1.0F, 8.0F, 2.0F, 2.0F, param0);
        this.shoulderStick.setPos(0.0F, 0.0F, 0.0F);
        this.basePlate = new ModelPart(this, 0, 32);
        this.basePlate.addBox(-6.0F, 11.0F, -6.0F, 12.0F, 1.0F, 12.0F, param0);
        this.basePlate.setPos(0.0F, 12.0F, 0.0F);
        this.hat.visible = false;
    }

    public void prepareMobModel(ArmorStand param0, float param1, float param2, float param3) {
        this.basePlate.xRot = 0.0F;
        this.basePlate.yRot = (float) (Math.PI / 180.0) * -Mth.rotLerp(param3, param0.yRotO, param0.yRot);
        this.basePlate.zRot = 0.0F;
    }

    @Override
    public void setupAnim(ArmorStand param0, float param1, float param2, float param3, float param4, float param5) {
        super.setupAnim(param0, param1, param2, param3, param4, param5);
        this.leftArm.visible = param0.isShowArms();
        this.rightArm.visible = param0.isShowArms();
        this.basePlate.visible = !param0.isNoBasePlate();
        this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
        this.rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        this.bodyStick1.xRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getX();
        this.bodyStick1.yRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getY();
        this.bodyStick1.zRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getZ();
        this.bodyStick2.xRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getX();
        this.bodyStick2.yRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getY();
        this.bodyStick2.zRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getZ();
        this.shoulderStick.xRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getX();
        this.shoulderStick.yRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getY();
        this.shoulderStick.zRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getZ();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.bodyStick1, this.bodyStick2, this.shoulderStick, this.basePlate));
    }

    @Override
    public void translateToHand(HumanoidArm param0, PoseStack param1) {
        ModelPart var0 = this.getArm(param0);
        boolean var1 = var0.visible;
        var0.visible = true;
        super.translateToHand(param0, param1);
        var0.visible = var1;
    }
}
