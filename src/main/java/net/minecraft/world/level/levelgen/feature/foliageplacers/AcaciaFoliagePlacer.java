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

public class AcaciaFoliagePlacer extends FoliagePlacer {
    public static final Codec<AcaciaFoliagePlacer> CODEC = RecordCodecBuilder.create(
        param0 -> foliagePlacerParts(param0).apply(param0, AcaciaFoliagePlacer::new)
    );

    public AcaciaFoliagePlacer(IntProvider param0, IntProvider param1) {
        super(param0, param1);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.ACACIA_FOLIAGE_PLACER;
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
        boolean var0 = param5.doubleTrunk();
        BlockPos var1 = param5.pos().above(param8);
        this.placeLeavesRow(param0, param1, param2, param3, var1, param7 + param5.radiusOffset(), -1 - param6, var0);
        this.placeLeavesRow(param0, param1, param2, param3, var1, param7 - 1, -param6, var0);
        this.placeLeavesRow(param0, param1, param2, param3, var1, param7 + param5.radiusOffset() - 1, 0, var0);
    }

    @Override
    public int foliageHeight(RandomSource param0, int param1, TreeConfiguration param2) {
        return 0;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource param0, int param1, int param2, int param3, int param4, boolean param5) {
        if (param2 == 0) {
            return (param1 > 1 || param3 > 1) && param1 != 0 && param3 != 0;
        } else {
            return param1 == param4 && param3 == param4 && param4 > 0;
        }
    }
}
