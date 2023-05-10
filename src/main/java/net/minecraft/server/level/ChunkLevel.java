package net.minecraft.server.level;

import net.minecraft.world.level.chunk.ChunkStatus;

public class ChunkLevel {
    private static final int FULL_CHUNK_LEVEL = 33;
    private static final int BLOCK_TICKING_LEVEL = 32;
    private static final int ENTITY_TICKING_LEVEL = 31;
    public static final int MAX_LEVEL = 33 + ChunkStatus.maxDistance();

    public static ChunkStatus generationStatus(int param0) {
        return param0 < 33 ? ChunkStatus.FULL : ChunkStatus.getStatusAroundFullChunk(param0 - 33);
    }

    public static int byStatus(ChunkStatus param0) {
        return 33 + ChunkStatus.getDistance(param0);
    }

    public static FullChunkStatus fullStatus(int param0) {
        if (param0 <= 31) {
            return FullChunkStatus.ENTITY_TICKING;
        } else if (param0 <= 32) {
            return FullChunkStatus.BLOCK_TICKING;
        } else {
            return param0 <= 33 ? FullChunkStatus.FULL : FullChunkStatus.INACCESSIBLE;
        }
    }

    public static int byStatus(FullChunkStatus param0) {
        return switch(param0) {
            case INACCESSIBLE -> MAX_LEVEL;
            case FULL -> 33;
            case BLOCK_TICKING -> 32;
            case ENTITY_TICKING -> 31;
        };
    }

    public static boolean isEntityTicking(int param0) {
        return param0 <= 31;
    }

    public static boolean isBlockTicking(int param0) {
        return param0 <= 32;
    }

    public static boolean isLoaded(int param0) {
        return param0 <= MAX_LEVEL;
    }
}
