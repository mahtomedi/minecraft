package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
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
    public DrownedModel(ModelPart param0) {
        super(param0);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation param0) {
        MeshDefinition var0 = HumanoidModel.createMesh(param0, 0.0F);
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0), PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0), PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(var0, 64, 64);
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        this.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        ItemStack var0 = param0.getItemInHand(InteractionHand.MAIN_HAND);
        if (var0.is(Items.TRIDENT) && param0.isAggressive()) {
            if (param0.getMainArm() == HumanoidArm.RIGHT) {
                this.rightArmPose = HumanoidModel.ArmPose.THROW_SPEAR;
            } else {
                this.leftArmPose = HumanoidModel.ArmPose.THROW_SPEAR;
            }
        }

        super.prepareMobModel(param0, param1, param2, param3);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        super.setupAnim(param0, param1, param2, param3, param4, param5);
        if (this.leftArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) Math.PI;
            this.leftArm.yRot = 0.0F;
        }

        if (this.rightArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float) Math.PI;
            this.rightArm.yRot = 0.0F;
        }

        if (this.swimAmount > 0.0F) {
            this.rightArm.xRot = this.rotlerpRad(this.swimAmount, this.rightArm.xRot, (float) (-Math.PI * 4.0 / 5.0))
                + this.swimAmount * 0.35F * Mth.sin(0.1F * param3);
            this.leftArm.xRot = this.rotlerpRad(this.swimAmount, this.leftArm.xRot, (float) (-Math.PI * 4.0 / 5.0))
                - this.swimAmount * 0.35F * Mth.sin(0.1F * param3);
            this.rightArm.zRot = this.rotlerpRad(this.swimAmount, this.rightArm.zRot, -0.15F);
            this.leftArm.zRot = this.rotlerpRad(this.swimAmount, this.leftArm.zRot, 0.15F);
            this.leftLeg.xRot -= this.swimAmount * 0.55F * Mth.sin(0.1F * param3);
            this.rightLeg.xRot += this.swimAmount * 0.55F * Mth.sin(0.1F * param3);
            this.head.xRot = 0.0F;
        }

    }
}
