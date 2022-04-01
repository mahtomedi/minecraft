package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class QuadrupedModel<T extends Entity> extends AgeableListModel<T> implements HeadedModel {
    protected final ModelPart head;
    protected final ModelPart body;
    protected final ModelPart rightHindLeg;
    protected final ModelPart leftHindLeg;
    protected final ModelPart rightFrontLeg;
    protected final ModelPart leftFrontLeg;

    protected QuadrupedModel(ModelPart param0, boolean param1, float param2, float param3, float param4, float param5, int param6) {
        super(param1, param2, param3, param4, param5, (float)param6);
        this.head = param0.getChild("head");
        this.body = param0.getChild("body");
        this.rightHindLeg = param0.getChild("right_hind_leg");
        this.leftHindLeg = param0.getChild("left_hind_leg");
        this.rightFrontLeg = param0.getChild("right_front_leg");
        this.leftFrontLeg = param0.getChild("left_front_leg");
    }

    public static MeshDefinition createBodyMesh(int param0, CubeDeformation param1) {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, param1),
            PartPose.offset(0.0F, (float)(18 - param0), -6.0F)
        );
        var1.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(28, 8).addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F, param1),
            PartPose.offsetAndRotation(0.0F, (float)(17 - param0), 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        CubeListBuilder var2 = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, (float)param0, 4.0F, param1);
        var1.addOrReplaceChild("right_hind_leg", var2, PartPose.offset(-3.0F, (float)(24 - param0), 7.0F));
        var1.addOrReplaceChild("left_hind_leg", var2, PartPose.offset(3.0F, (float)(24 - param0), 7.0F));
        var1.addOrReplaceChild("right_front_leg", var2, PartPose.offset(-3.0F, (float)(24 - param0), -5.0F));
        var1.addOrReplaceChild("left_front_leg", var2, PartPose.offset(3.0F, (float)(24 - param0), -5.0F));
        return var0;
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.rightHindLeg.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
        this.leftHindLeg.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.rightFrontLeg.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.leftFrontLeg.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }
}
