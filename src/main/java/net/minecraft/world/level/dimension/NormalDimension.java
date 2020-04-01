package net.minecraft.world.level.dimension;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NormalDimension extends Dimension {
    public NormalDimension(Level param0, DimensionType param1) {
        super(param0, param1, 0.0F);
    }

    @Override
    public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
        return this.level.getLevelData().getGeneratorProvider().create(this.level);
    }

    @Nullable
    @Override
    public BlockPos getSpawnPosInChunk(ChunkPos param0, boolean param1) {
        return getSpawnPosInChunkI(this.level, param0, param1);
    }

    @Nullable
    public static BlockPos getSpawnPosInChunkI(Level param0, ChunkPos param1, boolean param2) {
        for(int var0 = param1.getMinBlockX(); var0 <= param1.getMaxBlockX(); ++var0) {
            for(int var1 = param1.getMinBlockZ(); var1 <= param1.getMaxBlockZ(); ++var1) {
                BlockPos var2 = getValidSpawnPositionI(param0, var0, var1, param2);
                if (var2 != null) {
                    return var2;
                }
            }
        }

        return null;
    }

    @Nullable
    @Override
    public BlockPos getValidSpawnPosition(int param0, int param1, boolean param2) {
        return getValidSpawnPositionI(this.level, param0, param1, param2);
    }

    @Nullable
    public static BlockPos getValidSpawnPositionI(Level param0, int param1, int param2, boolean param3) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos(param1, 0, param2);
        Biome var1 = param0.getBiome(var0);
        BlockState var2 = var1.getSurfaceBuilderConfig().getTopMaterial();
        if (param3 && !var2.getBlock().is(BlockTags.VALID_SPAWN)) {
            return null;
        } else {
            LevelChunk var3 = param0.getChunk(param1 >> 4, param2 >> 4);
            int var4 = var3.getHeight(Heightmap.Types.MOTION_BLOCKING, param1 & 15, param2 & 15);
            if (var4 < 0) {
                return null;
            } else if (var3.getHeight(Heightmap.Types.WORLD_SURFACE, param1 & 15, param2 & 15)
                > var3.getHeight(Heightmap.Types.OCEAN_FLOOR, param1 & 15, param2 & 15)) {
                return null;
            } else {
                for(int var5 = var4 + 1; var5 >= 0; --var5) {
                    var0.set(param1, var5, param2);
                    BlockState var6 = param0.getBlockState(var0);
                    if (!var6.getFluidState().isEmpty()) {
                        break;
                    }

                    if (var6.equals(var2)) {
                        return var0.above().immutable();
                    }
                }

                return null;
            }
        }
    }

    @Override
    public float getTimeOfDay(long param0, float param1) {
        return getTimeOfDayI(param0, 24000.0);
    }

    public static float getTimeOfDayI(long param0, double param1) {
        double var0 = Mth.frac((double)param0 / param1 - 0.25);
        double var1 = 0.5 - Math.cos(var0 * Math.PI) / 2.0;
        return (float)(var0 * 2.0 + var1) / 3.0F;
    }

    @Override
    public boolean isNaturalDimension() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 param0, float param1) {
        return param0.multiply((double)(param1 * 0.94F + 0.06F), (double)(param1 * 0.94F + 0.06F), (double)(param1 * 0.91F + 0.09F));
    }

    @Override
    public boolean mayRespawn() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isFoggyAt(int param0, int param1) {
        return false;
    }
}
