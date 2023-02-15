package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class CherryFoliagePlacer extends FoliagePlacer {
    public static final Codec<CherryFoliagePlacer> CODEC = RecordCodecBuilder.create(
        param0 -> foliagePlacerParts(param0)
                .and(
                    param0.group(
                        IntProvider.codec(4, 16).fieldOf("height").forGetter(param0x -> param0x.height),
                        Codec.floatRange(0.0F, 1.0F).fieldOf("wide_bottom_layer_hole_chance").forGetter(param0x -> param0x.wideBottomLayerHoleChance),
                        Codec.floatRange(0.0F, 1.0F).fieldOf("corner_hole_chance").forGetter(param0x -> param0x.wideBottomLayerHoleChance),
                        Codec.floatRange(0.0F, 1.0F).fieldOf("hanging_leaves_chance").forGetter(param0x -> param0x.hangingLeavesChance),
                        Codec.floatRange(0.0F, 1.0F).fieldOf("hanging_leaves_extension_chance").forGetter(param0x -> param0x.hangingLeavesExtensionChance)
                    )
                )
                .apply(param0, CherryFoliagePlacer::new)
    );
    private final IntProvider height;
    private final float wideBottomLayerHoleChance;
    private final float cornerHoleChance;
    private final float hangingLeavesChance;
    private final float hangingLeavesExtensionChance;

    public CherryFoliagePlacer(IntProvider param0, IntProvider param1, IntProvider param2, float param3, float param4, float param5, float param6) {
        super(param0, param1);
        this.height = param2;
        this.wideBottomLayerHoleChance = param3;
        this.cornerHoleChance = param4;
        this.hangingLeavesChance = param5;
        this.hangingLeavesExtensionChance = param6;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.CHERRY_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(
        LevelSimulatedReader param0,
        FoliagePlacer.FoliageSetter param1,
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
        int var2 = param7 + param5.radiusOffset() - 1;
        this.placeLeavesRow(param0, param1, param2, param3, var1, var2 - 2, param6 - 3, var0);
        this.placeLeavesRow(param0, param1, param2, param3, var1, var2 - 1, param6 - 4, var0);

        for(int var3 = param6 - 5; var3 >= 0; --var3) {
            this.placeLeavesRow(param0, param1, param2, param3, var1, var2, var3, var0);
        }

        this.placeLeavesRowWithHangingLeavesBelow(
            param0, param1, param2, param3, var1, var2, -1, var0, this.hangingLeavesChance, this.hangingLeavesExtensionChance
        );
        this.placeLeavesRowWithHangingLeavesBelow(
            param0, param1, param2, param3, var1, var2 - 1, -2, var0, this.hangingLeavesChance, this.hangingLeavesExtensionChance
        );
    }

    @Override
    public int foliageHeight(RandomSource param0, int param1, TreeConfiguration param2) {
        return this.height.sample(param0);
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource param0, int param1, int param2, int param3, int param4, boolean param5) {
        if (param2 == -1 && (param1 == param4 || param3 == param4) && param0.nextFloat() < this.wideBottomLayerHoleChance) {
            return true;
        } else {
            boolean var0 = param1 == param4 && param3 == param4;
            boolean var1 = param4 > 2;
            if (var1) {
                return var0 || param1 + param3 > param4 * 2 - 2 && param0.nextFloat() < this.cornerHoleChance;
            } else {
                return var0 && param0.nextFloat() < this.cornerHoleChance;
            }
        }
    }
}
