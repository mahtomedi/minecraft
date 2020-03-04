package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface LevelReader extends BlockAndTintGetter, CollisionGetter, BiomeManager.NoiseBiomeSource {
    @Nullable
    ChunkAccess getChunk(int var1, int var2, ChunkStatus var3, boolean var4);

    @Deprecated
    boolean hasChunk(int var1, int var2);

    int getHeight(Heightmap.Types var1, int var2, int var3);

    int getSkyDarken();

    BiomeManager getBiomeManager();

    default Biome getBiome(BlockPos param0) {
        return this.getBiomeManager().getBiome(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    default int getBlockTint(BlockPos param0, ColorResolver param1) {
        return param1.getColor(this.getBiome(param0), (double)param0.getX(), (double)param0.getZ());
    }

    @Override
    default Biome getNoiseBiome(int param0, int param1, int param2) {
        ChunkAccess var0 = this.getChunk(param0 >> 2, param2 >> 2, ChunkStatus.BIOMES, false);
        return var0 != null && var0.getBiomes() != null
            ? var0.getBiomes().getNoiseBiome(param0, param1, param2)
            : this.getUncachedNoiseBiome(param0, param1, param2);
    }

    Biome getUncachedNoiseBiome(int var1, int var2, int var3);

    boolean isClientSide();

    int getSeaLevel();

    Dimension getDimension();

    default BlockPos getHeightmapPos(Heightmap.Types param0, BlockPos param1) {
        return new BlockPos(param1.getX(), this.getHeight(param0, param1.getX(), param1.getZ()), param1.getZ());
    }

    default boolean isEmptyBlock(BlockPos param0) {
        return this.getBlockState(param0).isAir();
    }

    default boolean canSeeSkyFromBelowWater(BlockPos param0) {
        if (param0.getY() >= this.getSeaLevel()) {
            return this.canSeeSky(param0);
        } else {
            BlockPos var0 = new BlockPos(param0.getX(), this.getSeaLevel(), param0.getZ());
            if (!this.canSeeSky(var0)) {
                return false;
            } else {
                for(BlockPos var4 = var0.below(); var4.getY() > param0.getY(); var4 = var4.below()) {
                    BlockState var1 = this.getBlockState(var4);
                    if (var1.getLightBlock(this, var4) > 0 && !var1.getMaterial().isLiquid()) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    @Deprecated
    default float getBrightness(BlockPos param0) {
        return this.getDimension().getBrightness(this.getMaxLocalRawBrightness(param0));
    }

    default int getDirectSignal(BlockPos param0, Direction param1) {
        return this.getBlockState(param0).getDirectSignal(this, param0, param1);
    }

    default ChunkAccess getChunk(BlockPos param0) {
        return this.getChunk(param0.getX() >> 4, param0.getZ() >> 4);
    }

    default ChunkAccess getChunk(int param0, int param1) {
        return this.getChunk(param0, param1, ChunkStatus.FULL, true);
    }

    default ChunkAccess getChunk(int param0, int param1, ChunkStatus param2) {
        return this.getChunk(param0, param1, param2, true);
    }

    @Nullable
    @Override
    default BlockGetter getChunkForCollisions(int param0, int param1) {
        return this.getChunk(param0, param1, ChunkStatus.EMPTY, false);
    }

    default boolean isWaterAt(BlockPos param0) {
        return this.getFluidState(param0).is(FluidTags.WATER);
    }

    default boolean containsAnyLiquid(AABB param0) {
        int var0 = Mth.floor(param0.minX);
        int var1 = Mth.ceil(param0.maxX);
        int var2 = Mth.floor(param0.minY);
        int var3 = Mth.ceil(param0.maxY);
        int var4 = Mth.floor(param0.minZ);
        int var5 = Mth.ceil(param0.maxZ);
        BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos();

        for(int var7 = var0; var7 < var1; ++var7) {
            for(int var8 = var2; var8 < var3; ++var8) {
                for(int var9 = var4; var9 < var5; ++var9) {
                    BlockState var10 = this.getBlockState(var6.set(var7, var8, var9));
                    if (!var10.getFluidState().isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    default int getMaxLocalRawBrightness(BlockPos param0) {
        return this.getMaxLocalRawBrightness(param0, this.getSkyDarken());
    }

    default int getMaxLocalRawBrightness(BlockPos param0, int param1) {
        return param0.getX() >= -30000000 && param0.getZ() >= -30000000 && param0.getX() < 30000000 && param0.getZ() < 30000000
            ? this.getRawBrightness(param0, param1)
            : 15;
    }

    @Deprecated
    default boolean hasChunkAt(BlockPos param0) {
        return this.hasChunk(param0.getX() >> 4, param0.getZ() >> 4);
    }

    @Deprecated
    default boolean hasChunksAt(BlockPos param0, BlockPos param1) {
        return this.hasChunksAt(param0.getX(), param0.getY(), param0.getZ(), param1.getX(), param1.getY(), param1.getZ());
    }

    @Deprecated
    default boolean hasChunksAt(int param0, int param1, int param2, int param3, int param4, int param5) {
        if (param4 >= 0 && param1 < 256) {
            param0 >>= 4;
            param2 >>= 4;
            param3 >>= 4;
            param5 >>= 4;

            for(int var0 = param0; var0 <= param3; ++var0) {
                for(int var1 = param2; var1 <= param5; ++var1) {
                    if (!this.hasChunk(var0, var1)) {
                        return false;
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }
}
