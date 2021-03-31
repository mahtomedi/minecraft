package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class GiantTrunkPlacer extends TrunkPlacer {
    public static final Codec<GiantTrunkPlacer> CODEC = RecordCodecBuilder.create(param0 -> trunkPlacerParts(param0).apply(param0, GiantTrunkPlacer::new));

    public GiantTrunkPlacer(int param0, int param1, int param2) {
        super(param0, param1, param2);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.GIANT_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
        LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, Random param2, int param3, BlockPos param4, TreeConfiguration param5
    ) {
        BlockPos var0 = param4.below();
        setDirtAt(param0, param1, param2, var0, param5);
        setDirtAt(param0, param1, param2, var0.east(), param5);
        setDirtAt(param0, param1, param2, var0.south(), param5);
        setDirtAt(param0, param1, param2, var0.south().east(), param5);
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

        for(int var2 = 0; var2 < param3; ++var2) {
            placeLogIfFreeWithOffset(param0, param1, param2, var1, param5, param4, 0, var2, 0);
            if (var2 < param3 - 1) {
                placeLogIfFreeWithOffset(param0, param1, param2, var1, param5, param4, 1, var2, 0);
                placeLogIfFreeWithOffset(param0, param1, param2, var1, param5, param4, 1, var2, 1);
                placeLogIfFreeWithOffset(param0, param1, param2, var1, param5, param4, 0, var2, 1);
            }
        }

        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(param4.above(param3), 0, true));
    }

    private static void placeLogIfFreeWithOffset(
        LevelSimulatedReader param0,
        BiConsumer<BlockPos, BlockState> param1,
        Random param2,
        BlockPos.MutableBlockPos param3,
        TreeConfiguration param4,
        BlockPos param5,
        int param6,
        int param7,
        int param8
    ) {
        param3.setWithOffset(param5, param6, param7, param8);
        placeLogIfFree(param0, param1, param2, param3, param4);
    }
}
