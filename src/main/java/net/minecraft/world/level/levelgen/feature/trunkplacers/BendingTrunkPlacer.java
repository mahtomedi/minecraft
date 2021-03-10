package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class BendingTrunkPlacer extends TrunkPlacer {
    public static final Codec<BendingTrunkPlacer> CODEC = RecordCodecBuilder.create(
        param0 -> trunkPlacerParts(param0)
                .and(
                    param0.group(
                        Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("min_height_for_leaves", 1).forGetter(param0x -> param0x.minHeightForLeaves),
                        UniformInt.codec(1, 32, 32).fieldOf("bend_length").forGetter(param0x -> param0x.bendLength)
                    )
                )
                .apply(param0, BendingTrunkPlacer::new)
    );
    private final int minHeightForLeaves;
    private final UniformInt bendLength;

    public BendingTrunkPlacer(int param0, int param1, int param2, int param3, UniformInt param4) {
        super(param0, param1, param2);
        this.minHeightForLeaves = param3;
        this.bendLength = param4;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.BENDING_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
        LevelSimulatedRW param0, Random param1, int param2, BlockPos param3, Set<BlockPos> param4, BoundingBox param5, TreeConfiguration param6
    ) {
        Direction var0 = Direction.Plane.HORIZONTAL.getRandomDirection(param1);
        int var1 = param2 - 1;
        BlockPos.MutableBlockPos var2 = param3.mutable();
        BlockPos var3 = var2.below();
        setDirtAt(param0, param1, var3, param6);
        List<FoliagePlacer.FoliageAttachment> var4 = Lists.newArrayList();

        for(int var5 = 0; var5 <= var1; ++var5) {
            if (var5 + 1 >= var1 + param1.nextInt(2)) {
                var2.move(var0);
            }

            if (TreeFeature.validTreePos(param0, var2)) {
                placeLog(param0, param1, var2, param4, param5, param6);
            }

            if (var5 >= this.minHeightForLeaves) {
                var4.add(new FoliagePlacer.FoliageAttachment(var2.immutable(), 0, false));
            }

            var2.move(Direction.UP);
        }

        int var6 = this.bendLength.sample(param1);

        for(int var7 = 0; var7 <= var6; ++var7) {
            if (TreeFeature.validTreePos(param0, var2)) {
                placeLog(param0, param1, var2, param4, param5, param6);
            }

            var4.add(new FoliagePlacer.FoliageAttachment(var2.immutable(), 0, false));
            var2.move(var0);
        }

        return var4;
    }
}
