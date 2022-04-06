package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class AlterGroundDecorator extends TreeDecorator {
    public static final Codec<AlterGroundDecorator> CODEC = BlockStateProvider.CODEC
        .fieldOf("provider")
        .xmap(AlterGroundDecorator::new, param0 -> param0.provider)
        .codec();
    private final BlockStateProvider provider;

    public AlterGroundDecorator(BlockStateProvider param0) {
        this.provider = param0;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.ALTER_GROUND;
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
        List<BlockPos> var0 = Lists.newArrayList();
        if (param5.isEmpty()) {
            var0.addAll(param3);
        } else if (!param3.isEmpty() && param5.get(0).getY() == param3.get(0).getY()) {
            var0.addAll(param3);
            var0.addAll(param5);
        } else {
            var0.addAll(param5);
        }

        if (!var0.isEmpty()) {
            int var1 = var0.get(0).getY();
            var0.stream().filter(param1x -> param1x.getY() == var1).forEach(param3x -> {
                this.placeCircle(param0, param1, param2, param3x.west().north());
                this.placeCircle(param0, param1, param2, param3x.east(2).north());
                this.placeCircle(param0, param1, param2, param3x.west().south(2));
                this.placeCircle(param0, param1, param2, param3x.east(2).south(2));

                for(int var0x = 0; var0x < 5; ++var0x) {
                    int var1x = param2.nextInt(64);
                    int var2x = var1x % 8;
                    int var3x = var1x / 8;
                    if (var2x == 0 || var2x == 7 || var3x == 0 || var3x == 7) {
                        this.placeCircle(param0, param1, param2, param3x.offset(-3 + var2x, 0, -3 + var3x));
                    }
                }

            });
        }
    }

    private void placeCircle(LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, RandomSource param2, BlockPos param3) {
        for(int var0 = -2; var0 <= 2; ++var0) {
            for(int var1 = -2; var1 <= 2; ++var1) {
                if (Math.abs(var0) != 2 || Math.abs(var1) != 2) {
                    this.placeBlockAt(param0, param1, param2, param3.offset(var0, 0, var1));
                }
            }
        }

    }

    private void placeBlockAt(LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, RandomSource param2, BlockPos param3) {
        for(int var0 = 2; var0 >= -3; --var0) {
            BlockPos var1 = param3.above(var0);
            if (Feature.isGrassOrDirt(param0, var1)) {
                param1.accept(var1, this.provider.getState(param2, param3));
                break;
            }

            if (!Feature.isAir(param0, var1) && var0 < 0) {
                break;
            }
        }

    }
}
