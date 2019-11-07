package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RenderLayer<T extends Entity, M extends EntityModel<T>> {
    private final RenderLayerParent<T, M> renderer;

    public RenderLayer(RenderLayerParent<T, M> param0) {
        this.renderer = param0;
    }

    protected static <T extends LivingEntity> void coloredCutoutModelCopyLayerRender(
        EntityModel<T> param0,
        EntityModel<T> param1,
        ResourceLocation param2,
        PoseStack param3,
        MultiBufferSource param4,
        int param5,
        T param6,
        float param7,
        float param8,
        float param9,
        float param10,
        float param11,
        float param12,
        float param13,
        float param14,
        float param15
    ) {
        if (!param6.isInvisible()) {
            param0.copyPropertiesTo(param1);
            param1.prepareMobModel(param6, param7, param8, param12);
            param1.setupAnim(param6, param7, param8, param9, param10, param11);
            renderColoredCutoutModel(param1, param2, param3, param4, param5, param6, param13, param14, param15);
        }

    }

    protected static <T extends LivingEntity> void renderColoredCutoutModel(
        EntityModel<T> param0,
        ResourceLocation param1,
        PoseStack param2,
        MultiBufferSource param3,
        int param4,
        T param5,
        float param6,
        float param7,
        float param8
    ) {
        VertexConsumer var0 = param3.getBuffer(RenderType.entityCutoutNoCull(param1));
        param0.renderToBuffer(param2, var0, param4, LivingEntityRenderer.getOverlayCoords(param5, 0.0F), param6, param7, param8);
    }

    public M getParentModel() {
        return this.renderer.getModel();
    }

    protected ResourceLocation getTextureLocation(T param0) {
        return this.renderer.getTextureLocation(param0);
    }

    public abstract void render(
        PoseStack var1, MultiBufferSource var2, int var3, T var4, float var5, float var6, float var7, float var8, float var9, float var10
    );
}
