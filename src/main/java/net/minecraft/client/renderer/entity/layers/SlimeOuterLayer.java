package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SlimeOuterLayer<T extends Entity> extends RenderLayer<T, SlimeModel<T>> {
    private final EntityModel<T> model = new SlimeModel<>(0);

    public SlimeOuterLayer(RenderLayerParent<T, SlimeModel<T>> param0) {
        super(param0);
    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (!param0.isInvisible()) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableNormalize();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.getParentModel().copyPropertiesTo(this.model);
            this.model.render(param0, param1, param2, param4, param5, param6, param7);
            RenderSystem.disableBlend();
            RenderSystem.disableNormalize();
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return true;
    }
}
