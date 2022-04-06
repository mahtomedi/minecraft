package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

public class TrunkVineDecorator extends TreeDecorator {
    public static final Codec<TrunkVineDecorator> CODEC = Codec.unit(() -> TrunkVineDecorator.INSTANCE);
    public static final TrunkVineDecorator INSTANCE = new TrunkVineDecorator();

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.TRUNK_VINE;
    }

    @Override
    public void place(
        LevelSimulatedReader param0,
        BiConsumer<BlockPos, BlockState> param1,
        RandomSource param2,
        List<BlockPos> param3,
        List<BlockPos> param4,
        List<BlockPos> param5
    ) {
        param3.forEach(param3x -> {
            if (param2.nextInt(3) > 0) {
                BlockPos var3x = param3x.west();
                if (Feature.isAir(param0, var3x)) {
                    placeVine(param1, var3x, VineBlock.EAST);
                }
            }

            if (param2.nextInt(3) > 0) {
                BlockPos var5x = param3x.east();
                if (Feature.isAir(param0, var5x)) {
                    placeVine(param1, var5x, VineBlock.WEST);
                }
            }

            if (param2.nextInt(3) > 0) {
                BlockPos var6x = param3x.north();
                if (Feature.isAir(param0, var6x)) {
                    placeVine(param1, var6x, VineBlock.SOUTH);
                }
            }

            if (param2.nextInt(3) > 0) {
                BlockPos var3 = param3x.south();
                if (Feature.isAir(param0, var3)) {
                    placeVine(param1, var3, VineBlock.NORTH);
                }
            }

        });
    }
}
