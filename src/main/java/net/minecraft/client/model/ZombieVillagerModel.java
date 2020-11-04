package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombieVillagerModel<T extends Zombie> extends HumanoidModel<T> implements VillagerHeadModel {
    private final ModelPart hatRim = this.hat.getChild("hat_rim");

    public ZombieVillagerModel(ModelPart param0) {
        super(param0);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "head",
            new CubeListBuilder().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F).texOffs(24, 0).addBox(-1.0F, -3.0F, -6.0F, 2.0F, 4.0F, 2.0F),
            PartPose.ZERO
        );
        PartDefinition var2 = var1.addOrReplaceChild(
            "hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.ZERO
        );
        var2.addOrReplaceChild(
            "hat_rim",
            CubeListBuilder.create().texOffs(30, 47).addBox(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F),
            PartPose.rotation((float) (-Math.PI / 2), 0.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(16, 20)
                .addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F)
                .texOffs(0, 38)
                .addBox(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, new CubeDeformation(0.05F)),
            PartPose.ZERO
        );
        var1.addOrReplaceChild(
            "right_arm", CubeListBuilder.create().texOffs(44, 22).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "left_arm", CubeListBuilder.create().texOffs(44, 22).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "right_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-2.0F, 12.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "left_leg", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(2.0F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(var0, 64, 64);
    }

    public static LayerDefinition createArmorLayer(CubeDeformation param0) {
        MeshDefinition var0 = HumanoidModel.createMesh(param0, 0.0F);
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F, 8.0F, param0), PartPose.ZERO);
        var1.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, param0.extend(0.1F)), PartPose.ZERO
        );
        var1.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0.extend(0.1F)),
            PartPose.offset(-2.0F, 12.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0.extend(0.1F)),
            PartPose.offset(2.0F, 12.0F, 0.0F)
        );
        var1.getChild("hat").addOrReplaceChild("hat_rim", CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(var0, 64, 32);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        super.setupAnim(param0, param1, param2, param3, param4, param5);
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, param0.isAggressive(), this.attackTime, param3);
    }

    @Override
    public void hatVisible(boolean param0) {
        this.head.visible = param0;
        this.hat.visible = param0;
        this.hatRim.visible = param0;
    }
}
