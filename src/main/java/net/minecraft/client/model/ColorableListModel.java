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
public abstract class ColorableListModel<E extends Entity> extends ListModel<E> {
    private float r = 1.0F;
    private float g = 1.0F;
    private float b = 1.0F;

    public ColorableListModel(Function<ResourceLocation, RenderType> param0) {
        super(param0);
    }

    public void setColor(float param0, float param1, float param2) {
        this.r = param0;
        this.g = param1;
        this.b = param2;
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6) {
        super.renderToBuffer(param0, param1, param2, param3, this.r * param4, this.g * param5, this.b * param6);
    }
}
