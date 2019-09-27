package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ListModel<E extends Entity> extends EntityModel<E> {
    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, float param3, float param4, float param5) {
        this.parts().forEach(param6 -> param6.render(param0, param1, 0.0625F, param2, null, param3, param4, param5));
    }

    public abstract Iterable<ModelPart> parts();
}
