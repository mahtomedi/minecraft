package net.minecraft.client.model;

import java.util.Arrays;
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
public class SilverfishModel<T extends Entity> extends HierarchicalModel<T> {
    private static final int BODY_COUNT = 7;
    private final ModelPart root;
    private final ModelPart[] bodyParts = new ModelPart[7];
    private final ModelPart[] bodyLayers = new ModelPart[3];
    private static final int[][] BODY_SIZES = new int[][]{{3, 2, 2}, {4, 3, 2}, {6, 4, 3}, {3, 3, 3}, {2, 2, 3}, {2, 1, 2}, {1, 1, 2}};
    private static final int[][] BODY_TEXS = new int[][]{{0, 0}, {0, 4}, {0, 9}, {0, 16}, {0, 22}, {11, 0}, {13, 4}};

    public SilverfishModel(ModelPart param0) {
        this.root = param0;
        Arrays.setAll(this.bodyParts, param1 -> param0.getChild(getSegmentName(param1)));
        Arrays.setAll(this.bodyLayers, param1 -> param0.getChild(getLayerName(param1)));
    }

    private static String getLayerName(int param0) {
        return "layer" + param0;
    }

    private static String getSegmentName(int param0) {
        return "segment" + param0;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        float[] var2 = new float[7];
        float var3 = -3.5F;

        for(int var4 = 0; var4 < 7; ++var4) {
            var1.addOrReplaceChild(
                getSegmentName(var4),
                CubeListBuilder.create()
                    .texOffs(BODY_TEXS[var4][0], BODY_TEXS[var4][1])
                    .addBox(
                        (float)BODY_SIZES[var4][0] * -0.5F,
                        0.0F,
                        (float)BODY_SIZES[var4][2] * -0.5F,
                        (float)BODY_SIZES[var4][0],
                        (float)BODY_SIZES[var4][1],
                        (float)BODY_SIZES[var4][2]
                    ),
                PartPose.offset(0.0F, (float)(24 - BODY_SIZES[var4][1]), var3)
            );
            var2[var4] = var3;
            if (var4 < 6) {
                var3 += (float)(BODY_SIZES[var4][2] + BODY_SIZES[var4 + 1][2]) * 0.5F;
            }
        }

        var1.addOrReplaceChild(
            getLayerName(0),
            CubeListBuilder.create().texOffs(20, 0).addBox(-5.0F, 0.0F, (float)BODY_SIZES[2][2] * -0.5F, 10.0F, 8.0F, (float)BODY_SIZES[2][2]),
            PartPose.offset(0.0F, 16.0F, var2[2])
        );
        var1.addOrReplaceChild(
            getLayerName(1),
            CubeListBuilder.create().texOffs(20, 11).addBox(-3.0F, 0.0F, (float)BODY_SIZES[4][2] * -0.5F, 6.0F, 4.0F, (float)BODY_SIZES[4][2]),
            PartPose.offset(0.0F, 20.0F, var2[4])
        );
        var1.addOrReplaceChild(
            getLayerName(2),
            CubeListBuilder.create().texOffs(20, 18).addBox(-3.0F, 0.0F, (float)BODY_SIZES[4][2] * -0.5F, 6.0F, 5.0F, (float)BODY_SIZES[1][2]),
            PartPose.offset(0.0F, 19.0F, var2[1])
        );
        return LayerDefinition.create(var0, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        for(int var0 = 0; var0 < this.bodyParts.length; ++var0) {
            this.bodyParts[var0].yRot = Mth.cos(param3 * 0.9F + (float)var0 * 0.15F * (float) Math.PI)
                * (float) Math.PI
                * 0.05F
                * (float)(1 + Math.abs(var0 - 2));
            this.bodyParts[var0].x = Mth.sin(param3 * 0.9F + (float)var0 * 0.15F * (float) Math.PI) * (float) Math.PI * 0.2F * (float)Math.abs(var0 - 2);
        }

        this.bodyLayers[0].yRot = this.bodyParts[2].yRot;
        this.bodyLayers[1].yRot = this.bodyParts[4].yRot;
        this.bodyLayers[1].x = this.bodyParts[4].x;
        this.bodyLayers[2].yRot = this.bodyParts[1].yRot;
        this.bodyLayers[2].x = this.bodyParts[1].x;
    }
}
