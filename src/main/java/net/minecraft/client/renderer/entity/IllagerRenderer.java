package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class IllagerRenderer<T extends AbstractIllager> extends MobRenderer<T, IllagerModel<T>> {
    protected IllagerRenderer(EntityRenderDispatcher param0, IllagerModel<T> param1, float param2) {
        super(param0, param1, param2);
        this.addLayer(new CustomHeadLayer<>(this));
    }

    public IllagerRenderer(EntityRenderDispatcher param0) {
        super(param0, new IllagerModel<>(0.0F, 0.0F, 64, 64), 0.5F);
        this.addLayer(new CustomHeadLayer<>(this));
    }

    protected void scale(T param0, float param1) {
        float var0 = 0.9375F;
        GlStateManager.scalef(0.9375F, 0.9375F, 0.9375F);
    }
}
