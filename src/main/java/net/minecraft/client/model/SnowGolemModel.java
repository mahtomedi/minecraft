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
public class SnowGolemModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart upperBody;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;

    public SnowGolemModel(ModelPart param0) {
        this.root = param0;
        this.head = param0.getChild("head");
        this.leftArm = param0.getChild("left_arm");
        this.rightArm = param0.getChild("right_arm");
        this.upperBody = param0.getChild("upper_body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        float var2 = 4.0F;
        CubeDeformation var3 = new CubeDeformation(-0.5F);
        var1.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, var3), PartPose.offset(0.0F, 4.0F, 0.0F)
        );
        CubeListBuilder var4 = CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 0.0F, -1.0F, 12.0F, 2.0F, 2.0F, var3);
        var1.addOrReplaceChild("left_arm", var4, PartPose.offsetAndRotation(5.0F, 6.0F, 1.0F, 0.0F, 0.0F, 1.0F));
        var1.addOrReplaceChild("right_arm", var4, PartPose.offsetAndRotation(-5.0F, 6.0F, -1.0F, 0.0F, (float) Math.PI, -1.0F));
        var1.addOrReplaceChild(
            "upper_body", CubeListBuilder.create().texOffs(0, 16).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, var3), PartPose.offset(0.0F, 13.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "lower_body", CubeListBuilder.create().texOffs(0, 36).addBox(-6.0F, -12.0F, -6.0F, 12.0F, 12.0F, 12.0F, var3), PartPose.offset(0.0F, 24.0F, 0.0F)
        );
        return LayerDefinition.create(var0, 64, 64);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.upperBody.yRot = param4 * (float) (Math.PI / 180.0) * 0.25F;
        float var0 = Mth.sin(this.upperBody.yRot);
        float var1 = Mth.cos(this.upperBody.yRot);
        this.leftArm.yRot = this.upperBody.yRot;
        this.rightArm.yRot = this.upperBody.yRot + (float) Math.PI;
        this.leftArm.x = var1 * 5.0F;
        this.leftArm.z = -var0 * 5.0F;
        this.rightArm.x = -var1 * 5.0F;
        this.rightArm.z = var0 * 5.0F;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public ModelPart getHead() {
        return this.head;
    }
}
