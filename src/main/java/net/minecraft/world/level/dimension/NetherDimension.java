package net.minecraft.world.level.dimension;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;

public class NetherDimension extends Dimension {
    public NetherDimension(Level param0, DimensionType param1) {
        super(param0, param1, 0.1F);
    }

    @Override
    public boolean isNaturalDimension() {
        return false;
    }

    @Nullable
    @Override
    public BlockPos getSpawnPosInChunk(long param0, ChunkPos param1, boolean param2) {
        return null;
    }

    @Nullable
    @Override
    public BlockPos getValidSpawnPosition(long param0, int param1, int param2, boolean param3) {
        return null;
    }

    @Override
    public float getTimeOfDay(long param0, float param1) {
        return 0.5F;
    }

    @Override
    public boolean mayRespawn() {
        return false;
    }

    @Override
    public WorldBorder createWorldBorder() {
        return new WorldBorder() {
            @Override
            public double getCenterX() {
                return super.getCenterX() / 8.0;
            }

            @Override
            public double getCenterZ() {
                return super.getCenterZ() / 8.0;
            }
        };
    }

    @Override
    public DimensionType getType() {
        return DimensionType.NETHER;
    }
}
