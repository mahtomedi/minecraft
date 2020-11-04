package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreeperModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public CreeperModel(ModelPart param0) {
        this.root = param0;
        this.head = param0.getChild("head");
        this.leftHindLeg = param0.getChild("right_hind_leg");
        this.rightHindLeg = param0.getChild("left_hind_leg");
        this.leftFrontLeg = param0.getChild("right_front_leg");
        this.rightFrontLeg = param0.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer(CubeDeformation param0) {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, param0), PartPose.offset(0.0F, 6.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, param0), PartPose.offset(0.0F, 6.0F, 0.0F)
        );
        CubeListBuilder var2 = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, param0);
        var1.addOrReplaceChild("right_hind_leg", var2, PartPose.offset(-2.0F, 18.0F, 4.0F));
        var1.addOrReplaceChild("left_hind_leg", var2, PartPose.offset(2.0F, 18.0F, 4.0F));
        var1.addOrReplaceChild("right_front_leg", var2, PartPose.offset(-2.0F, 18.0F, -4.0F));
        var1.addOrReplaceChild("left_front_leg", var2, PartPose.offset(2.0F, 18.0F, -4.0F));
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
        this.rightHindLeg.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
        this.leftHindLeg.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.rightFrontLeg.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.leftFrontLeg.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
    }
}
