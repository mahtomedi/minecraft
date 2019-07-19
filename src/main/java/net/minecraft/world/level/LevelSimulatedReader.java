package net.minecraft.world.level;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public interface LevelSimulatedReader {
    boolean isStateAtPosition(BlockPos var1, Predicate<BlockState> var2);

    BlockPos getHeightmapPos(Heightmap.Types var1, BlockPos var2);
}
