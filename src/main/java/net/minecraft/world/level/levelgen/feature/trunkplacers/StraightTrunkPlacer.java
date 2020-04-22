package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class StraightTrunkPlacer extends TrunkPlacer {
    public StraightTrunkPlacer(int param0, int param1, int param2) {
        super(param0, param1, param2, TrunkPlacerType.STRAIGHT_TRUNK_PLACER);
    }

    public <T> StraightTrunkPlacer(Dynamic<T> param0) {
        this(param0.get("base_height").asInt(0), param0.get("height_rand_a").asInt(0), param0.get("height_rand_b").asInt(0));
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
        LevelSimulatedRW param0, Random param1, int param2, BlockPos param3, Set<BlockPos> param4, BoundingBox param5, TreeConfiguration param6
    ) {
        setDirtAt(param0, param3.below());

        for(int var0 = 0; var0 < param2; ++var0) {
            placeLog(param0, param1, param3.above(var0), param4, param5, param6);
        }

        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(param3.above(param2), 0, false));
    }
}
