package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.List;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ChunkRenderList {
    private double xOff;
    private double yOff;
    private double zOff;
    protected final List<RenderChunk> chunks = Lists.newArrayListWithCapacity(17424);
    protected boolean ready;

    public void setCameraLocation(double param0, double param1, double param2) {
        this.ready = true;
        this.chunks.clear();
        this.xOff = param0;
        this.yOff = param1;
        this.zOff = param2;
    }

    public void translateToRelativeChunkPosition(RenderChunk param0) {
        BlockPos var0 = param0.getOrigin();
        GlStateManager.translatef((float)((double)var0.getX() - this.xOff), (float)((double)var0.getY() - this.yOff), (float)((double)var0.getZ() - this.zOff));
    }

    public void add(RenderChunk param0, BlockLayer param1) {
        this.chunks.add(param0);
    }

    public abstract void render(BlockLayer var1);
}
