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

public class MegaJungleFoliagePlacer extends FoliagePlacer {
    public static final Codec<MegaJungleFoliagePlacer> CODEC = RecordCodecBuilder.create(
        param0 -> foliagePlacerParts(param0)
                .and(Codec.intRange(0, 16).fieldOf("height").forGetter(param0x -> param0x.height))
                .apply(param0, MegaJungleFoliagePlacer::new)
    );
    protected final int height;

    public MegaJungleFoliagePlacer(IntProvider param0, IntProvider param1, int param2) {
        super(param0, param1);
        this.height = param2;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.MEGA_JUNGLE_FOLIAGE_PLACER;
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
        int var0 = param5.doubleTrunk() ? param6 : 1 + param2.nextInt(2);

        for(int var1 = param8; var1 >= param8 - var0; --var1) {
            int var2 = param7 + param5.radiusOffset() + 1 - var1;
            this.placeLeavesRow(param0, param1, param2, param3, param5.pos(), var2, var1, param5.doubleTrunk());
        }

    }

    @Override
    public int foliageHeight(RandomSource param0, int param1, TreeConfiguration param2) {
        return this.height;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource param0, int param1, int param2, int param3, int param4, boolean param5) {
        if (param1 + param3 >= 7) {
            return true;
        } else {
            return param1 * param1 + param3 * param3 > param4 * param4;
        }
    }
}
