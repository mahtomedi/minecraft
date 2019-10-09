package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PowerableMob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class SpinnyLayer<T extends Entity & PowerableMob, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public SpinnyLayer(RenderLayerParent<T, M> param0) {
        super(param0);
    }

    @Override
    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        T param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9,
        float param10
    ) {
        if (param3.isPowered()) {
            float var0 = (float)param3.tickCount + param6;
            EntityModel<T> var1 = this.model();
            var1.prepareMobModel(param3, param4, param5, param6);
            this.getParentModel().copyPropertiesTo(var1);
            VertexConsumer var2 = param1.getBuffer(RenderType.powerSwirl(this.getTextureLocation(), this.xOffset(var0), var0 * 0.01F));
            var1.setupAnim(param3, param4, param5, param7, param8, param9, param10);
            var1.renderToBuffer(param0, var2, param2, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F);
        }
    }

    protected abstract float xOffset(float var1);

    protected abstract ResourceLocation getTextureLocation();

    protected abstract EntityModel<T> model();
}
