package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class BlockEntityRenderer<T extends BlockEntity> {
    protected final BlockEntityRenderDispatcher renderer;

    public BlockEntityRenderer(BlockEntityRenderDispatcher param0) {
        this.renderer = param0;
    }

    public abstract void render(T var1, double var2, double var4, double var6, float var8, PoseStack var9, MultiBufferSource var10, int var11, int var12);

    protected TextureAtlasSprite getSprite(ResourceLocation param0) {
        return Minecraft.getInstance().getTextureAtlas().getSprite(param0);
    }

    public boolean shouldRenderOffScreen(T param0) {
        return false;
    }
}
