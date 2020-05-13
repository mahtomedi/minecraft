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
public abstract class ListModel<E extends Entity> extends EntityModel<E> {
    public ListModel() {
        this(RenderType::entityCutoutNoCull);
    }

    public ListModel(Function<ResourceLocation, RenderType> param0) {
        super(param0);
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
        this.parts().forEach(param8 -> param8.render(param0, param1, param2, param3, param4, param5, param6, param7));
    }

    public abstract Iterable<ModelPart> parts();
}
