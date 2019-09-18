package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EndermiteModel<T extends Entity> extends EntityModel<T> {
    private static final int[][] BODY_SIZES = new int[][]{{4, 3, 2}, {6, 4, 5}, {3, 3, 1}, {1, 2, 1}};
    private static final int[][] BODY_TEXS = new int[][]{{0, 0}, {0, 5}, {0, 14}, {0, 18}};
    private static final int BODY_COUNT = BODY_SIZES.length;
    private final ModelPart[] bodyParts = new ModelPart[BODY_COUNT];

    public EndermiteModel() {
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
            if (var1 < this.bodyParts.length - 1) {
                var0 += (float)(BODY_SIZES[var1][2] + BODY_SIZES[var1 + 1][2]) * 0.5F;
            }
        }

    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);

        for(ModelPart var0 : this.bodyParts) {
            var0.render(param6);
        }

    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        for(int var0 = 0; var0 < this.bodyParts.length; ++var0) {
            this.bodyParts[var0].yRot = Mth.cos(param3 * 0.9F + (float)var0 * 0.15F * (float) Math.PI)
                * (float) Math.PI
                * 0.01F
                * (float)(1 + Math.abs(var0 - 2));
            this.bodyParts[var0].x = Mth.sin(param3 * 0.9F + (float)var0 * 0.15F * (float) Math.PI) * (float) Math.PI * 0.1F * (float)Math.abs(var0 - 2);
        }

    }
}
