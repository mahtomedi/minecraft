package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreezeEyesLayer extends RenderLayer<Breeze, BreezeModel<Breeze>> {
    private final ResourceLocation textureLoc;
    private final BreezeModel<Breeze> model;

    public BreezeEyesLayer(RenderLayerParent<Breeze, BreezeModel<Breeze>> param0, EntityModelSet param1, ResourceLocation param2) {
        super(param0);
        this.model = new BreezeModel<>(param1.bakeLayer(ModelLayers.BREEZE_EYES));
        this.textureLoc = param2;
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        Breeze param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9
    ) {
        this.model.prepareMobModel(param3, param4, param5, param6);
        this.getParentModel().copyPropertiesTo(this.model);
        VertexConsumer var0 = param1.getBuffer(RenderType.breezeEyes(this.textureLoc));
        this.model.setupAnim(param3, param4, param5, param7, param8, param9);
        this.model.root().render(param0, var0, param2, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected ResourceLocation getTextureLocation(Breeze param0) {
        return this.textureLoc;
    }
}
