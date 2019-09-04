package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class BlockEntityRenderer<T extends BlockEntity> {
    public static final ResourceLocation[] BREAKING_LOCATIONS = new ResourceLocation[]{
        new ResourceLocation("textures/" + ModelBakery.DESTROY_STAGE_0.getPath() + ".png"),
        new ResourceLocation("textures/" + ModelBakery.DESTROY_STAGE_1.getPath() + ".png"),
        new ResourceLocation("textures/" + ModelBakery.DESTROY_STAGE_2.getPath() + ".png"),
        new ResourceLocation("textures/" + ModelBakery.DESTROY_STAGE_3.getPath() + ".png"),
        new ResourceLocation("textures/" + ModelBakery.DESTROY_STAGE_4.getPath() + ".png"),
        new ResourceLocation("textures/" + ModelBakery.DESTROY_STAGE_5.getPath() + ".png"),
        new ResourceLocation("textures/" + ModelBakery.DESTROY_STAGE_6.getPath() + ".png"),
        new ResourceLocation("textures/" + ModelBakery.DESTROY_STAGE_7.getPath() + ".png"),
        new ResourceLocation("textures/" + ModelBakery.DESTROY_STAGE_8.getPath() + ".png"),
        new ResourceLocation("textures/" + ModelBakery.DESTROY_STAGE_9.getPath() + ".png")
    };
    protected BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    public void render(T param0, double param1, double param2, double param3, float param4, int param5) {
        HitResult var0 = this.blockEntityRenderDispatcher.cameraHitResult;
        if (param0 instanceof Nameable
            && var0 != null
            && var0.getType() == HitResult.Type.BLOCK
            && param0.getBlockPos().equals(((BlockHitResult)var0).getBlockPos())) {
            this.setOverlayRenderState(true);
            this.renderNameTag(param0, ((Nameable)param0).getDisplayName().getColoredString(), param1, param2, param3, 12);
            this.setOverlayRenderState(false);
        }

    }

    protected void setOverlayRenderState(boolean param0) {
        RenderSystem.activeTexture(33985);
        if (param0) {
            RenderSystem.disableTexture();
        } else {
            RenderSystem.enableTexture();
        }

        RenderSystem.activeTexture(33984);
    }

    protected void bindTexture(ResourceLocation param0) {
        TextureManager var0 = this.blockEntityRenderDispatcher.textureManager;
        if (var0 != null) {
            var0.bind(param0);
        }

    }

    protected Level getLevel() {
        return this.blockEntityRenderDispatcher.level;
    }

    public void init(BlockEntityRenderDispatcher param0) {
        this.blockEntityRenderDispatcher = param0;
    }

    public Font getFont() {
        return this.blockEntityRenderDispatcher.getFont();
    }

    public boolean shouldRenderOffScreen(T param0) {
        return false;
    }

    protected void renderNameTag(T param0, String param1, double param2, double param3, double param4, int param5) {
        Camera var0 = this.blockEntityRenderDispatcher.camera;
        double var1 = param0.distanceToSqr(var0.getPosition().x, var0.getPosition().y, var0.getPosition().z);
        if (!(var1 > (double)(param5 * param5))) {
            float var2 = var0.getYRot();
            float var3 = var0.getXRot();
            GameRenderer.renderNameTagInWorld(this.getFont(), param1, (float)param2 + 0.5F, (float)param3 + 1.5F, (float)param4 + 0.5F, 0, var2, var3, false);
        }
    }
}
