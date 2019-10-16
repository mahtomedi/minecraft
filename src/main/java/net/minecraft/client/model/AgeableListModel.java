package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
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
        this(RenderType::entityCutoutNoCull, param0, param1, param2, param3, param4, param5);
    }

    protected AgeableListModel(
        Function<ResourceLocation, RenderType> param0, boolean param1, float param2, float param3, float param4, float param5, float param6
    ) {
        super(param0);
        this.scaleHead = param1;
        this.yHeadOffset = param2;
        this.zHeadOffset = param3;
        this.babyHeadScale = param4;
        this.babyBodyScale = param5;
        this.bodyYOffset = param6;
    }

    protected AgeableListModel() {
        this(false, 5.0F, 2.0F);
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6) {
        if (this.young) {
            param0.pushPose();
            if (this.scaleHead) {
                float var0 = 1.5F / this.babyHeadScale;
                param0.scale(var0, var0, var0);
            }

            param0.translate(0.0, (double)(this.yHeadOffset / 16.0F), (double)(this.zHeadOffset / 16.0F));
            this.headParts().forEach(param7 -> param7.render(param0, param1, 0.0625F, param2, param3, null, param4, param5, param6));
            param0.popPose();
            param0.pushPose();
            float var1 = 1.0F / this.babyBodyScale;
            param0.scale(var1, var1, var1);
            param0.translate(0.0, (double)(this.bodyYOffset / 16.0F), 0.0);
            this.bodyParts().forEach(param7 -> param7.render(param0, param1, 0.0625F, param2, param3, null, param4, param5, param6));
            param0.popPose();
        } else {
            this.headParts().forEach(param7 -> param7.render(param0, param1, 0.0625F, param2, param3, null, param4, param5, param6));
            this.bodyParts().forEach(param7 -> param7.render(param0, param1, 0.0625F, param2, param3, null, param4, param5, param6));
        }

    }

    protected abstract Iterable<ModelPart> headParts();

    protected abstract Iterable<ModelPart> bodyParts();
}
