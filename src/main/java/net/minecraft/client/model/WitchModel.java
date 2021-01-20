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
public class WitchModel<T extends Entity> extends VillagerModel<T> {
    private boolean holdingItem;

    public WitchModel(ModelPart param0) {
        super(param0);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = VillagerModel.createBodyModel();
        PartDefinition var1 = var0.getRoot();
        PartDefinition var2 = var1.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F), PartPose.ZERO
        );
        PartDefinition var3 = var2.addOrReplaceChild(
            "hat", CubeListBuilder.create().texOffs(0, 64).addBox(0.0F, 0.0F, 0.0F, 10.0F, 2.0F, 10.0F), PartPose.offset(-5.0F, -10.03125F, -5.0F)
        );
        PartDefinition var4 = var3.addOrReplaceChild(
            "hat2",
            CubeListBuilder.create().texOffs(0, 76).addBox(0.0F, 0.0F, 0.0F, 7.0F, 4.0F, 7.0F),
            PartPose.offsetAndRotation(1.75F, -4.0F, 2.0F, -0.05235988F, 0.0F, 0.02617994F)
        );
        PartDefinition var5 = var4.addOrReplaceChild(
            "hat3",
            CubeListBuilder.create().texOffs(0, 87).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F),
            PartPose.offsetAndRotation(1.75F, -4.0F, 2.0F, -0.10471976F, 0.0F, 0.05235988F)
        );
        var5.addOrReplaceChild(
            "hat4",
            CubeListBuilder.create().texOffs(0, 95).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)),
            PartPose.offsetAndRotation(1.75F, -2.0F, 2.0F, (float) (-Math.PI / 15), 0.0F, 0.10471976F)
        );
        PartDefinition var6 = var2.getChild("nose");
        var6.addOrReplaceChild(
            "mole",
            CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 3.0F, -6.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)),
            PartPose.offset(0.0F, -2.0F, 0.0F)
        );
        return LayerDefinition.create(var0, 64, 128);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        super.setupAnim(param0, param1, param2, param3, param4, param5);
        this.nose.setPos(0.0F, -2.0F, 0.0F);
        float var0 = 0.01F * (float)(param0.getId() % 10);
        this.nose.xRot = Mth.sin((float)param0.tickCount * var0) * 4.5F * (float) (Math.PI / 180.0);
        this.nose.yRot = 0.0F;
        this.nose.zRot = Mth.cos((float)param0.tickCount * var0) * 2.5F * (float) (Math.PI / 180.0);
        if (this.holdingItem) {
            this.nose.setPos(0.0F, 1.0F, -1.5F);
            this.nose.xRot = -0.9F;
        }

    }

    public ModelPart getNose() {
        return this.nose;
    }

    public void setHoldingItem(boolean param0) {
        this.holdingItem = param0;
    }
}
