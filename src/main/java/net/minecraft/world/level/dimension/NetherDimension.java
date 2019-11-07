package net.minecraft.world.level.dimension;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.levelgen.NetherGeneratorSettings;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NetherDimension extends Dimension {
    private static final Vec3 NETHER_FOG_COLOR = new Vec3(0.2F, 0.03F, 0.03F);

    public NetherDimension(Level param0, DimensionType param1) {
        super(param0, param1, 0.1F);
        this.ultraWarm = true;
        this.hasCeiling = true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vec3 getFogColor(float param0, float param1) {
        return NETHER_FOG_COLOR;
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        NetherGeneratorSettings var0 = ChunkGeneratorType.CAVES.createSettings();
        var0.setDefaultBlock(Blocks.NETHERRACK.defaultBlockState());
        var0.setDefaultFluid(Blocks.LAVA.defaultBlockState());
        return ChunkGeneratorType.CAVES
            .create(this.level, BiomeSourceType.FIXED.create(BiomeSourceType.FIXED.createSettings(this.level.getLevelData()).setBiome(Biomes.NETHER)), var0);
    }

    @Override
    public boolean isNaturalDimension() {
        return false;
    }

    @Nullable
    @Override
    public BlockPos getSpawnPosInChunk(ChunkPos param0, boolean param1) {
        return null;
    }

    @Nullable
    @Override
    public BlockPos getValidSpawnPosition(int param0, int param1, boolean param2) {
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isFoggyAt(int param0, int param1) {
        return true;
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
