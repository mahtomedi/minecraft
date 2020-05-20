package net.minecraft.server.level;

import java.util.Random;
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
    private static BlockPos getOverworldRespawnPos(ServerLevel param0, int param1, int param2, boolean param3) {
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

    @Nullable
    private static BlockPos getEndRespawnPos(ServerLevel param0, long param1, int param2, int param3) {
        ChunkPos var0 = new ChunkPos(param2 >> 4, param3 >> 4);
        Random var1 = new Random(param1);
        BlockPos var2 = new BlockPos(var0.getMinBlockX() + var1.nextInt(15), 0, var0.getMaxBlockZ() + var1.nextInt(15));
        return param0.getTopBlockState(var2).getMaterial().blocksMotion() ? var2 : null;
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

    @Nullable
    protected static BlockPos validSpawnPosition(ServerLevel param0, BlockPos param1, int param2, int param3, int param4) {
        if (param0.dimensionType().isOverworld()) {
            return getOverworldRespawnPos(param0, param1.getX() + param3 - param2, param1.getZ() + param4 - param2, false);
        } else {
            return param0.dimensionType().isEnd()
                ? getEndRespawnPos(param0, param0.getSeed(), param1.getX() + param3 - param2, param1.getZ() + param4 - param2)
                : null;
        }
    }
}
