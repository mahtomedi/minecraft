package net.minecraft.world.level.dimension.special;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSourceSettings;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;

public abstract class SpecialDimensionBase extends Dimension {
    protected final Level level;

    public SpecialDimensionBase(Level param0, DimensionType param1, float param2) {
        super(param0, param1, param2);
        this.level = param0;
    }

    @Nullable
    @Override
    public BlockPos getSpawnPosInChunk(ChunkPos param0, boolean param1) {
        return NormalDimension.getSpawnPosInChunkI(this.level, param0, param1);
    }

    @Nullable
    @Override
    public BlockPos getValidSpawnPosition(int param0, int param1, boolean param2) {
        return NormalDimension.getValidSpawnPositionI(this.level, param0, param1, param2);
    }

    @Override
    public boolean isNaturalDimension() {
        return false;
    }

    @Override
    public boolean mayRespawn() {
        return false;
    }

    public static FixedBiomeSource fixedBiome(Biome param0) {
        FixedBiomeSourceSettings var0 = BiomeSourceType.FIXED.createSettings(0L).setBiome(param0);
        return new FixedBiomeSource(var0);
    }

    public static OverworldBiomeSource normalBiomes(long param0) {
        return BiomeSourceType.VANILLA_LAYERED.create(BiomeSourceType.VANILLA_LAYERED.createSettings(param0));
    }
}
