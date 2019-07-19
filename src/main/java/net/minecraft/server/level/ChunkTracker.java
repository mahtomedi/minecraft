package net.minecraft.server.level;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;

public abstract class ChunkTracker extends DynamicGraphMinFixedPoint {
    protected ChunkTracker(int param0, int param1, int param2) {
        super(param0, param1, param2);
    }

    @Override
    protected boolean isSource(long param0) {
        return param0 == ChunkPos.INVALID_CHUNK_POS;
    }

    @Override
    protected void checkNeighborsAfterUpdate(long param0, int param1, boolean param2) {
        ChunkPos var0 = new ChunkPos(param0);
        int var1 = var0.x;
        int var2 = var0.z;

        for(int var3 = -1; var3 <= 1; ++var3) {
            for(int var4 = -1; var4 <= 1; ++var4) {
                long var5 = ChunkPos.asLong(var1 + var3, var2 + var4);
                if (var5 != param0) {
                    this.checkNeighbor(param0, var5, param1, param2);
                }
            }
        }

    }

    @Override
    protected int getComputedLevel(long param0, long param1, int param2) {
        int var0 = param2;
        ChunkPos var1 = new ChunkPos(param0);
        int var2 = var1.x;
        int var3 = var1.z;

        for(int var4 = -1; var4 <= 1; ++var4) {
            for(int var5 = -1; var5 <= 1; ++var5) {
                long var6 = ChunkPos.asLong(var2 + var4, var3 + var5);
                if (var6 == param0) {
                    var6 = ChunkPos.INVALID_CHUNK_POS;
                }

                if (var6 != param1) {
                    int var7 = this.computeLevelFromNeighbor(var6, param0, this.getLevel(var6));
                    if (var0 > var7) {
                        var0 = var7;
                    }

                    if (var0 == 0) {
                        return var0;
                    }
                }
            }
        }

        return var0;
    }

    @Override
    protected int computeLevelFromNeighbor(long param0, long param1, int param2) {
        return param0 == ChunkPos.INVALID_CHUNK_POS ? this.getLevelFromSource(param1) : param2 + 1;
    }

    protected abstract int getLevelFromSource(long var1);

    public void update(long param0, int param1, boolean param2) {
        this.checkEdge(ChunkPos.INVALID_CHUNK_POS, param0, param1, param2);
    }
}
