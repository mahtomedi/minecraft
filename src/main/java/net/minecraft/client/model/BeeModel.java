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
import net.minecraft.world.entity.animal.Bee;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeeModel<T extends Bee> extends AgeableListModel<T> {
    private final ModelPart bone;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart frontLeg;
    private final ModelPart midLeg;
    private final ModelPart backLeg;
    private final ModelPart stinger;
    private final ModelPart leftAntenna;
    private final ModelPart rightAntenna;
    private float rollAmount;

    public BeeModel(ModelPart param0) {
        super(false, 24.0F, 0.0F);
        this.bone = param0.getChild("bone");
        ModelPart var0 = this.bone.getChild("body");
        this.stinger = var0.getChild("stinger");
        this.leftAntenna = var0.getChild("left_antenna");
        this.rightAntenna = var0.getChild("right_antenna");
        this.rightWing = this.bone.getChild("right_wing");
        this.leftWing = this.bone.getChild("left_wing");
        this.frontLeg = this.bone.getChild("front_legs");
        this.midLeg = this.bone.getChild("middle_legs");
        this.backLeg = this.bone.getChild("back_legs");
    }

    public static LayerDefinition createBodyLayer() {
        float var0 = 19.0F;
        MeshDefinition var1 = new MeshDefinition();
        PartDefinition var2 = var1.getRoot();
        PartDefinition var3 = var2.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 19.0F, 0.0F));
        PartDefinition var4 = var3.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -4.0F, -5.0F, 7.0F, 7.0F, 10.0F), PartPose.ZERO
        );
        var4.addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(26, 7).addBox(0.0F, -1.0F, 5.0F, 0.0F, 1.0F, 2.0F), PartPose.ZERO);
        var4.addOrReplaceChild(
            "left_antenna", CubeListBuilder.create().texOffs(2, 0).addBox(1.5F, -2.0F, -3.0F, 1.0F, 2.0F, 3.0F), PartPose.offset(0.0F, -2.0F, -5.0F)
        );
        var4.addOrReplaceChild(
            "right_antenna", CubeListBuilder.create().texOffs(2, 3).addBox(-2.5F, -2.0F, -3.0F, 1.0F, 2.0F, 3.0F), PartPose.offset(0.0F, -2.0F, -5.0F)
        );
        CubeDeformation var5 = new CubeDeformation(0.001F);
        var3.addOrReplaceChild(
            "right_wing",
            CubeListBuilder.create().texOffs(0, 18).addBox(-9.0F, 0.0F, 0.0F, 9.0F, 0.0F, 6.0F, var5),
            PartPose.offsetAndRotation(-1.5F, -4.0F, -3.0F, 0.0F, -0.2618F, 0.0F)
        );
        var3.addOrReplaceChild(
            "left_wing",
            CubeListBuilder.create().texOffs(0, 18).mirror().addBox(0.0F, 0.0F, 0.0F, 9.0F, 0.0F, 6.0F, var5),
            PartPose.offsetAndRotation(1.5F, -4.0F, -3.0F, 0.0F, 0.2618F, 0.0F)
        );
        var3.addOrReplaceChild(
            "front_legs", CubeListBuilder.create().addBox("front_legs", -5.0F, 0.0F, 0.0F, 7, 2, 0, 26, 1), PartPose.offset(1.5F, 3.0F, -2.0F)
        );
        var3.addOrReplaceChild(
            "middle_legs", CubeListBuilder.create().addBox("middle_legs", -5.0F, 0.0F, 0.0F, 7, 2, 0, 26, 3), PartPose.offset(1.5F, 3.0F, 0.0F)
        );
        var3.addOrReplaceChild("back_legs", CubeListBuilder.create().addBox("back_legs", -5.0F, 0.0F, 0.0F, 7, 2, 0, 26, 5), PartPose.offset(1.5F, 3.0F, 2.0F));
        return LayerDefinition.create(var1, 64, 64);
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        super.prepareMobModel(param0, param1, param2, param3);
        this.rollAmount = param0.getRollAmount(param3);
        this.stinger.visible = !param0.hasStung();
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.rightWing.xRot = 0.0F;
        this.leftAntenna.xRot = 0.0F;
        this.rightAntenna.xRot = 0.0F;
        this.bone.xRot = 0.0F;
        boolean var0 = param0.isOnGround() && param0.getDeltaMovement().lengthSqr() < 1.0E-7;
        if (var0) {
            this.rightWing.yRot = -0.2618F;
            this.rightWing.zRot = 0.0F;
            this.leftWing.xRot = 0.0F;
            this.leftWing.yRot = 0.2618F;
            this.leftWing.zRot = 0.0F;
            this.frontLeg.xRot = 0.0F;
            this.midLeg.xRot = 0.0F;
            this.backLeg.xRot = 0.0F;
        } else {
            float var1 = param3 * 2.1F;
            this.rightWing.yRot = 0.0F;
            this.rightWing.zRot = Mth.cos(var1) * (float) Math.PI * 0.15F;
            this.leftWing.xRot = this.rightWing.xRot;
            this.leftWing.yRot = this.rightWing.yRot;
            this.leftWing.zRot = -this.rightWing.zRot;
            this.frontLeg.xRot = (float) (Math.PI / 4);
            this.midLeg.xRot = (float) (Math.PI / 4);
            this.backLeg.xRot = (float) (Math.PI / 4);
            this.bone.xRot = 0.0F;
            this.bone.yRot = 0.0F;
            this.bone.zRot = 0.0F;
        }

        if (!param0.isAngry()) {
            this.bone.xRot = 0.0F;
            this.bone.yRot = 0.0F;
            this.bone.zRot = 0.0F;
            if (!var0) {
                float var2 = Mth.cos(param3 * 0.18F);
                this.bone.xRot = 0.1F + var2 * (float) Math.PI * 0.025F;
                this.leftAntenna.xRot = var2 * (float) Math.PI * 0.03F;
                this.rightAntenna.xRot = var2 * (float) Math.PI * 0.03F;
                this.frontLeg.xRot = -var2 * (float) Math.PI * 0.1F + (float) (Math.PI / 8);
                this.backLeg.xRot = -var2 * (float) Math.PI * 0.05F + (float) (Math.PI / 4);
                this.bone.y = 19.0F - Mth.cos(param3 * 0.18F) * 0.9F;
            }
        }

        if (this.rollAmount > 0.0F) {
            this.bone.xRot = ModelUtils.rotlerpRad(this.bone.xRot, 3.0915928F, this.rollAmount);
        }

    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.bone);
    }
}
