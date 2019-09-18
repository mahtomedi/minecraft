package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SilverfishModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart[] bodyParts;
    private final ModelPart[] bodyLayers;
    private final float[] zPlacement = new float[7];
    private static final int[][] BODY_SIZES = new int[][]{{3, 2, 2}, {4, 3, 2}, {6, 4, 3}, {3, 3, 3}, {2, 2, 3}, {2, 1, 2}, {1, 1, 2}};
    private static final int[][] BODY_TEXS = new int[][]{{0, 0}, {0, 4}, {0, 9}, {0, 16}, {0, 22}, {11, 0}, {13, 4}};

    public SilverfishModel() {
        this.bodyParts = new ModelPart[7];
        float var0 = -3.5F;

        for(int var1 = 0; var1 < this.bodyParts.length; ++var1) {
            this.bodyParts[var1] = new ModelPart(this, BODY_TEXS[var1][0], BODY_TEXS[var1][1]);
            this.bodyParts[var1]
                .addBox(
                    (float)BODY_SIZES[var1][0] * -0.5F,
                    0.0F,
                    (float)BODY_SIZES[var1][2] * -0.5F,
                    (float)BODY_SIZES[var1][0],
                    (float)BODY_SIZES[var1][1],
                    (float)BODY_SIZES[var1][2]
                );
            this.bodyParts[var1].setPos(0.0F, (float)(24 - BODY_SIZES[var1][1]), var0);
            this.zPlacement[var1] = var0;
            if (var1 < this.bodyParts.length - 1) {
                var0 += (float)(BODY_SIZES[var1][2] + BODY_SIZES[var1 + 1][2]) * 0.5F;
            }
        }

        this.bodyLayers = new ModelPart[3];
        this.bodyLayers[0] = new ModelPart(this, 20, 0);
        this.bodyLayers[0].addBox(-5.0F, 0.0F, (float)BODY_SIZES[2][2] * -0.5F, 10.0F, 8.0F, (float)BODY_SIZES[2][2]);
        this.bodyLayers[0].setPos(0.0F, 16.0F, this.zPlacement[2]);
        this.bodyLayers[1] = new ModelPart(this, 20, 11);
        this.bodyLayers[1].addBox(-3.0F, 0.0F, (float)BODY_SIZES[4][2] * -0.5F, 6.0F, 4.0F, (float)BODY_SIZES[4][2]);
        this.bodyLayers[1].setPos(0.0F, 20.0F, this.zPlacement[4]);
        this.bodyLayers[2] = new ModelPart(this, 20, 18);
        this.bodyLayers[2].addBox(-3.0F, 0.0F, (float)BODY_SIZES[4][2] * -0.5F, 6.0F, 5.0F, (float)BODY_SIZES[1][2]);
        this.bodyLayers[2].setPos(0.0F, 19.0F, this.zPlacement[1]);
    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);

        for(ModelPart var0 : this.bodyParts) {
            var0.render(param6);
        }

        for(ModelPart var1 : this.bodyLayers) {
            var1.render(param6);
        }

    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
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
