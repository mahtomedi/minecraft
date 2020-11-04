package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class IllagerRenderer<T extends AbstractIllager> extends MobRenderer<T, IllagerModel<T>> {
    protected IllagerRenderer(EntityRendererProvider.Context param0, IllagerModel<T> param1, float param2) {
        super(param0, param1, param2);
        this.addLayer(new CustomHeadLayer<>(this, param0.getModelSet()));
    }

    protected void scale(T param0, PoseStack param1, float param2) {
        float var0 = 0.9375F;
        param1.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
