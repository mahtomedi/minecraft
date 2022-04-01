package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GoatModel<T extends Goat> extends QuadrupedModel<T> {
    public GoatModel(ModelPart param0) {
        super(param0, true, 19.0F, 1.0F, 2.5F, 2.0F, 24);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        PartDefinition var2 = var1.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .texOffs(2, 61)
                .addBox("right ear", -6.0F, -11.0F, -10.0F, 3.0F, 2.0F, 1.0F)
                .texOffs(2, 61)
                .mirror()
                .addBox("left ear", 2.0F, -11.0F, -10.0F, 3.0F, 2.0F, 1.0F)
                .texOffs(23, 52)
                .addBox("goatee", -0.5F, -3.0F, -14.0F, 0.0F, 7.0F, 5.0F),
            PartPose.offset(1.0F, 14.0F, 0.0F)
        );
        var2.addOrReplaceChild(
            "left_horn", CubeListBuilder.create().texOffs(12, 55).addBox(-0.01F, -16.0F, -10.0F, 2.0F, 7.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        var2.addOrReplaceChild(
            "right_horn", CubeListBuilder.create().texOffs(12, 55).addBox(-2.99F, -16.0F, -10.0F, 2.0F, 7.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        var2.addOrReplaceChild(
            "nose",
            CubeListBuilder.create().texOffs(34, 46).addBox(-3.0F, -4.0F, -8.0F, 5.0F, 7.0F, 10.0F),
            PartPose.offsetAndRotation(0.0F, -8.0F, -8.0F, 0.9599F, 0.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(1, 1)
                .addBox(-4.0F, -17.0F, -7.0F, 9.0F, 11.0F, 16.0F)
                .texOffs(0, 28)
                .addBox(-5.0F, -18.0F, -8.0F, 11.0F, 14.0F, 11.0F),
            PartPose.offset(0.0F, 24.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create().texOffs(36, 29).addBox(0.0F, 4.0F, 0.0F, 3.0F, 6.0F, 3.0F), PartPose.offset(1.0F, 14.0F, 4.0F)
        );
        var1.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create().texOffs(49, 29).addBox(0.0F, 4.0F, 0.0F, 3.0F, 6.0F, 3.0F), PartPose.offset(-3.0F, 14.0F, 4.0F)
        );
        var1.addOrReplaceChild(
            "left_front_leg", CubeListBuilder.create().texOffs(49, 2).addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F), PartPose.offset(1.0F, 14.0F, -6.0F)
        );
        var1.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().texOffs(35, 2).addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F), PartPose.offset(-3.0F, 14.0F, -6.0F)
        );
        return LayerDefinition.create(var0, 64, 64);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.head.getChild("left_horn").visible = !param0.isBaby();
        this.head.getChild("right_horn").visible = !param0.isBaby();
        super.setupAnim(param0, param1, param2, param3, param4, param5);
        if (param0.isPassenger()) {
            this.head.yRot = this.body.yRot;
        }

        float var0 = param0.getRammingXHeadRot();
        if (var0 != 0.0F) {
            this.head.xRot = var0;
        }

    }
}
