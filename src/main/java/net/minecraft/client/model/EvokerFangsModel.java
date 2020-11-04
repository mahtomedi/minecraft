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
public class EvokerFangsModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart base;
    private final ModelPart upperJaw;
    private final ModelPart lowerJaw;

    public EvokerFangsModel(ModelPart param0) {
        this.root = param0;
        this.base = param0.getChild("base");
        this.upperJaw = param0.getChild("upper_jaw");
        this.lowerJaw = param0.getChild("lower_jaw");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "base", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 10.0F, 12.0F, 10.0F), PartPose.offset(-5.0F, 24.0F, -5.0F)
        );
        CubeListBuilder var2 = CubeListBuilder.create().texOffs(40, 0).addBox(0.0F, 0.0F, 0.0F, 4.0F, 14.0F, 8.0F);
        var1.addOrReplaceChild("upper_jaw", var2, PartPose.offset(1.5F, 24.0F, -4.0F));
        var1.addOrReplaceChild("lower_jaw", var2, PartPose.offsetAndRotation(-1.5F, 24.0F, 4.0F, 0.0F, (float) Math.PI, 0.0F));
        return LayerDefinition.create(var0, 64, 32);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = param1 * 2.0F;
        if (var0 > 1.0F) {
            var0 = 1.0F;
        }

        var0 = 1.0F - var0 * var0 * var0;
        this.upperJaw.zRot = (float) Math.PI - var0 * 0.35F * (float) Math.PI;
        this.lowerJaw.zRot = (float) Math.PI + var0 * 0.35F * (float) Math.PI;
        float var1 = (param1 + Mth.sin(param1 * 2.7F)) * 0.6F * 12.0F;
        this.upperJaw.y = 24.0F - var1;
        this.lowerJaw.y = this.upperJaw.y;
        this.base.y = this.upperJaw.y;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
