package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AgeableHierarchicalModel<E extends Entity> extends HierarchicalModel<E> {
    private final float youngScaleFactor;
    private final float bodyYOffset;

    public AgeableHierarchicalModel(float param0, float param1) {
        this(param0, param1, RenderType::entityCutoutNoCull);
    }

    public AgeableHierarchicalModel(float param0, float param1, Function<ResourceLocation, RenderType> param2) {
        super(param2);
        this.bodyYOffset = param1;
        this.youngScaleFactor = param0;
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
        if (this.young) {
            param0.pushPose();
            param0.scale(this.youngScaleFactor, this.youngScaleFactor, this.youngScaleFactor);
            param0.translate(0.0F, this.bodyYOffset / 16.0F, 0.0F);
            this.root().render(param0, param1, param2, param3, param4, param5, param6, param7);
            param0.popPose();
        } else {
            this.root().render(param0, param1, param2, param3, param4, param5, param6, param7);
        }

    }
}
