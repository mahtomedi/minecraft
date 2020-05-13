package net.minecraft.world.level.dimension;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class NormalDimension extends Dimension {
    public NormalDimension(Level param0, DimensionType param1) {
        super(param0, param1, 0.0F);
    }

    @Override
    public DimensionType getType() {
        return DimensionType.OVERWORLD;
    }

    @Nullable
    @Override
    public BlockPos getSpawnPosInChunk(long param0, ChunkPos param1, boolean param2) {
        for(int var0 = param1.getMinBlockX(); var0 <= param1.getMaxBlockX(); ++var0) {
            for(int var1 = param1.getMinBlockZ(); var1 <= param1.getMaxBlockZ(); ++var1) {
                BlockPos var2 = this.getValidSpawnPosition(param0, var0, var1, param2);
                if (var2 != null) {
                    return var2;
                }
            }
        }

        return null;
    }

    @Nullable
    @Override
    public BlockPos getValidSpawnPosition(long param0, int param1, int param2, boolean param3) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos(param1, 0, param2);
        Biome var1 = this.level.getBiome(var0);
        BlockState var2 = var1.getSurfaceBuilderConfig().getTopMaterial();
        if (param3 && !var2.getBlock().is(BlockTags.VALID_SPAWN)) {
            return null;
        } else {
            LevelChunk var3 = this.level.getChunk(param1 >> 4, param2 >> 4);
            int var4 = var3.getHeight(Heightmap.Types.MOTION_BLOCKING, param1 & 15, param2 & 15);
            if (var4 < 0) {
                return null;
            } else if (var3.getHeight(Heightmap.Types.WORLD_SURFACE, param1 & 15, param2 & 15)
                > var3.getHeight(Heightmap.Types.OCEAN_FLOOR, param1 & 15, param2 & 15)) {
                return null;
            } else {
                for(int var5 = var4 + 1; var5 >= 0; --var5) {
                    var0.set(param1, var5, param2);
                    BlockState var6 = this.level.getBlockState(var0);
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
        double var0 = Mth.frac((double)param0 / 24000.0 - 0.25);
        double var1 = 0.5 - Math.cos(var0 * Math.PI) / 2.0;
        return (float)(var0 * 2.0 + var1) / 3.0F;
    }

    @Override
    public boolean isNaturalDimension() {
        return true;
    }

    @Override
    public boolean mayRespawn() {
        return true;
    }
}
