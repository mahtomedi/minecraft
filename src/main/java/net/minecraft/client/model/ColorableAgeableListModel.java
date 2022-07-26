package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ColorableAgeableListModel<E extends Entity> extends AgeableListModel<E> {
    private float r = 1.0F;
    private float g = 1.0F;
    private float b = 1.0F;

    public void setColor(float param0, float param1, float param2) {
        this.r = param0;
        this.g = param1;
        this.b = param2;
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
        super.renderToBuffer(param0, param1, param2, param3, this.r * param4, this.g * param5, this.b * param6, param7);
    }
}
