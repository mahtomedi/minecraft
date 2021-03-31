package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Fox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FoxModel<T extends Fox> extends AgeableListModel<T> {
    public final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;
    private static final int LEG_SIZE = 6;
    private static final float HEAD_HEIGHT = 16.5F;
    private static final float LEG_POS = 17.5F;
    private float legMotionPos;

    public FoxModel(ModelPart param0) {
        super(true, 8.0F, 3.35F);
        this.head = param0.getChild("head");
        this.body = param0.getChild("body");
        this.rightHindLeg = param0.getChild("right_hind_leg");
        this.leftHindLeg = param0.getChild("left_hind_leg");
        this.rightFrontLeg = param0.getChild("right_front_leg");
        this.leftFrontLeg = param0.getChild("left_front_leg");
        this.tail = this.body.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        PartDefinition var2 = var1.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(1, 5).addBox(-3.0F, -2.0F, -5.0F, 8.0F, 6.0F, 6.0F), PartPose.offset(-1.0F, 16.5F, -3.0F)
        );
        var2.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(8, 1).addBox(-3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F), PartPose.ZERO);
        var2.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(15, 1).addBox(3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F), PartPose.ZERO);
        var2.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(6, 18).addBox(-1.0F, 2.01F, -8.0F, 4.0F, 2.0F, 3.0F), PartPose.ZERO);
        PartDefinition var3 = var1.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(24, 15).addBox(-3.0F, 3.999F, -3.5F, 6.0F, 11.0F, 6.0F),
            PartPose.offsetAndRotation(0.0F, 16.0F, -6.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        CubeDeformation var4 = new CubeDeformation(0.001F);
        CubeListBuilder var5 = CubeListBuilder.create().texOffs(4, 24).addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, var4);
        CubeListBuilder var6 = CubeListBuilder.create().texOffs(13, 24).addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, var4);
        var1.addOrReplaceChild("right_hind_leg", var6, PartPose.offset(-5.0F, 17.5F, 7.0F));
        var1.addOrReplaceChild("left_hind_leg", var5, PartPose.offset(-1.0F, 17.5F, 7.0F));
        var1.addOrReplaceChild("right_front_leg", var6, PartPose.offset(-5.0F, 17.5F, 0.0F));
        var1.addOrReplaceChild("left_front_leg", var5, PartPose.offset(-1.0F, 17.5F, 0.0F));
        var3.addOrReplaceChild(
            "tail",
            CubeListBuilder.create().texOffs(30, 0).addBox(2.0F, 0.0F, -1.0F, 4.0F, 9.0F, 5.0F),
            PartPose.offsetAndRotation(-4.0F, 15.0F, -1.0F, -0.05235988F, 0.0F, 0.0F)
        );
        return LayerDefinition.create(var0, 48, 32);
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        this.body.xRot = (float) (Math.PI / 2);
        this.tail.xRot = -0.05235988F;
        this.rightHindLeg.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
        this.leftHindLeg.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.rightFrontLeg.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.leftFrontLeg.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
        this.head.setPos(-1.0F, 16.5F, -3.0F);
        this.head.yRot = 0.0F;
        this.head.zRot = param0.getHeadRollAngle(param3);
        this.rightHindLeg.visible = true;
        this.leftHindLeg.visible = true;
        this.rightFrontLeg.visible = true;
        this.leftFrontLeg.visible = true;
        this.body.setPos(0.0F, 16.0F, -6.0F);
        this.body.zRot = 0.0F;
        this.rightHindLeg.setPos(-5.0F, 17.5F, 7.0F);
        this.leftHindLeg.setPos(-1.0F, 17.5F, 7.0F);
        if (param0.isCrouching()) {
            this.body.xRot = 1.6755161F;
            float var0 = param0.getCrouchAmount(param3);
            this.body.setPos(0.0F, 16.0F + param0.getCrouchAmount(param3), -6.0F);
            this.head.setPos(-1.0F, 16.5F + var0, -3.0F);
            this.head.yRot = 0.0F;
        } else if (param0.isSleeping()) {
            this.body.zRot = (float) (-Math.PI / 2);
            this.body.setPos(0.0F, 21.0F, -6.0F);
            this.tail.xRot = (float) (-Math.PI * 5.0 / 6.0);
            if (this.young) {
                this.tail.xRot = -2.1816616F;
                this.body.setPos(0.0F, 21.0F, -2.0F);
            }

            this.head.setPos(1.0F, 19.49F, -3.0F);
            this.head.xRot = 0.0F;
            this.head.yRot = (float) (-Math.PI * 2.0 / 3.0);
            this.head.zRot = 0.0F;
            this.rightHindLeg.visible = false;
            this.leftHindLeg.visible = false;
            this.rightFrontLeg.visible = false;
            this.leftFrontLeg.visible = false;
        } else if (param0.isSitting()) {
            this.body.xRot = (float) (Math.PI / 6);
            this.body.setPos(0.0F, 9.0F, -3.0F);
            this.tail.xRot = (float) (Math.PI / 4);
            this.tail.setPos(-4.0F, 15.0F, -2.0F);
            this.head.setPos(-1.0F, 10.0F, -0.25F);
            this.head.xRot = 0.0F;
            this.head.yRot = 0.0F;
            if (this.young) {
                this.head.setPos(-1.0F, 13.0F, -3.75F);
            }

            this.rightHindLeg.xRot = (float) (-Math.PI * 5.0 / 12.0);
            this.rightHindLeg.setPos(-5.0F, 21.5F, 6.75F);
            this.leftHindLeg.xRot = (float) (-Math.PI * 5.0 / 12.0);
            this.leftHindLeg.setPos(-1.0F, 21.5F, 6.75F);
            this.rightFrontLeg.xRot = (float) (-Math.PI / 12);
            this.leftFrontLeg.xRot = (float) (-Math.PI / 12);
        }

    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        if (!param0.isSleeping() && !param0.isFaceplanted() && !param0.isCrouching()) {
            this.head.xRot = param5 * (float) (Math.PI / 180.0);
            this.head.yRot = param4 * (float) (Math.PI / 180.0);
        }

        if (param0.isSleeping()) {
            this.head.xRot = 0.0F;
            this.head.yRot = (float) (-Math.PI * 2.0 / 3.0);
            this.head.zRot = Mth.cos(param3 * 0.027F) / 22.0F;
        }

        if (param0.isCrouching()) {
            float var0 = Mth.cos(param3) * 0.01F;
            this.body.yRot = var0;
            this.rightHindLeg.zRot = var0;
            this.leftHindLeg.zRot = var0;
            this.rightFrontLeg.zRot = var0 / 2.0F;
            this.leftFrontLeg.zRot = var0 / 2.0F;
        }

        if (param0.isFaceplanted()) {
            float var1 = 0.1F;
            this.legMotionPos += 0.67F;
            this.rightHindLeg.xRot = Mth.cos(this.legMotionPos * 0.4662F) * 0.1F;
            this.leftHindLeg.xRot = Mth.cos(this.legMotionPos * 0.4662F + (float) Math.PI) * 0.1F;
            this.rightFrontLeg.xRot = Mth.cos(this.legMotionPos * 0.4662F + (float) Math.PI) * 0.1F;
            this.leftFrontLeg.xRot = Mth.cos(this.legMotionPos * 0.4662F) * 0.1F;
        }

    }
}
