package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RenderLayer<T extends Entity, M extends EntityModel<T>> {
    private final RenderLayerParent<T, M> renderer;

    public RenderLayer(RenderLayerParent<T, M> param0) {
        this.renderer = param0;
    }

    public M getParentModel() {
        return this.renderer.getModel();
    }

    public void bindTexture(ResourceLocation param0) {
        this.renderer.bindTexture(param0);
    }

    public void setLightColor(T param0) {
        this.renderer.setLightColor(param0);
    }

    public abstract void render(T var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8);

    public abstract boolean colorsOnDamage();
}
