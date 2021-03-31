package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class BendingTrunkPlacer extends TrunkPlacer {
    public static final Codec<BendingTrunkPlacer> CODEC = RecordCodecBuilder.create(
        param0 -> trunkPlacerParts(param0)
                .and(
                    param0.group(
                        Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("min_height_for_leaves", 1).forGetter(param0x -> param0x.minHeightForLeaves),
                        IntProvider.codec(1, 64).fieldOf("bend_length").forGetter(param0x -> param0x.bendLength)
                    )
                )
                .apply(param0, BendingTrunkPlacer::new)
    );
    private final int minHeightForLeaves;
    private final IntProvider bendLength;

    public BendingTrunkPlacer(int param0, int param1, int param2, int param3, IntProvider param4) {
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
        LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, Random param2, int param3, BlockPos param4, TreeConfiguration param5
    ) {
        Direction var0 = Direction.Plane.HORIZONTAL.getRandomDirection(param2);
        int var1 = param3 - 1;
        BlockPos.MutableBlockPos var2 = param4.mutable();
        BlockPos var3 = var2.below();
        setDirtAt(param0, param1, param2, var3, param5);
        List<FoliagePlacer.FoliageAttachment> var4 = Lists.newArrayList();

        for(int var5 = 0; var5 <= var1; ++var5) {
            if (var5 + 1 >= var1 + param2.nextInt(2)) {
                var2.move(var0);
            }

            if (TreeFeature.validTreePos(param0, var2)) {
                placeLog(param0, param1, param2, var2, param5);
            }

            if (var5 >= this.minHeightForLeaves) {
                var4.add(new FoliagePlacer.FoliageAttachment(var2.immutable(), 0, false));
            }

            var2.move(Direction.UP);
        }

        int var6 = this.bendLength.sample(param2);

        for(int var7 = 0; var7 <= var6; ++var7) {
            if (TreeFeature.validTreePos(param0, var2)) {
                placeLog(param0, param1, param2, var2, param5);
            }

            var4.add(new FoliagePlacer.FoliageAttachment(var2.immutable(), 0, false));
            var2.move(var0);
        }

        return var4;
    }
}
