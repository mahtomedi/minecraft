package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class AcaciaFoliagePlacer extends FoliagePlacer {
    public static final Codec<AcaciaFoliagePlacer> CODEC = RecordCodecBuilder.create(
        param0 -> foliagePlacerParts(param0).apply(param0, AcaciaFoliagePlacer::new)
    );

    public AcaciaFoliagePlacer(UniformInt param0, UniformInt param1) {
        super(param0, param1);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.ACACIA_FOLIAGE_PLACER;
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
        boolean var0 = param4.doubleTrunk();
        BlockPos var1 = param4.foliagePos().above(param8);
        this.placeLeavesRow(param0, param1, param2, var1, param6 + param4.radiusOffset(), param7, -1 - param5, var0, param9);
        this.placeLeavesRow(param0, param1, param2, var1, param6 - 1, param7, -param5, var0, param9);
        this.placeLeavesRow(param0, param1, param2, var1, param6 + param4.radiusOffset() - 1, param7, 0, var0, param9);
    }

    @Override
    public int foliageHeight(Random param0, int param1, TreeConfiguration param2) {
        return 0;
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
        if (param2 == 0) {
            return (param1 > 1 || param3 > 1) && param1 != 0 && param3 != 0;
        } else {
            return param1 == param4 && param3 == param4 && param4 > 0;
        }
    }
}
