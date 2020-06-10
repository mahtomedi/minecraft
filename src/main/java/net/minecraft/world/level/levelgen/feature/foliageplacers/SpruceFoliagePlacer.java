package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class SpruceFoliagePlacer extends FoliagePlacer {
    public static final Codec<SpruceFoliagePlacer> CODEC = RecordCodecBuilder.create(
        param0 -> foliagePlacerParts(param0)
                .and(
                    param0.group(
                        Codec.INT.fieldOf("trunk_height").forGetter(param0x -> param0x.trunkHeight),
                        Codec.INT.fieldOf("trunk_height_random").forGetter(param0x -> param0x.trunkHeightRandom)
                    )
                )
                .apply(param0, SpruceFoliagePlacer::new)
    );
    private final int trunkHeight;
    private final int trunkHeightRandom;

    public SpruceFoliagePlacer(int param0, int param1, int param2, int param3, int param4, int param5) {
        super(param0, param1, param2, param3);
        this.trunkHeight = param4;
        this.trunkHeightRandom = param5;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.SPRUCE_FOLIAGE_PLACER;
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
        BlockPos var0 = param4.foliagePos();
        int var1 = param1.nextInt(2);
        int var2 = 1;
        int var3 = 0;

        for(int var4 = param8; var4 >= -param5; --var4) {
            this.placeLeavesRow(param0, param1, param2, var0, var1, param7, var4, param4.doubleTrunk(), param9);
            if (var1 >= var2) {
                var1 = var3;
                var3 = 1;
                var2 = Math.min(var2 + 1, param6 + param4.radiusOffset());
            } else {
                ++var1;
            }
        }

    }

    @Override
    public int foliageHeight(Random param0, int param1, TreeConfiguration param2) {
        return Math.max(4, param1 - this.trunkHeight - param0.nextInt(this.trunkHeightRandom + 1));
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
        return param1 == param4 && param3 == param4 && param4 > 0;
    }
}
