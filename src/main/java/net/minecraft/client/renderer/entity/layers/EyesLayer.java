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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class EyesLayer<T extends Entity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public EyesLayer(RenderLayerParent<T, M> param0) {
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
        VertexConsumer var0 = param1.getBuffer(RenderType.EYES(this.getTextureLocation()));
        OverlayTexture.setDefault(var0);
        this.getParentModel().renderToBuffer(param0, var0, 15728640);
        var0.unsetDefaultOverlayCoords();
    }

    public abstract ResourceLocation getTextureLocation();
}
