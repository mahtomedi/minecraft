package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrownItemRenderer<T extends Entity & ItemSupplier> extends EntityRenderer<T> {
    private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25F;
    private final ItemRenderer itemRenderer;
    private final float scale;
    private final boolean fullBright;

    public ThrownItemRenderer(EntityRendererProvider.Context param0, float param1, boolean param2) {
        super(param0);
        this.itemRenderer = param0.getItemRenderer();
        this.scale = param1;
        this.fullBright = param2;
    }

    public ThrownItemRenderer(EntityRendererProvider.Context param0) {
        this(param0, 1.0F, false);
    }

    @Override
    protected int getBlockLightLevel(T param0, BlockPos param1) {
        return this.fullBright ? 15 : super.getBlockLightLevel(param0, param1);
    }

    @Override
    public void render(T param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        if (param0.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(param0) < 12.25)) {
            param3.pushPose();
            param3.scale(this.scale, this.scale, this.scale);
            param3.mulPose(this.entityRenderDispatcher.cameraOrientation());
            param3.mulPose(Axis.YP.rotationDegrees(180.0F));
            this.itemRenderer
                .renderStatic(param0.getItem(), ItemDisplayContext.GROUND, param5, OverlayTexture.NO_OVERLAY, param3, param4, param0.level(), param0.getId());
            param3.popPose();
            super.render(param0, param1, param2, param3, param4, param5);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Entity param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
