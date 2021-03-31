package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class MegaPineFoliagePlacer extends FoliagePlacer {
    public static final Codec<MegaPineFoliagePlacer> CODEC = RecordCodecBuilder.create(
        param0 -> foliagePlacerParts(param0)
                .and(IntProvider.codec(0, 24).fieldOf("crown_height").forGetter(param0x -> param0x.crownHeight))
                .apply(param0, MegaPineFoliagePlacer::new)
    );
    private final IntProvider crownHeight;

    public MegaPineFoliagePlacer(IntProvider param0, IntProvider param1, IntProvider param2) {
        super(param0, param1);
        this.crownHeight = param2;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.MEGA_PINE_FOLIAGE_PLACER;
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
        BlockPos var0 = param5.pos();
        int var1 = 0;

        for(int var2 = var0.getY() - param6 + param8; var2 <= var0.getY() + param8; ++var2) {
            int var3 = var0.getY() - var2;
            int var4 = param7 + param5.radiusOffset() + Mth.floor((float)var3 / (float)param6 * 3.5F);
            int var5;
            if (var3 > 0 && var4 == var1 && (var2 & 1) == 0) {
                var5 = var4 + 1;
            } else {
                var5 = var4;
            }

            this.placeLeavesRow(param0, param1, param2, param3, new BlockPos(var0.getX(), var2, var0.getZ()), var5, 0, param5.doubleTrunk());
            var1 = var4;
        }

    }

    @Override
    public int foliageHeight(Random param0, int param1, TreeConfiguration param2) {
        return this.crownHeight.sample(param0);
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
        if (param1 + param3 >= 7) {
            return true;
        } else {
            return param1 * param1 + param3 * param3 > param4 * param4;
        }
    }
}
