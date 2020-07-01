package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class CocoaDecorator extends TreeDecorator {
    public static final Codec<CocoaDecorator> CODEC = Codec.FLOAT.fieldOf("probability").xmap(CocoaDecorator::new, param0 -> param0.probability).codec();
    private final float probability;

    public CocoaDecorator(float param0) {
        this.probability = param0;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.COCOA;
    }

    @Override
    public void place(WorldGenLevel param0, Random param1, List<BlockPos> param2, List<BlockPos> param3, Set<BlockPos> param4, BoundingBox param5) {
        if (!(param1.nextFloat() >= this.probability)) {
            int var0 = param2.get(0).getY();
            param2.stream()
                .filter(param1x -> param1x.getY() - var0 <= 2)
                .forEach(
                    param4x -> {
                        for(Direction var0x : Direction.Plane.HORIZONTAL) {
                            if (param1.nextFloat() <= 0.25F) {
                                Direction var1x = var0x.getOpposite();
                                BlockPos var2x = param4x.offset(var1x.getStepX(), 0, var1x.getStepZ());
                                if (Feature.isAir(param0, var2x)) {
                                    BlockState var3x = Blocks.COCOA
                                        .defaultBlockState()
                                        .setValue(CocoaBlock.AGE, Integer.valueOf(param1.nextInt(3)))
                                        .setValue(CocoaBlock.FACING, var0x);
                                    this.setBlock(param0, var2x, var3x, param4, param5);
                                }
                            }
                        }
        
                    }
                );
        }
    }
}
