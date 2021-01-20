package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RabbitModel<T extends Rabbit> extends EntityModel<T> {
    private final ModelPart leftRearFoot;
    private final ModelPart rightRearFoot;
    private final ModelPart leftHaunch;
    private final ModelPart rightHaunch;
    private final ModelPart body;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart head;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart tail;
    private final ModelPart nose;
    private float jumpRotation;

    public RabbitModel(ModelPart param0) {
        this.leftRearFoot = param0.getChild("left_hind_foot");
        this.rightRearFoot = param0.getChild("right_hind_foot");
        this.leftHaunch = param0.getChild("left_haunch");
        this.rightHaunch = param0.getChild("right_haunch");
        this.body = param0.getChild("body");
        this.leftFrontLeg = param0.getChild("left_front_leg");
        this.rightFrontLeg = param0.getChild("right_front_leg");
        this.head = param0.getChild("head");
        this.rightEar = param0.getChild("right_ear");
        this.leftEar = param0.getChild("left_ear");
        this.tail = param0.getChild("tail");
        this.nose = param0.getChild("nose");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "left_hind_foot", CubeListBuilder.create().texOffs(26, 24).addBox(-1.0F, 5.5F, -3.7F, 2.0F, 1.0F, 7.0F), PartPose.offset(3.0F, 17.5F, 3.7F)
        );
        var1.addOrReplaceChild(
            "right_hind_foot", CubeListBuilder.create().texOffs(8, 24).addBox(-1.0F, 5.5F, -3.7F, 2.0F, 1.0F, 7.0F), PartPose.offset(-3.0F, 17.5F, 3.7F)
        );
        var1.addOrReplaceChild(
            "left_haunch",
            CubeListBuilder.create().texOffs(30, 15).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 5.0F),
            PartPose.offsetAndRotation(3.0F, 17.5F, 3.7F, (float) (-Math.PI / 9), 0.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "right_haunch",
            CubeListBuilder.create().texOffs(16, 15).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 5.0F),
            PartPose.offsetAndRotation(-3.0F, 17.5F, 3.7F, (float) (-Math.PI / 9), 0.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.0F, -10.0F, 6.0F, 5.0F, 10.0F),
            PartPose.offsetAndRotation(0.0F, 19.0F, 8.0F, (float) (-Math.PI / 9), 0.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "left_front_leg",
            CubeListBuilder.create().texOffs(8, 15).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F),
            PartPose.offsetAndRotation(3.0F, 17.0F, -1.0F, (float) (-Math.PI / 18), 0.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "right_front_leg",
            CubeListBuilder.create().texOffs(0, 15).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F),
            PartPose.offsetAndRotation(-3.0F, 17.0F, -1.0F, (float) (-Math.PI / 18), 0.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(32, 0).addBox(-2.5F, -4.0F, -5.0F, 5.0F, 4.0F, 5.0F), PartPose.offset(0.0F, 16.0F, -1.0F)
        );
        var1.addOrReplaceChild(
            "right_ear",
            CubeListBuilder.create().texOffs(52, 0).addBox(-2.5F, -9.0F, -1.0F, 2.0F, 5.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, 16.0F, -1.0F, 0.0F, (float) (-Math.PI / 12), 0.0F)
        );
        var1.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create().texOffs(58, 0).addBox(0.5F, -9.0F, -1.0F, 2.0F, 5.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, 16.0F, -1.0F, 0.0F, (float) (Math.PI / 12), 0.0F)
        );
        var1.addOrReplaceChild(
            "tail",
            CubeListBuilder.create().texOffs(52, 6).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, 20.0F, 7.0F, -0.3490659F, 0.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "nose", CubeListBuilder.create().texOffs(32, 9).addBox(-0.5F, -2.5F, -5.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 16.0F, -1.0F)
        );
        return LayerDefinition.create(var0, 64, 32);
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
        if (this.young) {
            float var0 = 1.5F;
            param0.pushPose();
            param0.scale(0.56666666F, 0.56666666F, 0.56666666F);
            param0.translate(0.0, 1.375, 0.125);
            ImmutableList.of(this.head, this.leftEar, this.rightEar, this.nose)
                .forEach(param8 -> param8.render(param0, param1, param2, param3, param4, param5, param6, param7));
            param0.popPose();
            param0.pushPose();
            param0.scale(0.4F, 0.4F, 0.4F);
            param0.translate(0.0, 2.25, 0.0);
            ImmutableList.of(
                    this.leftRearFoot, this.rightRearFoot, this.leftHaunch, this.rightHaunch, this.body, this.leftFrontLeg, this.rightFrontLeg, this.tail
                )
                .forEach(param8 -> param8.render(param0, param1, param2, param3, param4, param5, param6, param7));
            param0.popPose();
        } else {
            param0.pushPose();
            param0.scale(0.6F, 0.6F, 0.6F);
            param0.translate(0.0, 1.0, 0.0);
            ImmutableList.of(
                    this.leftRearFoot,
                    this.rightRearFoot,
                    this.leftHaunch,
                    this.rightHaunch,
                    this.body,
                    this.leftFrontLeg,
                    this.rightFrontLeg,
                    this.head,
                    this.rightEar,
                    this.leftEar,
                    this.tail,
                    this.nose
                )
                .forEach(param8 -> param8.render(param0, param1, param2, param3, param4, param5, param6, param7));
            param0.popPose();
        }

    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = param3 - (float)param0.tickCount;
        this.nose.xRot = param5 * (float) (Math.PI / 180.0);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.rightEar.xRot = param5 * (float) (Math.PI / 180.0);
        this.leftEar.xRot = param5 * (float) (Math.PI / 180.0);
        this.nose.yRot = param4 * (float) (Math.PI / 180.0);
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.rightEar.yRot = this.nose.yRot - (float) (Math.PI / 12);
        this.leftEar.yRot = this.nose.yRot + (float) (Math.PI / 12);
        this.jumpRotation = Mth.sin(param0.getJumpCompletion(var0) * (float) Math.PI);
        this.leftHaunch.xRot = (this.jumpRotation * 50.0F - 21.0F) * (float) (Math.PI / 180.0);
        this.rightHaunch.xRot = (this.jumpRotation * 50.0F - 21.0F) * (float) (Math.PI / 180.0);
        this.leftRearFoot.xRot = this.jumpRotation * 50.0F * (float) (Math.PI / 180.0);
        this.rightRearFoot.xRot = this.jumpRotation * 50.0F * (float) (Math.PI / 180.0);
        this.leftFrontLeg.xRot = (this.jumpRotation * -40.0F - 11.0F) * (float) (Math.PI / 180.0);
        this.rightFrontLeg.xRot = (this.jumpRotation * -40.0F - 11.0F) * (float) (Math.PI / 180.0);
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        super.prepareMobModel(param0, param1, param2, param3);
        this.jumpRotation = Mth.sin(param0.getJumpCompletion(param3) * (float) Math.PI);
    }
}
