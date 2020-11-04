package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PolarBearModel<T extends PolarBear> extends QuadrupedModel<T> {
    public PolarBearModel(ModelPart param0) {
        super(param0, true, 16.0F, 4.0F, 2.25F, 2.0F, 24);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-3.5F, -3.0F, -3.0F, 7.0F, 7.0F, 7.0F)
                .texOffs(0, 44)
                .addBox("mouth", -2.5F, 1.0F, -6.0F, 5.0F, 3.0F, 3.0F)
                .texOffs(26, 0)
                .addBox("right_ear", -4.5F, -4.0F, -1.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(26, 0)
                .mirror()
                .addBox("left_ear", 2.5F, -4.0F, -1.0F, 2.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, 10.0F, -16.0F)
        );
        var1.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(0, 19)
                .addBox(-5.0F, -13.0F, -7.0F, 14.0F, 14.0F, 11.0F)
                .texOffs(39, 0)
                .addBox(-4.0F, -25.0F, -7.0F, 12.0F, 12.0F, 10.0F),
            PartPose.offsetAndRotation(-2.0F, 9.0F, 12.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        int var2 = 10;
        CubeListBuilder var3 = CubeListBuilder.create().texOffs(50, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 8.0F);
        var1.addOrReplaceChild("right_hind_leg", var3, PartPose.offset(-4.5F, 14.0F, 6.0F));
        var1.addOrReplaceChild("left_hind_leg", var3, PartPose.offset(4.5F, 14.0F, 6.0F));
        CubeListBuilder var4 = CubeListBuilder.create().texOffs(50, 40).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 6.0F);
        var1.addOrReplaceChild("right_front_leg", var4, PartPose.offset(-3.5F, 14.0F, -8.0F));
        var1.addOrReplaceChild("left_front_leg", var4, PartPose.offset(3.5F, 14.0F, -8.0F));
        return LayerDefinition.create(var0, 128, 64);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        super.setupAnim(param0, param1, param2, param3, param4, param5);
        float var0 = param3 - (float)param0.tickCount;
        float var1 = param0.getStandingAnimationScale(var0);
        var1 *= var1;
        float var2 = 1.0F - var1;
        this.body.xRot = (float) (Math.PI / 2) - var1 * (float) Math.PI * 0.35F;
        this.body.y = 9.0F * var2 + 11.0F * var1;
        this.rightFrontLeg.y = 14.0F * var2 - 6.0F * var1;
        this.rightFrontLeg.z = -8.0F * var2 - 4.0F * var1;
        this.rightFrontLeg.xRot -= var1 * (float) Math.PI * 0.45F;
        this.leftFrontLeg.y = this.rightFrontLeg.y;
        this.leftFrontLeg.z = this.rightFrontLeg.z;
        this.leftFrontLeg.xRot -= var1 * (float) Math.PI * 0.45F;
        if (this.young) {
            this.head.y = 10.0F * var2 - 9.0F * var1;
            this.head.z = -16.0F * var2 - 7.0F * var1;
        } else {
            this.head.y = 10.0F * var2 - 14.0F * var1;
            this.head.z = -16.0F * var2 - 3.0F * var1;
        }

        this.head.xRot += var1 * (float) Math.PI * 0.15F;
    }
}
