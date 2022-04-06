package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

@Deprecated
public class CountOnEveryLayerPlacement extends PlacementModifier {
    public static final Codec<CountOnEveryLayerPlacement> CODEC = IntProvider.codec(0, 256)
        .fieldOf("count")
        .xmap(CountOnEveryLayerPlacement::new, param0 -> param0.count)
        .codec();
    private final IntProvider count;

    private CountOnEveryLayerPlacement(IntProvider param0) {
        this.count = param0;
    }

    public static CountOnEveryLayerPlacement of(IntProvider param0) {
        return new CountOnEveryLayerPlacement(param0);
    }

    public static CountOnEveryLayerPlacement of(int param0) {
        return of(ConstantInt.of(param0));
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext param0, RandomSource param1, BlockPos param2) {
        Builder<BlockPos> var0 = Stream.builder();
        int var1 = 0;

        boolean var2;
        do {
            var2 = false;

            for(int var3 = 0; var3 < this.count.sample(param1); ++var3) {
                int var4 = param1.nextInt(16) + param2.getX();
                int var5 = param1.nextInt(16) + param2.getZ();
                int var6 = param0.getHeight(Heightmap.Types.MOTION_BLOCKING, var4, var5);
                int var7 = findOnGroundYPosition(param0, var4, var6, var5, var1);
                if (var7 != Integer.MAX_VALUE) {
                    var0.add(new BlockPos(var4, var7, var5));
                    var2 = true;
                }
            }

            ++var1;
        } while(var2);

        return var0.build();
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.COUNT_ON_EVERY_LAYER;
    }

    private static int findOnGroundYPosition(PlacementContext param0, int param1, int param2, int param3, int param4) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos(param1, param2, param3);
        int var1 = 0;
        BlockState var2 = param0.getBlockState(var0);

        for(int var3 = param2; var3 >= param0.getMinBuildHeight() + 1; --var3) {
            var0.setY(var3 - 1);
            BlockState var4 = param0.getBlockState(var0);
            if (!isEmpty(var4) && isEmpty(var2) && !var4.is(Blocks.BEDROCK)) {
                if (var1 == param4) {
                    return var0.getY() + 1;
                }

                ++var1;
            }

            var2 = var4;
        }

        return Integer.MAX_VALUE;
    }

    private static boolean isEmpty(BlockState param0) {
        return param0.isAir() || param0.is(Blocks.WATER) || param0.is(Blocks.LAVA);
    }
}
