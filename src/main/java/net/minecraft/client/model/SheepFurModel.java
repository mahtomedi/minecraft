package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SheepFurModel<T extends Sheep> extends QuadrupedModel<T> {
    private float headXRot;

    public SheepFurModel(ModelPart param0) {
        super(param0, false, 8.0F, 4.0F, 2.0F, 2.0F, 24);
    }

    public static LayerDefinition createFurLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.6F)),
            PartPose.offset(0.0F, 6.0F, -8.0F)
        );
        var1.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(28, 8).addBox(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F, new CubeDeformation(1.75F)),
            PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        CubeListBuilder var2 = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.5F));
        var1.addOrReplaceChild("right_hind_leg", var2, PartPose.offset(-3.0F, 12.0F, 7.0F));
        var1.addOrReplaceChild("left_hind_leg", var2, PartPose.offset(3.0F, 12.0F, 7.0F));
        var1.addOrReplaceChild("right_front_leg", var2, PartPose.offset(-3.0F, 12.0F, -5.0F));
        var1.addOrReplaceChild("left_front_leg", var2, PartPose.offset(3.0F, 12.0F, -5.0F));
        return LayerDefinition.create(var0, 64, 32);
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        super.prepareMobModel(param0, param1, param2, param3);
        this.head.y = 6.0F + param0.getHeadEatPositionScale(param3) * 9.0F;
        this.headXRot = param0.getHeadEatAngleScale(param3);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        super.setupAnim(param0, param1, param2, param3, param4, param5);
        this.head.xRot = this.headXRot;
    }
}
