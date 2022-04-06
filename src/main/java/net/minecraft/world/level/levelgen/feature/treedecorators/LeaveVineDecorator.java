package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;

public class LeaveVineDecorator extends TreeDecorator {
    public static final Codec<LeaveVineDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
        .fieldOf("probability")
        .xmap(LeaveVineDecorator::new, param0 -> param0.probability)
        .codec();
    private final float probability;

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.LEAVE_VINE;
    }

    public LeaveVineDecorator(float param0) {
        this.probability = param0;
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
        param4.forEach(param3x -> {
            if (param2.nextFloat() < this.probability) {
                BlockPos var3x = param3x.west();
                if (Feature.isAir(param0, var3x)) {
                    addHangingVine(param0, var3x, VineBlock.EAST, param1);
                }
            }

            if (param2.nextFloat() < this.probability) {
                BlockPos var6x = param3x.east();
                if (Feature.isAir(param0, var6x)) {
                    addHangingVine(param0, var6x, VineBlock.WEST, param1);
                }
            }

            if (param2.nextFloat() < this.probability) {
                BlockPos var2 = param3x.north();
                if (Feature.isAir(param0, var2)) {
                    addHangingVine(param0, var2, VineBlock.SOUTH, param1);
                }
            }

            if (param2.nextFloat() < this.probability) {
                BlockPos var3 = param3x.south();
                if (Feature.isAir(param0, var3)) {
                    addHangingVine(param0, var3, VineBlock.NORTH, param1);
                }
            }

        });
    }

    private static void addHangingVine(LevelSimulatedReader param0, BlockPos param1, BooleanProperty param2, BiConsumer<BlockPos, BlockState> param3) {
        placeVine(param3, param1, param2);
        int var0 = 4;

        for(BlockPos var5 = param1.below(); Feature.isAir(param0, var5) && var0 > 0; --var0) {
            placeVine(param3, var5, param2);
            var5 = var5.below();
        }

    }
}
