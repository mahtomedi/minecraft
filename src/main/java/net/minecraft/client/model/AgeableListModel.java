package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AgeableListModel<E extends Entity> extends EntityModel<E> {
    private final boolean scaleHead;
    private final float yHeadOffset;
    private final float zHeadOffset;
    private final float babyHeadScale;
    private final float babyBodyScale;
    private final float bodyYOffset;

    protected AgeableListModel(boolean param0, float param1, float param2) {
        this(param0, param1, param2, 2.0F, 2.0F, 24.0F);
    }

    protected AgeableListModel(boolean param0, float param1, float param2, float param3, float param4, float param5) {
        this.scaleHead = param0;
        this.yHeadOffset = param1;
        this.zHeadOffset = param2;
        this.babyHeadScale = param3;
        this.babyBodyScale = param4;
        this.bodyYOffset = param5;
    }

    protected AgeableListModel() {
        this(false, 5.0F, 2.0F);
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, float param3, float param4, float param5) {
        if (this.young) {
            param0.pushPose();
            if (this.scaleHead) {
                float var0 = 1.5F / this.babyHeadScale;
                param0.scale(var0, var0, var0);
            }

            param0.translate(0.0, (double)(this.yHeadOffset / 16.0F), (double)(this.zHeadOffset / 16.0F));
            this.headParts().forEach(param6 -> param6.render(param0, param1, 0.0625F, param2, null, param3, param4, param5));
            param0.popPose();
            param0.pushPose();
            float var1 = 1.0F / this.babyBodyScale;
            param0.scale(var1, var1, var1);
            param0.translate(0.0, (double)(this.bodyYOffset / 16.0F), 0.0);
            this.bodyParts().forEach(param6 -> param6.render(param0, param1, 0.0625F, param2, null, param3, param4, param5));
            param0.popPose();
        } else {
            this.headParts().forEach(param6 -> param6.render(param0, param1, 0.0625F, param2, null, param3, param4, param5));
            this.bodyParts().forEach(param6 -> param6.render(param0, param1, 0.0625F, param2, null, param3, param4, param5));
        }

    }

    protected abstract Iterable<ModelPart> headParts();

    protected abstract Iterable<ModelPart> bodyParts();
}
