package net.minecraft.server.level;

import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class PlayerRespawnLogic {
    @Nullable
    protected static BlockPos getOverworldRespawnPos(ServerLevel param0, int param1, int param2) {
        boolean var0 = param0.dimensionType().hasCeiling();
        LevelChunk var1 = param0.getChunk(SectionPos.blockToSectionCoord(param1), SectionPos.blockToSectionCoord(param2));
        int var2 = var0
            ? param0.getChunkSource().getGenerator().getSpawnHeight(param0)
            : var1.getHeight(Heightmap.Types.MOTION_BLOCKING, param1 & 15, param2 & 15);
        if (var2 < param0.getMinBuildHeight()) {
            return null;
        } else {
            int var3 = var1.getHeight(Heightmap.Types.WORLD_SURFACE, param1 & 15, param2 & 15);
            if (var3 <= var2 && var3 > var1.getHeight(Heightmap.Types.OCEAN_FLOOR, param1 & 15, param2 & 15)) {
                return null;
            } else {
                BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

                for(int var5 = var2 + 1; var5 >= param0.getMinBuildHeight(); --var5) {
                    var4.set(param1, var5, param2);
                    BlockState var6 = param0.getBlockState(var4);
                    if (!var6.getFluidState().isEmpty()) {
                        break;
                    }

                    if (Block.isFaceFull(var6.getCollisionShape(param0, var4), Direction.UP)) {
                        return var4.above().immutable();
                    }
                }

                return null;
            }
        }
    }

    @Nullable
    public static BlockPos getSpawnPosInChunk(ServerLevel param0, ChunkPos param1) {
        if (SharedConstants.debugVoidTerrain(param1.getMinBlockX(), param1.getMinBlockZ())) {
            return null;
        } else {
            for(int var0 = param1.getMinBlockX(); var0 <= param1.getMaxBlockX(); ++var0) {
                for(int var1 = param1.getMinBlockZ(); var1 <= param1.getMaxBlockZ(); ++var1) {
                    BlockPos var2 = getOverworldRespawnPos(param0, var0, var1);
                    if (var2 != null) {
                        return var2;
                    }
                }
            }

            return null;
        }
    }
}
