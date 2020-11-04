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
public class MinecartModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart contents;

    public MinecartModel(ModelPart param0) {
        this.root = param0;
        this.contents = param0.getChild("contents");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        int var2 = 20;
        int var3 = 8;
        int var4 = 16;
        int var5 = 4;
        var1.addOrReplaceChild(
            "bottom",
            CubeListBuilder.create().texOffs(0, 10).addBox(-10.0F, -8.0F, -1.0F, 20.0F, 16.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "front",
            CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F),
            PartPose.offsetAndRotation(-9.0F, 4.0F, 0.0F, 0.0F, (float) (Math.PI * 3.0 / 2.0), 0.0F)
        );
        var1.addOrReplaceChild(
            "back",
            CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F),
            PartPose.offsetAndRotation(9.0F, 4.0F, 0.0F, 0.0F, (float) (Math.PI / 2), 0.0F)
        );
        var1.addOrReplaceChild(
            "left",
            CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, 4.0F, -7.0F, 0.0F, (float) Math.PI, 0.0F)
        );
        var1.addOrReplaceChild(
            "right", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F), PartPose.offset(0.0F, 4.0F, 7.0F)
        );
        var1.addOrReplaceChild(
            "contents",
            CubeListBuilder.create().texOffs(44, 10).addBox(-9.0F, -7.0F, -1.0F, 18.0F, 14.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, (float) (-Math.PI / 2), 0.0F, 0.0F)
        );
        return LayerDefinition.create(var0, 64, 32);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.contents.y = 4.0F - param3;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
