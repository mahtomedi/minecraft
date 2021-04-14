package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class PineFoliagePlacer extends FoliagePlacer {
    public static final Codec<PineFoliagePlacer> CODEC = RecordCodecBuilder.create(
        param0 -> foliagePlacerParts(param0)
                .and(IntProvider.codec(0, 24).fieldOf("height").forGetter(param0x -> param0x.height))
                .apply(param0, PineFoliagePlacer::new)
    );
    private final IntProvider height;

    public PineFoliagePlacer(IntProvider param0, IntProvider param1, IntProvider param2) {
        super(param0, param1);
        this.height = param2;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.PINE_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(
        LevelSimulatedReader param0,
        BiConsumer<BlockPos, BlockState> param1,
        Random param2,
        TreeConfiguration param3,
        int param4,
        FoliagePlacer.FoliageAttachment param5,
        int param6,
        int param7,
        int param8
    ) {
        int var0 = 0;

        for(int var1 = param8; var1 >= param8 - param6; --var1) {
            this.placeLeavesRow(param0, param1, param2, param3, param5.pos(), var0, var1, param5.doubleTrunk());
            if (var0 >= 1 && var1 == param8 - param6 + 1) {
                --var0;
            } else if (var0 < param7 + param5.radiusOffset()) {
                ++var0;
            }
        }

    }

    @Override
    public int foliageRadius(Random param0, int param1) {
        return super.foliageRadius(param0, param1) + param0.nextInt(Math.max(param1 + 1, 1));
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
