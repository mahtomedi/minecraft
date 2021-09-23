package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockFilterDecorator extends FeatureDecorator<BlockFilterConfiguration> {
    public BlockFilterDecorator(Codec<BlockFilterConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, BlockFilterConfiguration param2, BlockPos param3) {
        BlockState var0 = param0.getLevel().getBlockState(param3.offset(param2.offset()));

        for(Block var1 : param2.disallowed()) {
            if (var0.is(var1)) {
                return Stream.of();
            }
        }

        for(Block var2 : param2.allowed()) {
            if (var0.is(var2)) {
                return Stream.of(param3);
            }
        }

        return param2.allowed().isEmpty() ? Stream.of(param3) : Stream.of();
    }
}
