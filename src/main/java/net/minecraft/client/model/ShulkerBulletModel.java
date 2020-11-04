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
public class ShulkerBulletModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart main;

    public ShulkerBulletModel(ModelPart param0) {
        this.root = param0;
        this.main = param0.getChild("main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "main",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -4.0F, -1.0F, 8.0F, 8.0F, 2.0F)
                .texOffs(0, 10)
                .addBox(-1.0F, -4.0F, -4.0F, 2.0F, 8.0F, 8.0F)
                .texOffs(20, 0)
                .addBox(-4.0F, -1.0F, -4.0F, 8.0F, 2.0F, 8.0F),
            PartPose.ZERO
        );
        return LayerDefinition.create(var0, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.main.yRot = param4 * (float) (Math.PI / 180.0);
        this.main.xRot = param5 * (float) (Math.PI / 180.0);
    }
}
