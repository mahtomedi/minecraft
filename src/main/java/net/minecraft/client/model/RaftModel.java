package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RaftModel extends ListModel<Boat> {
    private static final String LEFT_PADDLE = "left_paddle";
    private static final String RIGHT_PADDLE = "right_paddle";
    private static final String BOTTOM = "bottom";
    private final ModelPart leftPaddle;
    private final ModelPart rightPaddle;
    private final ImmutableList<ModelPart> parts;

    public RaftModel(ModelPart param0) {
        this.leftPaddle = param0.getChild("left_paddle");
        this.rightPaddle = param0.getChild("right_paddle");
        this.parts = this.createPartsBuilder(param0).build();
    }

    protected Builder<ModelPart> createPartsBuilder(ModelPart param0) {
        Builder<ModelPart> var0 = new Builder<>();
        var0.add(param0.getChild("bottom"), this.leftPaddle, this.rightPaddle);
        return var0;
    }

    public static void createChildren(PartDefinition param0) {
        param0.addOrReplaceChild(
            "bottom",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-14.0F, -11.0F, -4.0F, 28.0F, 20.0F, 4.0F)
                .texOffs(0, 0)
                .addBox(-14.0F, -9.0F, -8.0F, 28.0F, 16.0F, 4.0F),
            PartPose.offsetAndRotation(0.0F, -2.0F, 1.0F, 1.5708F, 0.0F, 0.0F)
        );
        int var0 = 20;
        int var1 = 7;
        int var2 = 6;
        float var3 = -5.0F;
        param0.addOrReplaceChild(
            "left_paddle",
            CubeListBuilder.create().texOffs(0, 24).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).addBox(-1.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F),
            PartPose.offsetAndRotation(3.0F, -4.0F, 9.0F, 0.0F, 0.0F, (float) (Math.PI / 16))
        );
        param0.addOrReplaceChild(
            "right_paddle",
            CubeListBuilder.create().texOffs(40, 24).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).addBox(0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F),
            PartPose.offsetAndRotation(3.0F, -4.0F, -9.0F, 0.0F, (float) Math.PI, (float) (Math.PI / 16))
        );
    }

    public static LayerDefinition createBodyModel() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        createChildren(var1);
        return LayerDefinition.create(var0, 128, 64);
    }

    public void setupAnim(Boat param0, float param1, float param2, float param3, float param4, float param5) {
        animatePaddle(param0, 0, this.leftPaddle, param1);
        animatePaddle(param0, 1, this.rightPaddle, param1);
    }

    public ImmutableList<ModelPart> parts() {
        return this.parts;
    }

    private static void animatePaddle(Boat param0, int param1, ModelPart param2, float param3) {
        float var0 = param0.getRowingTime(param1, param3);
        param2.xRot = Mth.clampedLerp((float) (-Math.PI / 3), (float) (-Math.PI / 12), (Mth.sin(-var0) + 1.0F) / 2.0F);
        param2.yRot = Mth.clampedLerp((float) (-Math.PI / 4), (float) (Math.PI / 4), (Mth.sin(-var0 + 1.0F) + 1.0F) / 2.0F);
        if (param1 == 1) {
            param2.yRot = (float) Math.PI - param2.yRot;
        }

    }
}
