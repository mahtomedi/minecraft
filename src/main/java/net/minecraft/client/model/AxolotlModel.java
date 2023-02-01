package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LerpingModel;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class AxolotlModel<T extends Axolotl & LerpingModel> extends AgeableListModel<T> {
    public static final float SWIMMING_LEG_XROT = 1.8849558F;
    private final ModelPart tail;
    private final ModelPart leftHindLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart topGills;
    private final ModelPart leftGills;
    private final ModelPart rightGills;

    public AxolotlModel(ModelPart param0) {
        super(true, 8.0F, 3.35F);
        this.body = param0.getChild("body");
        this.head = this.body.getChild("head");
        this.rightHindLeg = this.body.getChild("right_hind_leg");
        this.leftHindLeg = this.body.getChild("left_hind_leg");
        this.rightFrontLeg = this.body.getChild("right_front_leg");
        this.leftFrontLeg = this.body.getChild("left_front_leg");
        this.tail = this.body.getChild("tail");
        this.topGills = this.head.getChild("top_gills");
        this.leftGills = this.head.getChild("left_gills");
        this.rightGills = this.head.getChild("right_gills");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        PartDefinition var2 = var1.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(0, 11).addBox(-4.0F, -2.0F, -9.0F, 8.0F, 4.0F, 10.0F).texOffs(2, 17).addBox(0.0F, -3.0F, -8.0F, 0.0F, 5.0F, 9.0F),
            PartPose.offset(0.0F, 20.0F, 5.0F)
        );
        CubeDeformation var3 = new CubeDeformation(0.001F);
        PartDefinition var4 = var2.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 1).addBox(-4.0F, -3.0F, -5.0F, 8.0F, 5.0F, 5.0F, var3), PartPose.offset(0.0F, 0.0F, -9.0F)
        );
        CubeListBuilder var5 = CubeListBuilder.create().texOffs(3, 37).addBox(-4.0F, -3.0F, 0.0F, 8.0F, 3.0F, 0.0F, var3);
        CubeListBuilder var6 = CubeListBuilder.create().texOffs(0, 40).addBox(-3.0F, -5.0F, 0.0F, 3.0F, 7.0F, 0.0F, var3);
        CubeListBuilder var7 = CubeListBuilder.create().texOffs(11, 40).addBox(0.0F, -5.0F, 0.0F, 3.0F, 7.0F, 0.0F, var3);
        var4.addOrReplaceChild("top_gills", var5, PartPose.offset(0.0F, -3.0F, -1.0F));
        var4.addOrReplaceChild("left_gills", var6, PartPose.offset(-4.0F, 0.0F, -1.0F));
        var4.addOrReplaceChild("right_gills", var7, PartPose.offset(4.0F, 0.0F, -1.0F));
        CubeListBuilder var8 = CubeListBuilder.create().texOffs(2, 13).addBox(-1.0F, 0.0F, 0.0F, 3.0F, 5.0F, 0.0F, var3);
        CubeListBuilder var9 = CubeListBuilder.create().texOffs(2, 13).addBox(-2.0F, 0.0F, 0.0F, 3.0F, 5.0F, 0.0F, var3);
        var2.addOrReplaceChild("right_hind_leg", var9, PartPose.offset(-3.5F, 1.0F, -1.0F));
        var2.addOrReplaceChild("left_hind_leg", var8, PartPose.offset(3.5F, 1.0F, -1.0F));
        var2.addOrReplaceChild("right_front_leg", var9, PartPose.offset(-3.5F, 1.0F, -8.0F));
        var2.addOrReplaceChild("left_front_leg", var8, PartPose.offset(3.5F, 1.0F, -8.0F));
        var2.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(2, 19).addBox(0.0F, -3.0F, 0.0F, 0.0F, 5.0F, 12.0F), PartPose.offset(0.0F, 0.0F, 1.0F));
        return LayerDefinition.create(var0, 64, 64);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.setupInitialAnimationValues(param0, param4, param5);
        if (param0.isPlayingDead()) {
            this.setupPlayDeadAnimation(param4);
            this.saveAnimationValues(param0);
        } else {
            boolean var0 = param2 > 1.0E-5F || param0.getXRot() != param0.xRotO || param0.getYRot() != param0.yRotO;
            if (param0.isInWaterOrBubble()) {
                if (var0) {
                    this.setupSwimmingAnimation(param3, param5);
                } else {
                    this.setupWaterHoveringAnimation(param3);
                }

                this.saveAnimationValues(param0);
            } else {
                if (param0.isOnGround()) {
                    if (var0) {
                        this.setupGroundCrawlingAnimation(param3, param4);
                    } else {
                        this.setupLayStillOnGroundAnimation(param3, param4);
                    }
                }

                this.saveAnimationValues(param0);
            }
        }
    }

    private void saveAnimationValues(T param0) {
        Map<String, Vector3f> var0 = param0.getModelRotationValues();
        var0.put("body", this.getRotationVector(this.body));
        var0.put("head", this.getRotationVector(this.head));
        var0.put("right_hind_leg", this.getRotationVector(this.rightHindLeg));
        var0.put("left_hind_leg", this.getRotationVector(this.leftHindLeg));
        var0.put("right_front_leg", this.getRotationVector(this.rightFrontLeg));
        var0.put("left_front_leg", this.getRotationVector(this.leftFrontLeg));
        var0.put("tail", this.getRotationVector(this.tail));
        var0.put("top_gills", this.getRotationVector(this.topGills));
        var0.put("left_gills", this.getRotationVector(this.leftGills));
        var0.put("right_gills", this.getRotationVector(this.rightGills));
    }

    private Vector3f getRotationVector(ModelPart param0) {
        return new Vector3f(param0.xRot, param0.yRot, param0.zRot);
    }

    private void setRotationFromVector(ModelPart param0, Vector3f param1) {
        param0.setRotation(param1.x(), param1.y(), param1.z());
    }

    private void setupInitialAnimationValues(T param0, float param1, float param2) {
        this.body.x = 0.0F;
        this.head.y = 0.0F;
        this.body.y = 20.0F;
        Map<String, Vector3f> var0 = param0.getModelRotationValues();
        if (var0.isEmpty()) {
            this.body.setRotation(param2 * (float) (Math.PI / 180.0), param1 * (float) (Math.PI / 180.0), 0.0F);
            this.head.setRotation(0.0F, 0.0F, 0.0F);
            this.leftHindLeg.setRotation(0.0F, 0.0F, 0.0F);
            this.rightHindLeg.setRotation(0.0F, 0.0F, 0.0F);
            this.leftFrontLeg.setRotation(0.0F, 0.0F, 0.0F);
            this.rightFrontLeg.setRotation(0.0F, 0.0F, 0.0F);
            this.leftGills.setRotation(0.0F, 0.0F, 0.0F);
            this.rightGills.setRotation(0.0F, 0.0F, 0.0F);
            this.topGills.setRotation(0.0F, 0.0F, 0.0F);
            this.tail.setRotation(0.0F, 0.0F, 0.0F);
        } else {
            this.setRotationFromVector(this.body, var0.get("body"));
            this.setRotationFromVector(this.head, var0.get("head"));
            this.setRotationFromVector(this.leftHindLeg, var0.get("left_hind_leg"));
            this.setRotationFromVector(this.rightHindLeg, var0.get("right_hind_leg"));
            this.setRotationFromVector(this.leftFrontLeg, var0.get("left_front_leg"));
            this.setRotationFromVector(this.rightFrontLeg, var0.get("right_front_leg"));
            this.setRotationFromVector(this.leftGills, var0.get("left_gills"));
            this.setRotationFromVector(this.rightGills, var0.get("right_gills"));
            this.setRotationFromVector(this.topGills, var0.get("top_gills"));
            this.setRotationFromVector(this.tail, var0.get("tail"));
        }

    }

    private float lerpTo(float param0, float param1) {
        return this.lerpTo(0.05F, param0, param1);
    }

    private float lerpTo(float param0, float param1, float param2) {
        return Mth.rotLerp(param0, param1, param2);
    }

    private void lerpPart(ModelPart param0, float param1, float param2, float param3) {
        param0.setRotation(this.lerpTo(param0.xRot, param1), this.lerpTo(param0.yRot, param2), this.lerpTo(param0.zRot, param3));
    }

    private void setupLayStillOnGroundAnimation(float param0, float param1) {
        float var0 = param0 * 0.09F;
        float var1 = Mth.sin(var0);
        float var2 = Mth.cos(var0);
        float var3 = var1 * var1 - 2.0F * var1;
        float var4 = var2 * var2 - 3.0F * var1;
        this.head.xRot = this.lerpTo(this.head.xRot, -0.09F * var3);
        this.head.yRot = this.lerpTo(this.head.yRot, 0.0F);
        this.head.zRot = this.lerpTo(this.head.zRot, -0.2F);
        this.tail.yRot = this.lerpTo(this.tail.yRot, -0.1F + 0.1F * var3);
        this.topGills.xRot = this.lerpTo(this.topGills.xRot, 0.6F + 0.05F * var4);
        this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, -this.topGills.xRot);
        this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
        this.lerpPart(this.leftHindLeg, 1.1F, 1.0F, 0.0F);
        this.lerpPart(this.leftFrontLeg, 0.8F, 2.3F, -0.5F);
        this.applyMirrorLegRotations();
        this.body.xRot = this.lerpTo(0.2F, this.body.xRot, 0.0F);
        this.body.yRot = this.lerpTo(this.body.yRot, param1 * (float) (Math.PI / 180.0));
        this.body.zRot = this.lerpTo(this.body.zRot, 0.0F);
    }

    private void setupGroundCrawlingAnimation(float param0, float param1) {
        float var0 = param0 * 0.11F;
        float var1 = Mth.cos(var0);
        float var2 = (var1 * var1 - 2.0F * var1) / 5.0F;
        float var3 = 0.7F * var1;
        this.head.xRot = this.lerpTo(this.head.xRot, 0.0F);
        this.head.yRot = this.lerpTo(this.head.yRot, 0.09F * var1);
        this.head.zRot = this.lerpTo(this.head.zRot, 0.0F);
        this.tail.yRot = this.lerpTo(this.tail.yRot, this.head.yRot);
        this.topGills.xRot = this.lerpTo(this.topGills.xRot, 0.6F - 0.08F * (var1 * var1 + 2.0F * Mth.sin(var0)));
        this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, -this.topGills.xRot);
        this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
        this.lerpPart(this.leftHindLeg, 0.9424779F, 1.5F - var2, -0.1F);
        this.lerpPart(this.leftFrontLeg, 1.0995574F, (float) (Math.PI / 2) - var3, 0.0F);
        this.lerpPart(this.rightHindLeg, this.leftHindLeg.xRot, -1.0F - var2, 0.0F);
        this.lerpPart(this.rightFrontLeg, this.leftFrontLeg.xRot, (float) (-Math.PI / 2) - var3, 0.0F);
        this.body.xRot = this.lerpTo(0.2F, this.body.xRot, 0.0F);
        this.body.yRot = this.lerpTo(this.body.yRot, param1 * (float) (Math.PI / 180.0));
        this.body.zRot = this.lerpTo(this.body.zRot, 0.0F);
    }

    private void setupWaterHoveringAnimation(float param0) {
        float var0 = param0 * 0.075F;
        float var1 = Mth.cos(var0);
        float var2 = Mth.sin(var0) * 0.15F;
        this.body.xRot = this.lerpTo(this.body.xRot, -0.15F + 0.075F * var1);
        this.body.y -= var2;
        this.head.xRot = this.lerpTo(this.head.xRot, -this.body.xRot);
        this.topGills.xRot = this.lerpTo(this.topGills.xRot, 0.2F * var1);
        this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, -0.3F * var1 - 0.19F);
        this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
        this.lerpPart(this.leftHindLeg, (float) (Math.PI * 3.0 / 4.0) - var1 * 0.11F, 0.47123894F, 1.7278761F);
        this.lerpPart(this.leftFrontLeg, (float) (Math.PI / 4) - var1 * 0.2F, 2.042035F, 0.0F);
        this.applyMirrorLegRotations();
        this.tail.yRot = this.lerpTo(this.tail.yRot, 0.5F * var1);
        this.head.yRot = this.lerpTo(this.head.yRot, 0.0F);
        this.head.zRot = this.lerpTo(this.head.zRot, 0.0F);
    }

    private void setupSwimmingAnimation(float param0, float param1) {
        float var0 = param0 * 0.33F;
        float var1 = Mth.sin(var0);
        float var2 = Mth.cos(var0);
        float var3 = 0.13F * var1;
        this.body.xRot = this.lerpTo(0.1F, this.body.xRot, param1 * (float) (Math.PI / 180.0) + var3);
        this.head.xRot = -var3 * 1.8F;
        this.body.y -= 0.45F * var2;
        this.topGills.xRot = this.lerpTo(this.topGills.xRot, -0.5F * var1 - 0.8F);
        this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, 0.3F * var1 + 0.9F);
        this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
        this.tail.yRot = this.lerpTo(this.tail.yRot, 0.3F * Mth.cos(var0 * 0.9F));
        this.lerpPart(this.leftHindLeg, 1.8849558F, -0.4F * var1, (float) (Math.PI / 2));
        this.lerpPart(this.leftFrontLeg, 1.8849558F, -0.2F * var2 - 0.1F, (float) (Math.PI / 2));
        this.applyMirrorLegRotations();
        this.head.yRot = this.lerpTo(this.head.yRot, 0.0F);
        this.head.zRot = this.lerpTo(this.head.zRot, 0.0F);
    }

    private void setupPlayDeadAnimation(float param0) {
        this.lerpPart(this.leftHindLeg, 1.4137167F, 1.0995574F, (float) (Math.PI / 4));
        this.lerpPart(this.leftFrontLeg, (float) (Math.PI / 4), 2.042035F, 0.0F);
        this.body.xRot = this.lerpTo(this.body.xRot, -0.15F);
        this.body.zRot = this.lerpTo(this.body.zRot, 0.35F);
        this.applyMirrorLegRotations();
        this.body.yRot = this.lerpTo(this.body.yRot, param0 * (float) (Math.PI / 180.0));
        this.head.xRot = this.lerpTo(this.head.xRot, 0.0F);
        this.head.yRot = this.lerpTo(this.head.yRot, 0.0F);
        this.head.zRot = this.lerpTo(this.head.zRot, 0.0F);
        this.tail.yRot = this.lerpTo(this.tail.yRot, 0.0F);
        this.lerpPart(this.topGills, 0.0F, 0.0F, 0.0F);
        this.lerpPart(this.leftGills, 0.0F, 0.0F, 0.0F);
        this.lerpPart(this.rightGills, 0.0F, 0.0F, 0.0F);
    }

    private void applyMirrorLegRotations() {
        this.lerpPart(this.rightHindLeg, this.leftHindLeg.xRot, -this.leftHindLeg.yRot, -this.leftHindLeg.zRot);
        this.lerpPart(this.rightFrontLeg, this.leftFrontLeg.xRot, -this.leftFrontLeg.yRot, -this.leftFrontLeg.zRot);
    }
}
