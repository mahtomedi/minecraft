package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.VineBlock;

public class TrunkVineDecorator extends TreeDecorator {
    public static final Codec<TrunkVineDecorator> CODEC = Codec.unit(() -> TrunkVineDecorator.INSTANCE);
    public static final TrunkVineDecorator INSTANCE = new TrunkVineDecorator();

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.TRUNK_VINE;
    }

    @Override
    public void place(TreeDecorator.Context param0) {
        RandomSource var0 = param0.random();
        param0.logs().forEach(param2 -> {
            if (var0.nextInt(3) > 0) {
                BlockPos var0x = param2.west();
                if (param0.isAir(var0x)) {
                    param0.placeVine(var0x, VineBlock.EAST);
                }
            }

            if (var0.nextInt(3) > 0) {
                BlockPos var1 = param2.east();
                if (param0.isAir(var1)) {
                    param0.placeVine(var1, VineBlock.WEST);
                }
            }

            if (var0.nextInt(3) > 0) {
                BlockPos var2 = param2.north();
                if (param0.isAir(var2)) {
                    param0.placeVine(var2, VineBlock.SOUTH);
                }
            }

            if (var0.nextInt(3) > 0) {
                BlockPos var3 = param2.south();
                if (param0.isAir(var3)) {
                    param0.placeVine(var3, VineBlock.NORTH);
                }
            }

        });
    }
}
