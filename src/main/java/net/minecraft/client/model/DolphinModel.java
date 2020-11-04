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
public class DolphinModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart tailFin;

    public DolphinModel(ModelPart param0) {
        this.root = param0;
        this.body = param0.getChild("body");
        this.tail = this.body.getChild("tail");
        this.tailFin = this.tail.getChild("tail_fin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        float var2 = 18.0F;
        float var3 = -8.0F;
        PartDefinition var4 = var1.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(22, 0).addBox(-4.0F, -7.0F, 0.0F, 8.0F, 7.0F, 13.0F), PartPose.offset(0.0F, 22.0F, -5.0F)
        );
        var4.addOrReplaceChild(
            "back_fin",
            CubeListBuilder.create().texOffs(51, 0).addBox(-0.5F, 0.0F, 8.0F, 1.0F, 4.0F, 5.0F),
            PartPose.rotation((float) (Math.PI / 3), 0.0F, 0.0F)
        );
        var4.addOrReplaceChild(
            "left_fin",
            CubeListBuilder.create().texOffs(48, 20).mirror().addBox(-0.5F, -4.0F, 0.0F, 1.0F, 4.0F, 7.0F),
            PartPose.offsetAndRotation(2.0F, -2.0F, 4.0F, (float) (Math.PI / 3), 0.0F, (float) (Math.PI * 2.0 / 3.0))
        );
        var4.addOrReplaceChild(
            "right_fin",
            CubeListBuilder.create().texOffs(48, 20).addBox(-0.5F, -4.0F, 0.0F, 1.0F, 4.0F, 7.0F),
            PartPose.offsetAndRotation(-2.0F, -2.0F, 4.0F, (float) (Math.PI / 3), 0.0F, (float) (-Math.PI * 2.0 / 3.0))
        );
        PartDefinition var5 = var4.addOrReplaceChild(
            "tail",
            CubeListBuilder.create().texOffs(0, 19).addBox(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 11.0F),
            PartPose.offsetAndRotation(0.0F, -2.5F, 11.0F, -0.10471976F, 0.0F, 0.0F)
        );
        var5.addOrReplaceChild(
            "tail_fin", CubeListBuilder.create().texOffs(19, 20).addBox(-5.0F, -0.5F, 0.0F, 10.0F, 1.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 9.0F)
        );
        PartDefinition var6 = var4.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -3.0F, -3.0F, 8.0F, 7.0F, 6.0F), PartPose.offset(0.0F, -4.0F, -3.0F)
        );
        var6.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 2.0F, -7.0F, 2.0F, 2.0F, 4.0F), PartPose.ZERO);
        return LayerDefinition.create(var0, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.body.xRot = param5 * (float) (Math.PI / 180.0);
        this.body.yRot = param4 * (float) (Math.PI / 180.0);
        if (Entity.getHorizontalDistanceSqr(param0.getDeltaMovement()) > 1.0E-7) {
            this.body.xRot += -0.05F - 0.05F * Mth.cos(param3 * 0.3F);
            this.tail.xRot = -0.1F * Mth.cos(param3 * 0.3F);
            this.tailFin.xRot = -0.2F * Mth.cos(param3 * 0.3F);
        }

    }
}
