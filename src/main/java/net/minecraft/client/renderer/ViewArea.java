package net.minecraft.client.renderer;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkFactory;
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
    public RenderChunk[] chunks;

    public ViewArea(Level param0, int param1, LevelRenderer param2, RenderChunkFactory param3) {
        this.levelRenderer = param2;
        this.level = param0;
        this.setViewDistance(param1);
        this.createChunks(param3);
    }

    protected void createChunks(RenderChunkFactory param0) {
        int var0 = this.chunkGridSizeX * this.chunkGridSizeY * this.chunkGridSizeZ;
        this.chunks = new RenderChunk[var0];

        for(int var1 = 0; var1 < this.chunkGridSizeX; ++var1) {
            for(int var2 = 0; var2 < this.chunkGridSizeY; ++var2) {
                for(int var3 = 0; var3 < this.chunkGridSizeZ; ++var3) {
                    int var4 = this.getChunkIndex(var1, var2, var3);
                    this.chunks[var4] = param0.create(this.level, this.levelRenderer);
                    this.chunks[var4].setOrigin(var1 * 16, var2 * 16, var3 * 16);
                }
            }
        }

    }

    public void releaseAllBuffers() {
        for(RenderChunk var0 : this.chunks) {
            var0.releaseBuffers();
        }

    }

    private int getChunkIndex(int param0, int param1, int param2) {
        return (param2 * this.chunkGridSizeY + param1) * this.chunkGridSizeX + param0;
    }

    protected void setViewDistance(int param0) {
        int var0 = param0 * 2 + 1;
        this.chunkGridSizeX = var0;
        this.chunkGridSizeY = 16;
        this.chunkGridSizeZ = var0;
    }

    public void repositionCamera(double param0, double param1) {
        int var0 = Mth.floor(param0) - 8;
        int var1 = Mth.floor(param1) - 8;
        int var2 = this.chunkGridSizeX * 16;

        for(int var3 = 0; var3 < this.chunkGridSizeX; ++var3) {
            int var4 = this.getCoordinate(var0, var2, var3);

            for(int var5 = 0; var5 < this.chunkGridSizeZ; ++var5) {
                int var6 = this.getCoordinate(var1, var2, var5);

                for(int var7 = 0; var7 < this.chunkGridSizeY; ++var7) {
                    int var8 = var7 * 16;
                    RenderChunk var9 = this.chunks[this.getChunkIndex(var3, var7, var5)];
                    var9.setOrigin(var4, var8, var6);
                }
            }
        }

    }

    private int getCoordinate(int param0, int param1, int param2) {
        int var0 = param2 * 16;
        int var1 = var0 - param0 + param1 / 2;
        if (var1 < 0) {
            var1 -= param1 - 1;
        }

        return var0 - var1 / param1 * param1;
    }

    public void setDirty(int param0, int param1, int param2, boolean param3) {
        int var0 = Math.floorMod(param0, this.chunkGridSizeX);
        int var1 = Math.floorMod(param1, this.chunkGridSizeY);
        int var2 = Math.floorMod(param2, this.chunkGridSizeZ);
        RenderChunk var3 = this.chunks[this.getChunkIndex(var0, var1, var2)];
        var3.setDirty(param3);
    }

    @Nullable
    protected RenderChunk getRenderChunkAt(BlockPos param0) {
        int var0 = Mth.intFloorDiv(param0.getX(), 16);
        int var1 = Mth.intFloorDiv(param0.getY(), 16);
        int var2 = Mth.intFloorDiv(param0.getZ(), 16);
        if (var1 >= 0 && var1 < this.chunkGridSizeY) {
            var0 = Mth.positiveModulo(var0, this.chunkGridSizeX);
            var2 = Mth.positiveModulo(var2, this.chunkGridSizeZ);
            return this.chunks[this.getChunkIndex(var0, var1, var2)];
        } else {
            return null;
        }
    }
}
