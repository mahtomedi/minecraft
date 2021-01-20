package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArmorStandModel extends ArmorStandArmorModel {
    private final ModelPart rightBodyStick;
    private final ModelPart leftBodyStick;
    private final ModelPart shoulderStick;
    private final ModelPart basePlate;

    public ArmorStandModel(ModelPart param0) {
        super(param0);
        this.rightBodyStick = param0.getChild("right_body_stick");
        this.leftBodyStick = param0.getChild("left_body_stick");
        this.shoulderStick = param0.getChild("shoulder_stick");
        this.basePlate = param0.getChild("base_plate");
        this.hat.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.offset(0.0F, 1.0F, 0.0F));
        var1.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 26).addBox(-6.0F, 0.0F, -1.5F, 12.0F, 3.0F, 3.0F), PartPose.ZERO);
        var1.addOrReplaceChild(
            "right_arm", CubeListBuilder.create().texOffs(24, 0).addBox(-2.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "left_arm", CubeListBuilder.create().texOffs(32, 16).mirror().addBox(0.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "right_leg", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F), PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "left_leg", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F), PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        var1.addOrReplaceChild("right_body_stick", CubeListBuilder.create().texOffs(16, 0).addBox(-3.0F, 3.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.ZERO);
        var1.addOrReplaceChild("left_body_stick", CubeListBuilder.create().texOffs(48, 16).addBox(1.0F, 3.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.ZERO);
        var1.addOrReplaceChild("shoulder_stick", CubeListBuilder.create().texOffs(0, 48).addBox(-4.0F, 10.0F, -1.0F, 8.0F, 2.0F, 2.0F), PartPose.ZERO);
        var1.addOrReplaceChild(
            "base_plate", CubeListBuilder.create().texOffs(0, 32).addBox(-6.0F, 11.0F, -6.0F, 12.0F, 1.0F, 12.0F), PartPose.offset(0.0F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(var0, 64, 64);
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
        this.rightBodyStick.xRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getX();
        this.rightBodyStick.yRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getY();
        this.rightBodyStick.zRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getZ();
        this.leftBodyStick.xRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getX();
        this.leftBodyStick.yRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getY();
        this.leftBodyStick.zRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getZ();
        this.shoulderStick.xRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getX();
        this.shoulderStick.yRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getY();
        this.shoulderStick.zRot = (float) (Math.PI / 180.0) * param0.getBodyPose().getZ();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.rightBodyStick, this.leftBodyStick, this.shoulderStick, this.basePlate));
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
