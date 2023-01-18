package net.minecraft.client.renderer;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ViewArea {
    protected final LevelRenderer levelRenderer;
    protected final Level level;
    protected int chunkGridSizeY;
    protected int chunkGridSizeX;
    protected int chunkGridSizeZ;
    public ChunkRenderDispatcher.RenderChunk[] chunks;

    public ViewArea(ChunkRenderDispatcher param0, Level param1, int param2, LevelRenderer param3) {
        this.levelRenderer = param3;
        this.level = param1;
        this.setViewDistance(param2);
        this.createChunks(param0);
    }

    protected void createChunks(ChunkRenderDispatcher param0) {
        if (!Minecraft.getInstance().isSameThread()) {
            throw new IllegalStateException("createChunks called from wrong thread: " + Thread.currentThread().getName());
        } else {
            int var0 = this.chunkGridSizeX * this.chunkGridSizeY * this.chunkGridSizeZ;
            this.chunks = new ChunkRenderDispatcher.RenderChunk[var0];

            for(int var1 = 0; var1 < this.chunkGridSizeX; ++var1) {
                for(int var2 = 0; var2 < this.chunkGridSizeY; ++var2) {
                    for(int var3 = 0; var3 < this.chunkGridSizeZ; ++var3) {
                        int var4 = this.getChunkIndex(var1, var2, var3);
                        this.chunks[var4] = param0.new RenderChunk(var4, var1 * 16, var2 * 16, var3 * 16);
                    }
                }
            }

        }
    }

    public void releaseAllBuffers() {
        for(ChunkRenderDispatcher.RenderChunk var0 : this.chunks) {
            var0.releaseBuffers();
        }

    }

    private int getChunkIndex(int param0, int param1, int param2) {
        return (param2 * this.chunkGridSizeY + param1) * this.chunkGridSizeX + param0;
    }

    protected void setViewDistance(int param0) {
        int var0 = param0 * 2 + 1;
        this.chunkGridSizeX = var0;
        this.chunkGridSizeY = this.level.getSectionsCount();
        this.chunkGridSizeZ = var0;
    }

    public void repositionCamera(double param0, double param1) {
        int var0 = Mth.ceil(param0);
        int var1 = Mth.ceil(param1);

        for(int var2 = 0; var2 < this.chunkGridSizeX; ++var2) {
            int var3 = this.chunkGridSizeX * 16;
            int var4 = var0 - 8 - var3 / 2;
            int var5 = var4 + Math.floorMod(var2 * 16 - var4, var3);

            for(int var6 = 0; var6 < this.chunkGridSizeZ; ++var6) {
                int var7 = this.chunkGridSizeZ * 16;
                int var8 = var1 - 8 - var7 / 2;
                int var9 = var8 + Math.floorMod(var6 * 16 - var8, var7);

                for(int var10 = 0; var10 < this.chunkGridSizeY; ++var10) {
                    int var11 = this.level.getMinBuildHeight() + var10 * 16;
                    ChunkRenderDispatcher.RenderChunk var12 = this.chunks[this.getChunkIndex(var2, var10, var6)];
                    BlockPos var13 = var12.getOrigin();
                    if (var5 != var13.getX() || var11 != var13.getY() || var9 != var13.getZ()) {
                        var12.setOrigin(var5, var11, var9);
                    }
                }
            }
        }

    }

    public void setDirty(int param0, int param1, int param2, boolean param3) {
        int var0 = Math.floorMod(param0, this.chunkGridSizeX);
        int var1 = Math.floorMod(param1 - this.level.getMinSection(), this.chunkGridSizeY);
        int var2 = Math.floorMod(param2, this.chunkGridSizeZ);
        ChunkRenderDispatcher.RenderChunk var3 = this.chunks[this.getChunkIndex(var0, var1, var2)];
        var3.setDirty(param3);
    }

    @Nullable
    protected ChunkRenderDispatcher.RenderChunk getRenderChunkAt(BlockPos param0) {
        int var0 = Mth.floorDiv(param0.getX(), 16);
        int var1 = Mth.floorDiv(param0.getY() - this.level.getMinBuildHeight(), 16);
        int var2 = Mth.floorDiv(param0.getZ(), 16);
        if (var1 >= 0 && var1 < this.chunkGridSizeY) {
            var0 = Mth.positiveModulo(var0, this.chunkGridSizeX);
            var2 = Mth.positiveModulo(var2, this.chunkGridSizeZ);
            return this.chunks[this.getChunkIndex(var0, var1, var2)];
        } else {
            return null;
        }
    }
}
