package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class PineFoliagePlacer extends FoliagePlacer {
    public static final Codec<PineFoliagePlacer> CODEC = RecordCodecBuilder.create(
        param0 -> foliagePlacerParts(param0)
                .and(
                    param0.group(
                        Codec.INT.fieldOf("height").forGetter(param0x -> param0x.height),
                        Codec.INT.fieldOf("height_random").forGetter(param0x -> param0x.heightRandom)
                    )
                )
                .apply(param0, PineFoliagePlacer::new)
    );
    private final int height;
    private final int heightRandom;

    public PineFoliagePlacer(int param0, int param1, int param2, int param3, int param4, int param5) {
        super(param0, param1, param2, param3);
        this.height = param4;
        this.heightRandom = param5;
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
        int param8
    ) {
        int var0 = 0;

        for(int var1 = param8; var1 >= param8 - param5; --var1) {
            this.placeLeavesRow(param0, param1, param2, param4.foliagePos(), var0, param7, var1, param4.doubleTrunk());
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
        return this.height + param0.nextInt(this.heightRandom + 1);
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
        return param1 == param4 && param3 == param4 && param4 > 0;
    }
}
