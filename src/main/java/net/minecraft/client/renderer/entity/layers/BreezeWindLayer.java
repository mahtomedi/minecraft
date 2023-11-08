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
public class BreezeWindLayer extends RenderLayer<Breeze, BreezeModel<Breeze>> {
    private static final float TOP_PART_ALPHA = 1.0F;
    private static final float MIDDLE_PART_ALPHA = 1.0F;
    private static final float BOTTOM_PART_ALPHA = 1.0F;
    private final ResourceLocation textureLoc;
    private final BreezeModel<Breeze> model;

    public BreezeWindLayer(RenderLayerParent<Breeze, BreezeModel<Breeze>> param0, EntityModelSet param1, ResourceLocation param2) {
        super(param0);
        this.model = new BreezeModel<>(param1.bakeLayer(ModelLayers.BREEZE_WIND));
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
        float var0 = (float)param3.tickCount + param6;
        this.model.prepareMobModel(param3, param4, param5, param6);
        this.getParentModel().copyPropertiesTo(this.model);
        VertexConsumer var1 = param1.getBuffer(RenderType.breezeWind(this.getTextureLocation(param3), this.xOffset(var0) % 1.0F, 0.0F));
        this.model.setupAnim(param3, param4, param5, param7, param8, param9);
        this.model.windTop().skipDraw = true;
        this.model.windMiddle().skipDraw = true;
        this.model.windBottom().skipDraw = false;
        this.model.root().render(param0, var1, param2, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        this.model.windTop().skipDraw = true;
        this.model.windMiddle().skipDraw = false;
        this.model.windBottom().skipDraw = true;
        this.model.root().render(param0, var1, param2, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        this.model.windTop().skipDraw = false;
        this.model.windMiddle().skipDraw = true;
        this.model.windBottom().skipDraw = true;
        this.model.root().render(param0, var1, param2, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private float xOffset(float param0) {
        return param0 * 0.02F;
    }

    protected ResourceLocation getTextureLocation(Breeze param0) {
        return this.textureLoc;
    }
}
