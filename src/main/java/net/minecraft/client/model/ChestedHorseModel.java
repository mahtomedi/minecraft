package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChestedHorseModel<T extends AbstractChestedHorse> extends HorseModel<T> {
    private final ModelPart leftChest = this.body.getChild("left_chest");
    private final ModelPart rightChest = this.body.getChild("right_chest");

    public ChestedHorseModel(ModelPart param0) {
        super(param0);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = HorseModel.createBodyMesh(CubeDeformation.NONE);
        PartDefinition var1 = var0.getRoot();
        PartDefinition var2 = var1.getChild("body");
        CubeListBuilder var3 = CubeListBuilder.create().texOffs(26, 21).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 3.0F);
        var2.addOrReplaceChild("left_chest", var3, PartPose.offsetAndRotation(6.0F, -8.0F, 0.0F, 0.0F, (float) (-Math.PI / 2), 0.0F));
        var2.addOrReplaceChild("right_chest", var3, PartPose.offsetAndRotation(-6.0F, -8.0F, 0.0F, 0.0F, (float) (Math.PI / 2), 0.0F));
        PartDefinition var4 = var1.getChild("head_parts").getChild("head");
        CubeListBuilder var5 = CubeListBuilder.create().texOffs(0, 12).addBox(-1.0F, -7.0F, 0.0F, 2.0F, 7.0F, 1.0F);
        var4.addOrReplaceChild("left_ear", var5, PartPose.offsetAndRotation(1.25F, -10.0F, 4.0F, (float) (Math.PI / 12), 0.0F, (float) (Math.PI / 12)));
        var4.addOrReplaceChild("right_ear", var5, PartPose.offsetAndRotation(-1.25F, -10.0F, 4.0F, (float) (Math.PI / 12), 0.0F, (float) (-Math.PI / 12)));
        return LayerDefinition.create(var0, 64, 64);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        super.setupAnim(param0, param1, param2, param3, param4, param5);
        if (param0.hasChest()) {
            this.leftChest.visible = true;
            this.rightChest.visible = true;
        } else {
            this.leftChest.visible = false;
            this.rightChest.visible = false;
        }

    }
}
