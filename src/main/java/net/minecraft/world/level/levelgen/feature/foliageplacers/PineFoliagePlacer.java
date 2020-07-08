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

public class PineFoliagePlacer extends FoliagePlacer {
    public static final Codec<PineFoliagePlacer> CODEC = RecordCodecBuilder.create(
        param0 -> foliagePlacerParts(param0)
                .and(UniformInt.codec(0, 16, 8).fieldOf("height").forGetter(param0x -> param0x.height))
                .apply(param0, PineFoliagePlacer::new)
    );
    private final UniformInt height;

    public PineFoliagePlacer(UniformInt param0, UniformInt param1, UniformInt param2) {
        super(param0, param1);
        this.height = param2;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.PINE_FOLIAGE_PLACER;
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
        int var0 = 0;

        for(int var1 = param8; var1 >= param8 - param5; --var1) {
            this.placeLeavesRow(param0, param1, param2, param4.foliagePos(), var0, param7, var1, param4.doubleTrunk(), param9);
            if (var0 >= 1 && var1 == param8 - param5 + 1) {
                --var0;
            } else if (var0 < param6 + param4.radiusOffset()) {
                ++var0;
            }
        }

    }

    @Override
    public int foliageRadius(Random param0, int param1) {
        return super.foliageRadius(param0, param1) + param0.nextInt(param1 + 1);
    }

    @Override
    public int foliageHeight(Random param0, int param1, TreeConfiguration param2) {
        return this.height.sample(param0);
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
        return param1 == param4 && param3 == param4 && param4 > 0;
    }
}
