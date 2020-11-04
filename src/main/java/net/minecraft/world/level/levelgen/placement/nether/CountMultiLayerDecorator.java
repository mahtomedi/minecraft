package net.minecraft.world.level.levelgen.placement.nether;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class CountMultiLayerDecorator extends FeatureDecorator<CountConfiguration> {
    public CountMultiLayerDecorator(Codec<CountConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, CountConfiguration param2, BlockPos param3) {
        List<BlockPos> var0 = Lists.newArrayList();
        int var1 = 0;

        boolean var2;
        do {
            var2 = false;

            for(int var3 = 0; var3 < param2.count().sample(param1); ++var3) {
                int var4 = param1.nextInt(16) + param3.getX();
                int var5 = param1.nextInt(16) + param3.getZ();
                int var6 = param0.getHeight(Heightmap.Types.MOTION_BLOCKING, var4, var5);
                int var7 = findOnGroundYPosition(param0, var4, var6, var5, var1);
                if (var7 != Integer.MAX_VALUE) {
                    var0.add(new BlockPos(var4, var7, var5));
                    var2 = true;
                }
            }

            ++var1;
        } while(var2);

        return var0.stream();
    }

    private static int findOnGroundYPosition(DecorationContext param0, int param1, int param2, int param3, int param4) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos(param1, param2, param3);
        int var1 = 0;
        BlockState var2 = param0.getBlockState(var0);

        for(int var3 = param2; var3 >= param0.getMaxBuildHeight() + 1; --var3) {
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
