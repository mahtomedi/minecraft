package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

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
        LevelSimulatedRW param0, Random param1, int param2, BlockPos param3, Set<BlockPos> param4, BoundingBox param5, TreeConfiguration param6
    ) {
        BlockPos var0 = param3.below();
        setDirtAt(param0, var0);
        setDirtAt(param0, var0.east());
        setDirtAt(param0, var0.south());
        setDirtAt(param0, var0.south().east());
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

        for(int var2 = 0; var2 < param2; ++var2) {
            placeLogIfFreeWithOffset(param0, param1, var1, param4, param5, param6, param3, 0, var2, 0);
            if (var2 < param2 - 1) {
                placeLogIfFreeWithOffset(param0, param1, var1, param4, param5, param6, param3, 1, var2, 0);
                placeLogIfFreeWithOffset(param0, param1, var1, param4, param5, param6, param3, 1, var2, 1);
                placeLogIfFreeWithOffset(param0, param1, var1, param4, param5, param6, param3, 0, var2, 1);
            }
        }

        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(param3.above(param2), 0, true));
    }

    private static void placeLogIfFreeWithOffset(
        LevelSimulatedRW param0,
        Random param1,
        BlockPos.MutableBlockPos param2,
        Set<BlockPos> param3,
        BoundingBox param4,
        TreeConfiguration param5,
        BlockPos param6,
        int param7,
        int param8,
        int param9
    ) {
        param2.setWithOffset(param6, param7, param8, param9);
        placeLogIfFree(param0, param1, param2, param3, param4, param5);
    }
}
