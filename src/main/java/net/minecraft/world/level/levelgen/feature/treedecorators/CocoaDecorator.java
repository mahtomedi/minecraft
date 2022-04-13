package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;

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
    public void place(TreeDecorator.Context param0) {
        RandomSource var0 = param0.random();
        if (!(var0.nextFloat() >= this.probability)) {
            List<BlockPos> var1 = param0.logs();
            int var2 = var1.get(0).getY();
            var1.stream()
                .filter(param1 -> param1.getY() - var2 <= 2)
                .forEach(
                    param2 -> {
                        for(Direction var0x : Direction.Plane.HORIZONTAL) {
                            if (var0.nextFloat() <= 0.25F) {
                                Direction var1x = var0x.getOpposite();
                                BlockPos var2x = param2.offset(var1x.getStepX(), 0, var1x.getStepZ());
                                if (param0.isAir(var2x)) {
                                    param0.setBlock(
                                        var2x,
                                        Blocks.COCOA
                                            .defaultBlockState()
                                            .setValue(CocoaBlock.AGE, Integer.valueOf(var0.nextInt(3)))
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
