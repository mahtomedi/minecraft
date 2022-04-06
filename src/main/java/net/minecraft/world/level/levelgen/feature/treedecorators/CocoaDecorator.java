package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

public class CocoaDecorator extends TreeDecorator {
    public static final Codec<CocoaDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
        .fieldOf("probability")
        .xmap(CocoaDecorator::new, param0 -> param0.probability)
        .codec();
    private final float probability;

    public CocoaDecorator(float param0) {
        this.probability = param0;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.COCOA;
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
        if (!(param2.nextFloat() >= this.probability)) {
            int var0 = param3.get(0).getY();
            param3.stream()
                .filter(param1x -> param1x.getY() - var0 <= 2)
                .forEach(
                    param3x -> {
                        for(Direction var0x : Direction.Plane.HORIZONTAL) {
                            if (param2.nextFloat() <= 0.25F) {
                                Direction var1x = var0x.getOpposite();
                                BlockPos var2x = param3x.offset(var1x.getStepX(), 0, var1x.getStepZ());
                                if (Feature.isAir(param0, var2x)) {
                                    param1.accept(
                                        var2x,
                                        Blocks.COCOA
                                            .defaultBlockState()
                                            .setValue(CocoaBlock.AGE, Integer.valueOf(param2.nextInt(3)))
                                            .setValue(CocoaBlock.FACING, var0x)
                                    );
                                }
                            }
                        }
        
                    }
                );
        }
    }
}
