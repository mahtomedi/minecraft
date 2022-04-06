package net.minecraft.world.level.levelgen.placement;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public abstract class RepeatingPlacement extends PlacementModifier {
    protected abstract int count(RandomSource var1, BlockPos var2);

    @Override
    public Stream<BlockPos> getPositions(PlacementContext param0, RandomSource param1, BlockPos param2) {
        return IntStream.range(0, this.count(param1, param2)).mapToObj(param1x -> param2);
    }
}
