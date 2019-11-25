package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrownItemRenderer<T extends Entity & ItemSupplier> extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;
    private final float scale;
    private final boolean fullBright;

    public ThrownItemRenderer(EntityRenderDispatcher param0, ItemRenderer param1, float param2, boolean param3) {
        super(param0);
        this.itemRenderer = param1;
        this.scale = param2;
        this.fullBright = param3;
    }

    public ThrownItemRenderer(EntityRenderDispatcher param0, ItemRenderer param1) {
        this(param0, param1, 1.0F, false);
    }

    @Override
    protected int getBlockLightLevel(T param0, float param1) {
        return this.fullBright ? 15 : super.getBlockLightLevel(param0, param1);
    }

    @Override
    public void render(T param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        param3.scale(this.scale, this.scale, this.scale);
        param3.mulPose(this.entityRenderDispatcher.cameraOrientation());
        this.itemRenderer.renderStatic(param0.getItem(), ItemTransforms.TransformType.GROUND, param5, OverlayTexture.NO_OVERLAY, param3, param4);
        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public ResourceLocation getTextureLocation(Entity param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
