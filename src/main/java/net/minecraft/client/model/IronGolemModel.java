package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IronGolemModel<T extends IronGolem> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public IronGolemModel(ModelPart param0) {
        this.root = param0;
        this.head = param0.getChild("head");
        this.rightArm = param0.getChild("right_arm");
        this.leftArm = param0.getChild("left_arm");
        this.rightLeg = param0.getChild("right_leg");
        this.leftLeg = param0.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -12.0F, -5.5F, 8.0F, 10.0F, 8.0F).texOffs(24, 0).addBox(-1.0F, -5.0F, -7.5F, 2.0F, 4.0F, 2.0F),
            PartPose.offset(0.0F, -7.0F, -2.0F)
        );
        var1.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(0, 40)
                .addBox(-9.0F, -2.0F, -6.0F, 18.0F, 12.0F, 11.0F)
                .texOffs(0, 70)
                .addBox(-4.5F, 10.0F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.5F)),
            PartPose.offset(0.0F, -7.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "right_arm", CubeListBuilder.create().texOffs(60, 21).addBox(-13.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F), PartPose.offset(0.0F, -7.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "left_arm", CubeListBuilder.create().texOffs(60, 58).addBox(9.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F), PartPose.offset(0.0F, -7.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "right_leg", CubeListBuilder.create().texOffs(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F), PartPose.offset(-4.0F, 11.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "left_leg", CubeListBuilder.create().texOffs(60, 0).mirror().addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F), PartPose.offset(5.0F, 11.0F, 0.0F)
        );
        return LayerDefinition.create(var0, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.rightLeg.xRot = -1.5F * Mth.triangleWave(param1, 13.0F) * param2;
        this.leftLeg.xRot = 1.5F * Mth.triangleWave(param1, 13.0F) * param2;
        this.rightLeg.yRot = 0.0F;
        this.leftLeg.yRot = 0.0F;
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        int var0 = param0.getAttackAnimationTick();
        this.rightArm.resetPose();
        this.leftArm.resetPose();
        if (var0 > 0) {
            this.rightArm.xRot = -2.0F + 1.5F * Mth.triangleWave((float)var0 - param3, 10.0F);
            this.leftArm.xRot = -2.0F + 1.5F * Mth.triangleWave((float)var0 - param3, 10.0F);
        } else {
            int var1 = param0.getOfferFlowerTick();
            if (var1 > 0) {
                this.rightArm.xRot = -0.8F + 0.025F * Mth.triangleWave((float)var1, 70.0F);
                this.leftArm.xRot = 0.0F;
            } else if (param0.isJolly()) {
                this.rightArm.xRot = 3.5F + 1.5F * Mth.triangleWave(param1, 13.0F) * param2;
                this.rightArm.zRot = -0.5F;
                this.rightArm.y = -11.0F;
                this.leftArm.xRot = 3.5F - 1.5F * Mth.triangleWave(param1, 13.0F) * param2;
                this.leftArm.zRot = 0.5F;
                this.leftArm.y = -11.0F;
            } else {
                this.rightArm.xRot = (-0.2F + 1.5F * Mth.triangleWave(param1, 13.0F)) * param2;
                this.leftArm.xRot = (-0.2F - 1.5F * Mth.triangleWave(param1, 13.0F)) * param2;
            }
        }

    }

    public ModelPart getFlowerHoldingArm() {
        return this.rightArm;
    }
}
