package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpiderModel<T extends Entity> extends HierarchicalModel<T> {
    private static final String BODY_0 = "body0";
    private static final String BODY_1 = "body1";
    private static final String RIGHT_MIDDLE_FRONT_LEG = "right_middle_front_leg";
    private static final String LEFT_MIDDLE_FRONT_LEG = "left_middle_front_leg";
    private static final String RIGHT_MIDDLE_HIND_LEG = "right_middle_hind_leg";
    private static final String LEFT_MIDDLE_HIND_LEG = "left_middle_hind_leg";
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightMiddleHindLeg;
    private final ModelPart leftMiddleHindLeg;
    private final ModelPart rightMiddleFrontLeg;
    private final ModelPart leftMiddleFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public SpiderModel(ModelPart param0) {
        this.root = param0;
        this.head = param0.getChild("head");
        this.rightHindLeg = param0.getChild("right_hind_leg");
        this.leftHindLeg = param0.getChild("left_hind_leg");
        this.rightMiddleHindLeg = param0.getChild("right_middle_hind_leg");
        this.leftMiddleHindLeg = param0.getChild("left_middle_hind_leg");
        this.rightMiddleFrontLeg = param0.getChild("right_middle_front_leg");
        this.leftMiddleFrontLeg = param0.getChild("left_middle_front_leg");
        this.rightFrontLeg = param0.getChild("right_front_leg");
        this.leftFrontLeg = param0.getChild("left_front_leg");
    }

    public static LayerDefinition createSpiderBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        int var2 = 15;
        var1.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(32, 4).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 15.0F, -3.0F)
        );
        var1.addOrReplaceChild(
            "body0", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 15.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "body1", CubeListBuilder.create().texOffs(0, 12).addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F), PartPose.offset(0.0F, 15.0F, 9.0F)
        );
        CubeListBuilder var3 = CubeListBuilder.create().texOffs(18, 0).addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);
        CubeListBuilder var4 = CubeListBuilder.create().texOffs(18, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);
        var1.addOrReplaceChild("right_hind_leg", var3, PartPose.offset(-4.0F, 15.0F, 2.0F));
        var1.addOrReplaceChild("left_hind_leg", var4, PartPose.offset(4.0F, 15.0F, 2.0F));
        var1.addOrReplaceChild("right_middle_hind_leg", var3, PartPose.offset(-4.0F, 15.0F, 1.0F));
        var1.addOrReplaceChild("left_middle_hind_leg", var4, PartPose.offset(4.0F, 15.0F, 1.0F));
        var1.addOrReplaceChild("right_middle_front_leg", var3, PartPose.offset(-4.0F, 15.0F, 0.0F));
        var1.addOrReplaceChild("left_middle_front_leg", var4, PartPose.offset(4.0F, 15.0F, 0.0F));
        var1.addOrReplaceChild("right_front_leg", var3, PartPose.offset(-4.0F, 15.0F, -1.0F));
        var1.addOrReplaceChild("left_front_leg", var4, PartPose.offset(4.0F, 15.0F, -1.0F));
        return LayerDefinition.create(var0, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        float var0 = (float) (Math.PI / 4);
        this.rightHindLeg.zRot = (float) (-Math.PI / 4);
        this.leftHindLeg.zRot = (float) (Math.PI / 4);
        this.rightMiddleHindLeg.zRot = -0.58119464F;
        this.leftMiddleHindLeg.zRot = 0.58119464F;
        this.rightMiddleFrontLeg.zRot = -0.58119464F;
        this.leftMiddleFrontLeg.zRot = 0.58119464F;
        this.rightFrontLeg.zRot = (float) (-Math.PI / 4);
        this.leftFrontLeg.zRot = (float) (Math.PI / 4);
        float var1 = -0.0F;
        float var2 = (float) (Math.PI / 8);
        this.rightHindLeg.yRot = (float) (Math.PI / 4);
        this.leftHindLeg.yRot = (float) (-Math.PI / 4);
        this.rightMiddleHindLeg.yRot = (float) (Math.PI / 8);
        this.leftMiddleHindLeg.yRot = (float) (-Math.PI / 8);
        this.rightMiddleFrontLeg.yRot = (float) (-Math.PI / 8);
        this.leftMiddleFrontLeg.yRot = (float) (Math.PI / 8);
        this.rightFrontLeg.yRot = (float) (-Math.PI / 4);
        this.leftFrontLeg.yRot = (float) (Math.PI / 4);
        float var3 = -(Mth.cos(param1 * 0.6662F * 2.0F + 0.0F) * 0.4F) * param2;
        float var4 = -(Mth.cos(param1 * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * param2;
        float var5 = -(Mth.cos(param1 * 0.6662F * 2.0F + (float) (Math.PI / 2)) * 0.4F) * param2;
        float var6 = -(Mth.cos(param1 * 0.6662F * 2.0F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * param2;
        float var7 = Math.abs(Mth.sin(param1 * 0.6662F + 0.0F) * 0.4F) * param2;
        float var8 = Math.abs(Mth.sin(param1 * 0.6662F + (float) Math.PI) * 0.4F) * param2;
        float var9 = Math.abs(Mth.sin(param1 * 0.6662F + (float) (Math.PI / 2)) * 0.4F) * param2;
        float var10 = Math.abs(Mth.sin(param1 * 0.6662F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * param2;
        this.rightHindLeg.yRot += var3;
        this.leftHindLeg.yRot += -var3;
        this.rightMiddleHindLeg.yRot += var4;
        this.leftMiddleHindLeg.yRot += -var4;
        this.rightMiddleFrontLeg.yRot += var5;
        this.leftMiddleFrontLeg.yRot += -var5;
        this.rightFrontLeg.yRot += var6;
        this.leftFrontLeg.yRot += -var6;
        this.rightHindLeg.zRot += var7;
        this.leftHindLeg.zRot += -var7;
        this.rightMiddleHindLeg.zRot += var8;
        this.leftMiddleHindLeg.zRot += -var8;
        this.rightMiddleFrontLeg.zRot += var9;
        this.leftMiddleFrontLeg.zRot += -var9;
        this.rightFrontLeg.zRot += var10;
        this.leftFrontLeg.zRot += -var10;
    }
}
