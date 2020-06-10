package net.minecraft.server.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class PlayerRespawnLogic {
    @Nullable
    protected static BlockPos getOverworldRespawnPos(ServerLevel param0, int param1, int param2, boolean param3) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos(param1, 0, param2);
        Biome var1 = param0.getBiome(var0);
        boolean var2 = param0.dimensionType().hasCeiling();
        BlockState var3 = var1.getSurfaceBuilderConfig().getTopMaterial();
        if (param3 && !var3.getBlock().is(BlockTags.VALID_SPAWN)) {
            return null;
        } else {
            LevelChunk var4 = param0.getChunk(param1 >> 4, param2 >> 4);
            int var5 = var2
                ? param0.getChunkSource().getGenerator().getSpawnHeight()
                : var4.getHeight(Heightmap.Types.MOTION_BLOCKING, param1 & 15, param2 & 15);
            if (var5 < 0) {
                return null;
            } else {
                int var6 = var4.getHeight(Heightmap.Types.WORLD_SURFACE, param1 & 15, param2 & 15);
                if (var6 <= var5 && var6 > var4.getHeight(Heightmap.Types.OCEAN_FLOOR, param1 & 15, param2 & 15)) {
                    return null;
                } else {
                    for(int var7 = var5 + 1; var7 >= 0; --var7) {
                        var0.set(param1, var7, param2);
                        BlockState var8 = param0.getBlockState(var0);
                        if (!var8.getFluidState().isEmpty()) {
                            break;
                        }

                        if (var8.equals(var3)) {
                            return var0.above().immutable();
                        }
                    }

                    return null;
                }
            }
        }
    }

    @Nullable
    public static BlockPos getSpawnPosInChunk(ServerLevel param0, ChunkPos param1, boolean param2) {
        for(int var0 = param1.getMinBlockX(); var0 <= param1.getMaxBlockX(); ++var0) {
            for(int var1 = param1.getMinBlockZ(); var1 <= param1.getMaxBlockZ(); ++var1) {
                BlockPos var2 = getOverworldRespawnPos(param0, var0, var1, param2);
                if (var2 != null) {
                    return var2;
                }
            }
        }

        return null;
    }
}
