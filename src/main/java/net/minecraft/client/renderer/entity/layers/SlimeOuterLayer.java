package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SlimeOuterLayer<T extends LivingEntity> extends RenderLayer<T, SlimeModel<T>> {
    private final EntityModel<T> model = new SlimeModel<>(0);

    public SlimeOuterLayer(RenderLayerParent<T, SlimeModel<T>> param0) {
        super(param0);
    }

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
        if (!param3.isInvisible()) {
            this.getParentModel().copyPropertiesTo(this.model);
            this.model.prepareMobModel(param3, param4, param5, param6);
            this.model.setupAnim(param3, param4, param5, param7, param8, param9, param10);
            VertexConsumer var0 = param1.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(param3)));
            this.model.renderToBuffer(param0, var0, param2, LivingEntityRenderer.getOverlayCoords(param3, 0.0F), 1.0F, 1.0F, 1.0F);
        }
    }
}
