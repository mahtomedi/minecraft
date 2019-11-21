package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class BlockEntityRenderer<T extends BlockEntity> {
    protected final BlockEntityRenderDispatcher renderer;

    public BlockEntityRenderer(BlockEntityRenderDispatcher param0) {
        this.renderer = param0;
    }

    public abstract void render(T var1, float var2, PoseStack var3, MultiBufferSource var4, int var5, int var6);

    public boolean shouldRenderOffScreen(T param0) {
        return false;
    }
}
