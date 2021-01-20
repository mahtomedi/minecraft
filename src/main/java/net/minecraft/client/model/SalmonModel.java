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
public class SalmonModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart bodyBack;

    public SalmonModel(ModelPart param0) {
        this.root = param0;
        this.bodyBack = param0.getChild("body_back");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        int var2 = 20;
        PartDefinition var3 = var1.addOrReplaceChild(
            "body_front", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.5F, 0.0F, 3.0F, 5.0F, 8.0F), PartPose.offset(0.0F, 20.0F, 0.0F)
        );
        PartDefinition var4 = var1.addOrReplaceChild(
            "body_back", CubeListBuilder.create().texOffs(0, 13).addBox(-1.5F, -2.5F, 0.0F, 3.0F, 5.0F, 8.0F), PartPose.offset(0.0F, 20.0F, 8.0F)
        );
        var1.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 4.0F, 3.0F), PartPose.offset(0.0F, 20.0F, 0.0F)
        );
        var4.addOrReplaceChild(
            "back_fin", CubeListBuilder.create().texOffs(20, 10).addBox(0.0F, -2.5F, 0.0F, 0.0F, 5.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 8.0F)
        );
        var3.addOrReplaceChild(
            "top_front_fin", CubeListBuilder.create().texOffs(2, 1).addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 3.0F), PartPose.offset(0.0F, -4.5F, 5.0F)
        );
        var4.addOrReplaceChild(
            "top_back_fin", CubeListBuilder.create().texOffs(0, 2).addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 4.0F), PartPose.offset(0.0F, -4.5F, -1.0F)
        );
        var1.addOrReplaceChild(
            "right_fin",
            CubeListBuilder.create().texOffs(-4, 0).addBox(-2.0F, 0.0F, 0.0F, 2.0F, 0.0F, 2.0F),
            PartPose.offsetAndRotation(-1.5F, 21.5F, 0.0F, 0.0F, 0.0F, (float) (-Math.PI / 4))
        );
        var1.addOrReplaceChild(
            "left_fin",
            CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 2.0F, 0.0F, 2.0F),
            PartPose.offsetAndRotation(1.5F, 21.5F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 4))
        );
        return LayerDefinition.create(var0, 32, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = 1.0F;
        float var1 = 1.0F;
        if (!param0.isInWater()) {
            var0 = 1.3F;
            var1 = 1.7F;
        }

        this.bodyBack.yRot = -var0 * 0.25F * Mth.sin(var1 * 0.6F * param3);
    }
}
