package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PhantomModel<T extends Phantom> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart leftWingBase;
    private final ModelPart leftWingTip;
    private final ModelPart rightWingBase;
    private final ModelPart rightWingTip;
    private final ModelPart tailBase;
    private final ModelPart tailTip;

    public PhantomModel(ModelPart param0) {
        this.root = param0;
        ModelPart var0 = param0.getChild("body");
        this.tailBase = var0.getChild("tail_base");
        this.tailTip = this.tailBase.getChild("tail_tip");
        this.leftWingBase = var0.getChild("left_wing_base");
        this.leftWingTip = this.leftWingBase.getChild("left_wing_tip");
        this.rightWingBase = var0.getChild("right_wing_base");
        this.rightWingTip = this.rightWingBase.getChild("right_wing_tip");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        PartDefinition var2 = var1.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 8).addBox(-3.0F, -2.0F, -8.0F, 5.0F, 3.0F, 9.0F), PartPose.rotation(-0.1F, 0.0F, 0.0F)
        );
        PartDefinition var3 = var2.addOrReplaceChild(
            "tail_base", CubeListBuilder.create().texOffs(3, 20).addBox(-2.0F, 0.0F, 0.0F, 3.0F, 2.0F, 6.0F), PartPose.offset(0.0F, -2.0F, 1.0F)
        );
        var3.addOrReplaceChild(
            "tail_tip", CubeListBuilder.create().texOffs(4, 29).addBox(-1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 6.0F), PartPose.offset(0.0F, 0.5F, 6.0F)
        );
        PartDefinition var4 = var2.addOrReplaceChild(
            "left_wing_base",
            CubeListBuilder.create().texOffs(23, 12).addBox(0.0F, 0.0F, 0.0F, 6.0F, 2.0F, 9.0F),
            PartPose.offsetAndRotation(2.0F, -2.0F, -8.0F, 0.0F, 0.0F, 0.1F)
        );
        var4.addOrReplaceChild(
            "left_wing_tip",
            CubeListBuilder.create().texOffs(16, 24).addBox(0.0F, 0.0F, 0.0F, 13.0F, 1.0F, 9.0F),
            PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1F)
        );
        PartDefinition var5 = var2.addOrReplaceChild(
            "right_wing_base",
            CubeListBuilder.create().texOffs(23, 12).mirror().addBox(-6.0F, 0.0F, 0.0F, 6.0F, 2.0F, 9.0F),
            PartPose.offsetAndRotation(-3.0F, -2.0F, -8.0F, 0.0F, 0.0F, -0.1F)
        );
        var5.addOrReplaceChild(
            "right_wing_tip",
            CubeListBuilder.create().texOffs(16, 24).mirror().addBox(-13.0F, 0.0F, 0.0F, 13.0F, 1.0F, 9.0F),
            PartPose.offsetAndRotation(-6.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1F)
        );
        var2.addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -2.0F, -5.0F, 7.0F, 3.0F, 5.0F),
            PartPose.offsetAndRotation(0.0F, 1.0F, -7.0F, 0.2F, 0.0F, 0.0F)
        );
        return LayerDefinition.create(var0, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = ((float)param0.getUniqueFlapTickOffset() + param3) * 7.448451F * (float) (Math.PI / 180.0);
        float var1 = 16.0F;
        this.leftWingBase.zRot = Mth.cos(var0) * 16.0F * (float) (Math.PI / 180.0);
        this.leftWingTip.zRot = Mth.cos(var0) * 16.0F * (float) (Math.PI / 180.0);
        this.rightWingBase.zRot = -this.leftWingBase.zRot;
        this.rightWingTip.zRot = -this.leftWingTip.zRot;
        this.tailBase.xRot = -(5.0F + Mth.cos(var0 * 2.0F) * 5.0F) * (float) (Math.PI / 180.0);
        this.tailTip.xRot = -(5.0F + Mth.cos(var0 * 2.0F) * 5.0F) * (float) (Math.PI / 180.0);
    }
}
