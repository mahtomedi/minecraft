package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PigModel<T extends Entity> extends QuadrupedModel<T> {
    public PigModel(ModelPart param0) {
        super(param0, false, 4.0F, 4.0F, 2.0F, 2.0F, 24);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation param0) {
        MeshDefinition var0 = QuadrupedModel.createBodyMesh(6, param0);
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, param0)
                .texOffs(16, 16)
                .addBox(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 1.0F, param0),
            PartPose.offset(0.0F, 12.0F, -6.0F)
        );
        return LayerDefinition.create(var0, 64, 32);
    }
}
