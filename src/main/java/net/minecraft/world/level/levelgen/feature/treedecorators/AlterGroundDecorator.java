package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
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
    public void place(TreeDecorator.Context param0) {
        List<BlockPos> var0 = Lists.newArrayList();
        List<BlockPos> var1 = param0.roots();
        List<BlockPos> var2 = param0.logs();
        if (var1.isEmpty()) {
            var0.addAll(var2);
        } else if (!var2.isEmpty() && var1.get(0).getY() == var2.get(0).getY()) {
            var0.addAll(var2);
            var0.addAll(var1);
        } else {
            var0.addAll(var1);
        }

        if (!var0.isEmpty()) {
            int var3 = var0.get(0).getY();
            var0.stream().filter(param1 -> param1.getY() == var3).forEach(param1 -> {
                this.placeCircle(param0, param1.west().north());
                this.placeCircle(param0, param1.east(2).north());
                this.placeCircle(param0, param1.west().south(2));
                this.placeCircle(param0, param1.east(2).south(2));

                for(int var0x = 0; var0x < 5; ++var0x) {
                    int var1x = param0.random().nextInt(64);
                    int var2x = var1x % 8;
                    int var3x = var1x / 8;
                    if (var2x == 0 || var2x == 7 || var3x == 0 || var3x == 7) {
                        this.placeCircle(param0, param1.offset(-3 + var2x, 0, -3 + var3x));
                    }
                }

            });
        }
    }

    private void placeCircle(TreeDecorator.Context param0, BlockPos param1) {
        for(int var0 = -2; var0 <= 2; ++var0) {
            for(int var1 = -2; var1 <= 2; ++var1) {
                if (Math.abs(var0) != 2 || Math.abs(var1) != 2) {
                    this.placeBlockAt(param0, param1.offset(var0, 0, var1));
                }
            }
        }

    }

    private void placeBlockAt(TreeDecorator.Context param0, BlockPos param1) {
        for(int var0 = 2; var0 >= -3; --var0) {
            BlockPos var1 = param1.above(var0);
            if (Feature.isGrassOrDirt(param0.level(), var1)) {
                param0.setBlock(var1, this.provider.getState(param0.random(), param1));
                break;
            }

            if (!param0.isAir(var1) && var0 < 0) {
                break;
            }
        }

    }
}
