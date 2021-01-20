package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaSpitModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;

    public LlamaSpitModel(ModelPart param0) {
        this.root = param0;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        int var2 = 2;
        var1.addOrReplaceChild(
            "main",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F)
                .addBox(0.0F, -4.0F, 0.0F, 2.0F, 2.0F, 2.0F)
                .addBox(0.0F, 0.0F, -4.0F, 2.0F, 2.0F, 2.0F)
                .addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F)
                .addBox(2.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F)
                .addBox(0.0F, 2.0F, 0.0F, 2.0F, 2.0F, 2.0F)
                .addBox(0.0F, 0.0F, 2.0F, 2.0F, 2.0F, 2.0F),
            PartPose.ZERO
        );
        return LayerDefinition.create(var0, 64, 32);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
