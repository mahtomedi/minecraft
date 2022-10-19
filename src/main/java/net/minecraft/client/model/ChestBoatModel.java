package net.minecraft.client.model;

import com.google.common.collect.ImmutableList.Builder;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChestBoatModel extends BoatModel {
    private static final String CHEST_BOTTOM = "chest_bottom";
    private static final String CHEST_LID = "chest_lid";
    private static final String CHEST_LOCK = "chest_lock";

    public ChestBoatModel(ModelPart param0) {
        super(param0);
    }

    @Override
    protected Builder<ModelPart> createPartsBuilder(ModelPart param0) {
        Builder<ModelPart> var0 = super.createPartsBuilder(param0);
        var0.add(param0.getChild("chest_bottom"));
        var0.add(param0.getChild("chest_lid"));
        var0.add(param0.getChild("chest_lock"));
        return var0;
    }

    public static LayerDefinition createBodyModel() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        BoatModel.createChildren(var1);
        var1.addOrReplaceChild(
            "chest_bottom",
            CubeListBuilder.create().texOffs(0, 76).addBox(0.0F, 0.0F, 0.0F, 12.0F, 8.0F, 12.0F),
            PartPose.offsetAndRotation(-2.0F, -5.0F, -6.0F, 0.0F, (float) (-Math.PI / 2), 0.0F)
        );
        var1.addOrReplaceChild(
            "chest_lid",
            CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 0.0F, 0.0F, 12.0F, 4.0F, 12.0F),
            PartPose.offsetAndRotation(-2.0F, -9.0F, -6.0F, 0.0F, (float) (-Math.PI / 2), 0.0F)
        );
        var1.addOrReplaceChild(
            "chest_lock",
            CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 0.0F, 0.0F, 2.0F, 4.0F, 1.0F),
            PartPose.offsetAndRotation(-1.0F, -6.0F, -1.0F, 0.0F, (float) (-Math.PI / 2), 0.0F)
        );
        return LayerDefinition.create(var0, 128, 128);
    }
}
