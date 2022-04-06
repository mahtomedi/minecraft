package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class DarkOakFoliagePlacer extends FoliagePlacer {
    public static final Codec<DarkOakFoliagePlacer> CODEC = RecordCodecBuilder.create(
        param0 -> foliagePlacerParts(param0).apply(param0, DarkOakFoliagePlacer::new)
    );

    public DarkOakFoliagePlacer(IntProvider param0, IntProvider param1) {
        super(param0, param1);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.DARK_OAK_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(
        LevelSimulatedReader param0,
        BiConsumer<BlockPos, BlockState> param1,
        RandomSource param2,
        TreeConfiguration param3,
        int param4,
        FoliagePlacer.FoliageAttachment param5,
        int param6,
        int param7,
        int param8
    ) {
        BlockPos var0 = param5.pos().above(param8);
        boolean var1 = param5.doubleTrunk();
        if (var1) {
            this.placeLeavesRow(param0, param1, param2, param3, var0, param7 + 2, -1, var1);
            this.placeLeavesRow(param0, param1, param2, param3, var0, param7 + 3, 0, var1);
            this.placeLeavesRow(param0, param1, param2, param3, var0, param7 + 2, 1, var1);
            if (param2.nextBoolean()) {
                this.placeLeavesRow(param0, param1, param2, param3, var0, param7, 2, var1);
            }
        } else {
            this.placeLeavesRow(param0, param1, param2, param3, var0, param7 + 2, -1, var1);
            this.placeLeavesRow(param0, param1, param2, param3, var0, param7 + 1, 0, var1);
        }

    }

    @Override
    public int foliageHeight(RandomSource param0, int param1, TreeConfiguration param2) {
        return 4;
    }

    @Override
    protected boolean shouldSkipLocationSigned(RandomSource param0, int param1, int param2, int param3, int param4, boolean param5) {
        return param2 != 0 || !param5 || param1 != -param4 && param1 < param4 || param3 != -param4 && param3 < param4
            ? super.shouldSkipLocationSigned(param0, param1, param2, param3, param4, param5)
            : true;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource param0, int param1, int param2, int param3, int param4, boolean param5) {
        if (param2 == -1 && !param5) {
            return param1 == param4 && param3 == param4;
        } else if (param2 == 1) {
            return param1 + param3 > param4 * 2 - 2;
        } else {
            return false;
        }
    }
}
