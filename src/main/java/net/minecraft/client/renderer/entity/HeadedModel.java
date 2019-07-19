package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface HeadedModel {
    ModelPart getHead();

    default void translateToHead(float param0) {
        this.getHead().translateTo(param0);
    }
}
