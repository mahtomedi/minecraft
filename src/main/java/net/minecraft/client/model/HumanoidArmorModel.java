package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HumanoidArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
    public HumanoidArmorModel(ModelPart param0) {
        super(param0);
    }

    public static MeshDefinition createBodyLayer(CubeDeformation param0) {
        MeshDefinition var0 = HumanoidModel.createMesh(param0, 0.0F);
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0.extend(-0.1F)),
            PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0.extend(-0.1F)),
            PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return var0;
    }
}
