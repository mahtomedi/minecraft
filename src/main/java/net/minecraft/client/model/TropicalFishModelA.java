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
public class TropicalFishModelA<T extends Entity> extends ColorableHierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart tail;

    public TropicalFishModelA(ModelPart param0) {
        this.root = param0;
        this.tail = param0.getChild("tail");
    }

    public static LayerDefinition createBodyLayer(CubeDeformation param0) {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        int var2 = 22;
        var1.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.5F, -3.0F, 2.0F, 3.0F, 6.0F, param0), PartPose.offset(0.0F, 22.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "tail", CubeListBuilder.create().texOffs(22, -6).addBox(0.0F, -1.5F, 0.0F, 0.0F, 3.0F, 6.0F, param0), PartPose.offset(0.0F, 22.0F, 3.0F)
        );
        var1.addOrReplaceChild(
            "right_fin",
            CubeListBuilder.create().texOffs(2, 16).addBox(-2.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, param0),
            PartPose.offsetAndRotation(-1.0F, 22.5F, 0.0F, 0.0F, (float) (Math.PI / 4), 0.0F)
        );
        var1.addOrReplaceChild(
            "left_fin",
            CubeListBuilder.create().texOffs(2, 12).addBox(0.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, param0),
            PartPose.offsetAndRotation(1.0F, 22.5F, 0.0F, 0.0F, (float) (-Math.PI / 4), 0.0F)
        );
        var1.addOrReplaceChild(
            "top_fin", CubeListBuilder.create().texOffs(10, -5).addBox(0.0F, -3.0F, 0.0F, 0.0F, 3.0F, 6.0F, param0), PartPose.offset(0.0F, 20.5F, -3.0F)
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
        if (!param0.isInWater()) {
            var0 = 1.5F;
        }

        this.tail.yRot = -var0 * 0.45F * Mth.sin(0.6F * param3);
    }
}
