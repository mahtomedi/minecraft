package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
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
public class BoatModel extends ListModel<Boat> {
    private static final String LEFT_PADDLE = "left_paddle";
    private static final String RIGHT_PADDLE = "right_paddle";
    private static final String WATER_PATCH = "water_patch";
    private static final String BOTTOM = "bottom";
    private static final String BACK = "back";
    private static final String FRONT = "front";
    private static final String RIGHT = "right";
    private static final String LEFT = "left";
    private final ModelPart leftPaddle;
    private final ModelPart rightPaddle;
    private final ModelPart waterPatch;
    private final ImmutableList<ModelPart> parts;

    public BoatModel(ModelPart param0) {
        this.leftPaddle = param0.getChild("left_paddle");
        this.rightPaddle = param0.getChild("right_paddle");
        this.waterPatch = param0.getChild("water_patch");
        this.parts = ImmutableList.of(
            param0.getChild("bottom"),
            param0.getChild("back"),
            param0.getChild("front"),
            param0.getChild("right"),
            param0.getChild("left"),
            this.leftPaddle,
            this.rightPaddle
        );
    }

    public static LayerDefinition createBodyModel() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        int var2 = 32;
        int var3 = 6;
        int var4 = 20;
        int var5 = 4;
        int var6 = 28;
        var1.addOrReplaceChild(
            "bottom",
            CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F),
            PartPose.offsetAndRotation(0.0F, 3.0F, 1.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "back",
            CubeListBuilder.create().texOffs(0, 19).addBox(-13.0F, -7.0F, -1.0F, 18.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(-15.0F, 4.0F, 4.0F, 0.0F, (float) (Math.PI * 3.0 / 2.0), 0.0F)
        );
        var1.addOrReplaceChild(
            "front",
            CubeListBuilder.create().texOffs(0, 27).addBox(-8.0F, -7.0F, -1.0F, 16.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(15.0F, 4.0F, 0.0F, 0.0F, (float) (Math.PI / 2), 0.0F)
        );
        var1.addOrReplaceChild(
            "right",
            CubeListBuilder.create().texOffs(0, 35).addBox(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, 4.0F, -9.0F, 0.0F, (float) Math.PI, 0.0F)
        );
        var1.addOrReplaceChild(
            "left", CubeListBuilder.create().texOffs(0, 43).addBox(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F), PartPose.offset(0.0F, 4.0F, 9.0F)
        );
        int var7 = 20;
        int var8 = 7;
        int var9 = 6;
        float var10 = -5.0F;
        var1.addOrReplaceChild(
            "left_paddle",
            CubeListBuilder.create().texOffs(62, 0).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).addBox(-1.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F),
            PartPose.offsetAndRotation(3.0F, -5.0F, 9.0F, 0.0F, 0.0F, (float) (Math.PI / 16))
        );
        var1.addOrReplaceChild(
            "right_paddle",
            CubeListBuilder.create().texOffs(62, 20).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).addBox(0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F),
            PartPose.offsetAndRotation(3.0F, -5.0F, -9.0F, 0.0F, (float) Math.PI, (float) (Math.PI / 16))
        );
        var1.addOrReplaceChild(
            "water_patch",
            CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F),
            PartPose.offsetAndRotation(0.0F, -3.0F, 1.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        return LayerDefinition.create(var0, 128, 64);
    }

    public void setupAnim(Boat param0, float param1, float param2, float param3, float param4, float param5) {
        animatePaddle(param0, 0, this.leftPaddle, param1);
        animatePaddle(param0, 1, this.rightPaddle, param1);
    }

    public ImmutableList<ModelPart> parts() {
        return this.parts;
    }

    public ModelPart waterPatch() {
        return this.waterPatch;
    }

    private static void animatePaddle(Boat param0, int param1, ModelPart param2, float param3) {
        float var0 = param0.getRowingTime(param1, param3);
        param2.xRot = (float)Mth.clampedLerp((float) (-Math.PI / 3), (float) (-Math.PI / 12), (double)((Mth.sin(-var0) + 1.0F) / 2.0F));
        param2.yRot = (float)Mth.clampedLerp((float) (-Math.PI / 4), (float) (Math.PI / 4), (double)((Mth.sin(-var0 + 1.0F) + 1.0F) / 2.0F));
        if (param1 == 1) {
            param2.yRot = (float) Math.PI - param2.yRot;
        }

    }
}
