package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
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

    public ThrownItemRenderer(EntityRenderDispatcher param0, ItemRenderer param1, float param2) {
        super(param0);
        this.itemRenderer = param1;
        this.scale = param2;
    }

    public ThrownItemRenderer(EntityRenderDispatcher param0, ItemRenderer param1) {
        this(param0, param1, 1.0F);
    }

    @Override
    public void render(T param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7) {
        param6.pushPose();
        param6.scale(this.scale, this.scale, this.scale);
        param6.mulPose(Vector3f.YP.rotation(-this.entityRenderDispatcher.playerRotY, true));
        param6.mulPose(
            Vector3f.XP.rotation((float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * this.entityRenderDispatcher.playerRotX, true)
        );
        param6.mulPose(Vector3f.YP.rotation(180.0F, true));
        this.itemRenderer.renderStatic(param0.getItem(), ItemTransforms.TransformType.GROUND, param0.getLightColor(), param6, param7);
        param6.popPose();
        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    @Override
    public ResourceLocation getTextureLocation(Entity param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
