package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Panda;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PandaModel<T extends Panda> extends QuadrupedModel<T> {
    private float sitAmount;
    private float lieOnBackAmount;
    private float rollAmount;

    public PandaModel(ModelPart param0) {
        super(param0, true, 23.0F, 4.8F, 2.7F, 3.0F, 49);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .texOffs(0, 6)
                .addBox(-6.5F, -5.0F, -4.0F, 13.0F, 10.0F, 9.0F)
                .texOffs(45, 16)
                .addBox("nose", -3.5F, 0.0F, -6.0F, 7.0F, 5.0F, 2.0F)
                .texOffs(52, 25)
                .addBox("left_ear", 3.5F, -8.0F, -1.0F, 5.0F, 4.0F, 1.0F)
                .texOffs(52, 25)
                .addBox("right_ear", -8.5F, -8.0F, -1.0F, 5.0F, 4.0F, 1.0F),
            PartPose.offset(0.0F, 11.5F, -17.0F)
        );
        var1.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(0, 25).addBox(-9.5F, -13.0F, -6.5F, 19.0F, 26.0F, 13.0F),
            PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        int var2 = 9;
        int var3 = 6;
        CubeListBuilder var4 = CubeListBuilder.create().texOffs(40, 0).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 9.0F, 6.0F);
        var1.addOrReplaceChild("right_hind_leg", var4, PartPose.offset(-5.5F, 15.0F, 9.0F));
        var1.addOrReplaceChild("left_hind_leg", var4, PartPose.offset(5.5F, 15.0F, 9.0F));
        var1.addOrReplaceChild("right_front_leg", var4, PartPose.offset(-5.5F, 15.0F, -9.0F));
        var1.addOrReplaceChild("left_front_leg", var4, PartPose.offset(5.5F, 15.0F, -9.0F));
        return LayerDefinition.create(var0, 64, 64);
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        super.prepareMobModel(param0, param1, param2, param3);
        this.sitAmount = param0.getSitAmount(param3);
        this.lieOnBackAmount = param0.getLieOnBackAmount(param3);
        this.rollAmount = param0.isBaby() ? 0.0F : param0.getRollAmount(param3);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        super.setupAnim(param0, param1, param2, param3, param4, param5);
        boolean var0 = param0.getUnhappyCounter() > 0;
        boolean var1 = param0.isSneezing();
        int var2 = param0.getSneezeCounter();
        boolean var3 = param0.isEating();
        boolean var4 = param0.isScared();
        if (var0) {
            this.head.yRot = 0.35F * Mth.sin(0.6F * param3);
            this.head.zRot = 0.35F * Mth.sin(0.6F * param3);
            this.rightFrontLeg.xRot = -0.75F * Mth.sin(0.3F * param3);
            this.leftFrontLeg.xRot = 0.75F * Mth.sin(0.3F * param3);
        } else {
            this.head.zRot = 0.0F;
        }

        if (var1) {
            if (var2 < 15) {
                this.head.xRot = (float) (-Math.PI / 4) * (float)var2 / 14.0F;
            } else if (var2 < 20) {
                float var5 = (float)((var2 - 15) / 5);
                this.head.xRot = (float) (-Math.PI / 4) + (float) (Math.PI / 4) * var5;
            }
        }

        if (this.sitAmount > 0.0F) {
            this.body.xRot = ModelUtils.rotlerpRad(this.body.xRot, 1.7407963F, this.sitAmount);
            this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, (float) (Math.PI / 2), this.sitAmount);
            this.rightFrontLeg.zRot = -0.27079642F;
            this.leftFrontLeg.zRot = 0.27079642F;
            this.rightHindLeg.zRot = 0.5707964F;
            this.leftHindLeg.zRot = -0.5707964F;
            if (var3) {
                this.head.xRot = (float) (Math.PI / 2) + 0.2F * Mth.sin(param3 * 0.6F);
                this.rightFrontLeg.xRot = -0.4F - 0.2F * Mth.sin(param3 * 0.6F);
                this.leftFrontLeg.xRot = -0.4F - 0.2F * Mth.sin(param3 * 0.6F);
            }

            if (var4) {
                this.head.xRot = 2.1707964F;
                this.rightFrontLeg.xRot = -0.9F;
                this.leftFrontLeg.xRot = -0.9F;
            }
        } else {
            this.rightHindLeg.zRot = 0.0F;
            this.leftHindLeg.zRot = 0.0F;
            this.rightFrontLeg.zRot = 0.0F;
            this.leftFrontLeg.zRot = 0.0F;
        }

        if (this.lieOnBackAmount > 0.0F) {
            this.rightHindLeg.xRot = -0.6F * Mth.sin(param3 * 0.15F);
            this.leftHindLeg.xRot = 0.6F * Mth.sin(param3 * 0.15F);
            this.rightFrontLeg.xRot = 0.3F * Mth.sin(param3 * 0.25F);
            this.leftFrontLeg.xRot = -0.3F * Mth.sin(param3 * 0.25F);
            this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, (float) (Math.PI / 2), this.lieOnBackAmount);
        }

        if (this.rollAmount > 0.0F) {
            this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, 2.0561945F, this.rollAmount);
            this.rightHindLeg.xRot = -0.5F * Mth.sin(param3 * 0.5F);
            this.leftHindLeg.xRot = 0.5F * Mth.sin(param3 * 0.5F);
            this.rightFrontLeg.xRot = 0.5F * Mth.sin(param3 * 0.5F);
            this.leftFrontLeg.xRot = -0.5F * Mth.sin(param3 * 0.5F);
        }

    }
}
