package net.minecraft.world.level.levelgen.placement;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public abstract class RepeatingPlacement extends PlacementModifier {
    protected abstract int count(Random var1, BlockPos var2);

    @Override
    public Stream<BlockPos> getPositions(PlacementContext param0, Random param1, BlockPos param2) {
        return IntStream.range(0, this.count(param1, param2)).mapToObj(param1x -> param2);
    }
}
