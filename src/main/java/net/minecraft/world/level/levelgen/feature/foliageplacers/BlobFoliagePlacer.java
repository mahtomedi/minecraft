package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products.P5;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class BlobFoliagePlacer extends FoliagePlacer {
    public static final Codec<BlobFoliagePlacer> CODEC = RecordCodecBuilder.create(param0 -> blobParts(param0).apply(param0, BlobFoliagePlacer::new));
    protected final int height;

    protected static <P extends BlobFoliagePlacer> P5<Mu<P>, Integer, Integer, Integer, Integer, Integer> blobParts(Instance<P> param0) {
        return foliagePlacerParts(param0).and(Codec.INT.fieldOf("height").forGetter(param0x -> param0x.height));
    }

    protected BlobFoliagePlacer(int param0, int param1, int param2, int param3, int param4, FoliagePlacerType<?> param5) {
        super(param0, param1, param2, param3);
        this.height = param4;
    }

    public BlobFoliagePlacer(int param0, int param1, int param2, int param3, int param4) {
        this(param0, param1, param2, param3, param4, FoliagePlacerType.BLOB_FOLIAGE_PLACER);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.BLOB_FOLIAGE_PLACER;
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
        for(int var0 = param8; var0 >= param8 - param5; --var0) {
            int var1 = Math.max(param6 + param4.radiusOffset() - 1 - var0 / 2, 0);
            this.placeLeavesRow(param0, param1, param2, param4.foliagePos(), var1, param7, var0, param4.doubleTrunk());
        }

    }

    @Override
    public int foliageHeight(Random param0, int param1, TreeConfiguration param2) {
        return this.height;
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
        return param1 == param4 && param3 == param4 && (param0.nextInt(2) == 0 || param2 == 0);
    }
}
