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

public class RandomSpreadFoliagePlacer extends FoliagePlacer {
    public static final Codec<RandomSpreadFoliagePlacer> CODEC = RecordCodecBuilder.create(
        param0 -> foliagePlacerParts(param0)
                .and(
                    param0.group(
                        UniformInt.codec(1, 256, 256).fieldOf("foliage_height").forGetter(param0x -> param0x.foliageHeight),
                        Codec.intRange(0, 256).fieldOf("leaf_placement_attempts").forGetter(param0x -> param0x.leafPlacementAttempts)
                    )
                )
                .apply(param0, RandomSpreadFoliagePlacer::new)
    );
    private final UniformInt foliageHeight;
    private final int leafPlacementAttempts;

    public RandomSpreadFoliagePlacer(UniformInt param0, UniformInt param1, UniformInt param2, int param3) {
        super(param0, param1);
        this.foliageHeight = param2;
        this.leafPlacementAttempts = param3;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.RANDOM_SPREAD_FOLIAGE_PLACER;
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
        BlockPos.MutableBlockPos var1 = var0.mutable();

        for(int var2 = 0; var2 < this.leafPlacementAttempts; ++var2) {
            var1.setWithOffset(
                var0,
                param1.nextInt(param6) - param1.nextInt(param6),
                param1.nextInt(param5) - param1.nextInt(param5),
                param1.nextInt(param6) - param1.nextInt(param6)
            );
            this.tryPlaceLeaf(param0, param1, param2, param7, param9, var1);
        }

    }

    @Override
    public int foliageHeight(Random param0, int param1, TreeConfiguration param2) {
        return this.foliageHeight.sample(param0);
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
        return false;
    }
}
