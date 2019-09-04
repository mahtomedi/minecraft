package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
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
    public void render(T param0, double param1, double param2, double param3, float param4, float param5) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)param1, (float)param2, (float)param3);
        RenderSystem.enableRescaleNormal();
        RenderSystem.scalef(this.scale, this.scale, this.scale);
        RenderSystem.rotatef(-this.entityRenderDispatcher.playerRotY, 0.0F, 1.0F, 0.0F);
        RenderSystem.rotatef(
            (float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * this.entityRenderDispatcher.playerRotX, 1.0F, 0.0F, 0.0F
        );
        RenderSystem.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
        this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
        if (this.solidRender) {
            RenderSystem.enableColorMaterial();
            RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
        }

        this.itemRenderer.renderStatic(param0.getItem(), ItemTransforms.TransformType.GROUND);
        if (this.solidRender) {
            RenderSystem.tearDownSolidRenderingTextureCombine();
            RenderSystem.disableColorMaterial();
        }

        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    @Override
    protected ResourceLocation getTextureLocation(Entity param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
