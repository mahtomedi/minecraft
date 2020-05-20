package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class FancyFoliagePlacer extends BlobFoliagePlacer {
    public static final Codec<FancyFoliagePlacer> CODEC = RecordCodecBuilder.create(param0 -> blobParts(param0).apply(param0, FancyFoliagePlacer::new));

    public FancyFoliagePlacer(int param0, int param1, int param2, int param3, int param4) {
        super(param0, param1, param2, param3, param4, FoliagePlacerType.FANCY_FOLIAGE_PLACER);
    }

    @Override
    protected void createFoliage(
        LevelSimulatedRW param0,
        Random param1,
        TreeConfiguration param2,
        int param3,
        FoliagePlacer.FoliageAttachment param4,
        int param5,
        int param6,
        Set<BlockPos> param7,
        int param8
    ) {
        for(int var0 = param8; var0 >= param8 - param5; --var0) {
            int var1 = param6 + (var0 != param8 && var0 != param8 - param5 ? 1 : 0);
            this.placeLeavesRow(param0, param1, param2, param4.foliagePos(), var1, param7, var0, param4.doubleTrunk());
        }

    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
        return Mth.square((float)param1 + 0.5F) + Mth.square((float)param3 + 0.5F) > (float)(param4 * param4);
    }
}
