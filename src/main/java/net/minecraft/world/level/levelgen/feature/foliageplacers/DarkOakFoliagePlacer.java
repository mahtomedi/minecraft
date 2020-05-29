package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class DarkOakFoliagePlacer extends FoliagePlacer {
    public static final Codec<DarkOakFoliagePlacer> CODEC = RecordCodecBuilder.create(
        param0 -> foliagePlacerParts(param0).apply(param0, DarkOakFoliagePlacer::new)
    );

    public DarkOakFoliagePlacer(int param0, int param1, int param2, int param3) {
        super(param0, param1, param2, param3);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.DARK_OAK_FOLIAGE_PLACER;
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
        int param8,
        BoundingBox param9
    ) {
        BlockPos var0 = param4.foliagePos().above(param8);
        boolean var1 = param4.doubleTrunk();
        if (var1) {
            this.placeLeavesRow(param0, param1, param2, var0, param6 + 2, param7, -1, var1, param9);
            this.placeLeavesRow(param0, param1, param2, var0, param6 + 3, param7, 0, var1, param9);
            this.placeLeavesRow(param0, param1, param2, var0, param6 + 2, param7, 1, var1, param9);
            if (param1.nextBoolean()) {
                this.placeLeavesRow(param0, param1, param2, var0, param6, param7, 2, var1, param9);
            }
        } else {
            this.placeLeavesRow(param0, param1, param2, var0, param6 + 2, param7, -1, var1, param9);
            this.placeLeavesRow(param0, param1, param2, var0, param6 + 1, param7, 0, var1, param9);
        }

    }

    @Override
    public int foliageHeight(Random param0, int param1, TreeConfiguration param2) {
        return 4;
    }

    @Override
    protected boolean shouldSkipLocationSigned(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
        return param2 != 0 || !param5 || param1 != -param4 && param1 < param4 || param3 != -param4 && param3 < param4
            ? super.shouldSkipLocationSigned(param0, param1, param2, param3, param4, param5)
            : true;
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
        if (param2 == -1 && !param5) {
            return param1 == param4 && param3 == param4;
        } else if (param2 == 1) {
            return param1 + param3 > param4 * 2 - 2;
        } else {
            return false;
        }
    }
}
