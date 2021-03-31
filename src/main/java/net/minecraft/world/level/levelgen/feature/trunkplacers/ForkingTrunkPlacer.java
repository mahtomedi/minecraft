package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class ForkingTrunkPlacer extends TrunkPlacer {
    public static final Codec<ForkingTrunkPlacer> CODEC = RecordCodecBuilder.create(param0 -> trunkPlacerParts(param0).apply(param0, ForkingTrunkPlacer::new));

    public ForkingTrunkPlacer(int param0, int param1, int param2) {
        super(param0, param1, param2);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.FORKING_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
        LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, Random param2, int param3, BlockPos param4, TreeConfiguration param5
    ) {
        setDirtAt(param0, param1, param2, param4.below(), param5);
        List<FoliagePlacer.FoliageAttachment> var0 = Lists.newArrayList();
        Direction var1 = Direction.Plane.HORIZONTAL.getRandomDirection(param2);
        int var2 = param3 - param2.nextInt(4) - 1;
        int var3 = 3 - param2.nextInt(3);
        BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();
        int var5 = param4.getX();
        int var6 = param4.getZ();
        int var7 = 0;

        for(int var8 = 0; var8 < param3; ++var8) {
            int var9 = param4.getY() + var8;
            if (var8 >= var2 && var3 > 0) {
                var5 += var1.getStepX();
                var6 += var1.getStepZ();
                --var3;
            }

            if (placeLog(param0, param1, param2, var4.set(var5, var9, var6), param5)) {
                var7 = var9 + 1;
            }
        }

        var0.add(new FoliagePlacer.FoliageAttachment(new BlockPos(var5, var7, var6), 1, false));
        var5 = param4.getX();
        var6 = param4.getZ();
        Direction var10 = Direction.Plane.HORIZONTAL.getRandomDirection(param2);
        if (var10 != var1) {
            int var11 = var2 - param2.nextInt(2) - 1;
            int var12 = 1 + param2.nextInt(3);
            var7 = 0;

            for(int var13 = var11; var13 < param3 && var12 > 0; --var12) {
                if (var13 >= 1) {
                    int var14 = param4.getY() + var13;
                    var5 += var10.getStepX();
                    var6 += var10.getStepZ();
                    if (placeLog(param0, param1, param2, var4.set(var5, var14, var6), param5)) {
                        var7 = var14 + 1;
                    }
                }

                ++var13;
            }

            if (var7 > 1) {
                var0.add(new FoliagePlacer.FoliageAttachment(new BlockPos(var5, var7, var6), 0, false));
            }
        }

        return var0;
    }
}
