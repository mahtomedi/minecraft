package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArmorStandArmorModel extends HumanoidModel<ArmorStand> {
    public ArmorStandArmorModel(ModelPart param0) {
        super(param0);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation param0) {
        MeshDefinition var0 = HumanoidModel.createMesh(param0, 0.0F);
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, param0), PartPose.offset(0.0F, 1.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "hat",
            CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, param0.extend(0.5F)),
            PartPose.offset(0.0F, 1.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0.extend(-0.1F)),
            PartPose.offset(-1.9F, 11.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0.extend(-0.1F)),
            PartPose.offset(1.9F, 11.0F, 0.0F)
        );
        return LayerDefinition.create(var0, 64, 32);
    }

    public void setupAnim(ArmorStand param0, float param1, float param2, float param3, float param4, float param5) {
        this.head.xRot = (float) (Math.PI / 180.0) * param0.getHeadPose().getX();
        this.head.yRot = (float) (Math.PI / 180.0) * param0.getHeadPose().getY();
        this.head.zRot = (float) (Math.PI / 180.0) * param0.getHeadPose().getZ();
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
        this.rightLeg.xRot = (float) (Math.PI / 180.0) * param0.getRightLegPose().getX();
        this.rightLeg.yRot = (float) (Math.PI / 180.0) * param0.getRightLegPose().getY();
        this.rightLeg.zRot = (float) (Math.PI / 180.0) * param0.getRightLegPose().getZ();
        this.hat.copyFrom(this.head);
    }
}
