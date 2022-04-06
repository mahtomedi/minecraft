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

public class SpruceFoliagePlacer extends FoliagePlacer {
    public static final Codec<SpruceFoliagePlacer> CODEC = RecordCodecBuilder.create(
        param0 -> foliagePlacerParts(param0)
                .and(IntProvider.codec(0, 24).fieldOf("trunk_height").forGetter(param0x -> param0x.trunkHeight))
                .apply(param0, SpruceFoliagePlacer::new)
    );
    private final IntProvider trunkHeight;

    public SpruceFoliagePlacer(IntProvider param0, IntProvider param1, IntProvider param2) {
        super(param0, param1);
        this.trunkHeight = param2;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.SPRUCE_FOLIAGE_PLACER;
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
        BlockPos var0 = param5.pos();
        int var1 = param2.nextInt(2);
        int var2 = 1;
        int var3 = 0;

        for(int var4 = param8; var4 >= -param6; --var4) {
            this.placeLeavesRow(param0, param1, param2, param3, var0, var1, var4, param5.doubleTrunk());
            if (var1 >= var2) {
                var1 = var3;
                var3 = 1;
                var2 = Math.min(var2 + 1, param7 + param5.radiusOffset());
            } else {
                ++var1;
            }
        }

    }

    @Override
    public int foliageHeight(RandomSource param0, int param1, TreeConfiguration param2) {
        return Math.max(4, param1 - this.trunkHeight.sample(param0));
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource param0, int param1, int param2, int param3, int param4, boolean param5) {
        return param1 == param4 && param3 == param4 && param4 > 0;
    }
}
