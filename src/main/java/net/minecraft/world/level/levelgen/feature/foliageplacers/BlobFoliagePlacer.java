package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class BlobFoliagePlacer extends FoliagePlacer {
    public static final Codec<BlobFoliagePlacer> CODEC = RecordCodecBuilder.create(param0 -> blobParts(param0).apply(param0, BlobFoliagePlacer::new));
    protected final int height;

    protected static <P extends BlobFoliagePlacer> P3<Mu<P>, UniformInt, UniformInt, Integer> blobParts(Instance<P> param0) {
        return foliagePlacerParts(param0).and(Codec.intRange(0, 16).fieldOf("height").forGetter(param0x -> param0x.height));
    }

    public BlobFoliagePlacer(UniformInt param0, UniformInt param1, int param2) {
        super(param0, param1);
        this.height = param2;
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
        int param8,
        BoundingBox param9
    ) {
        for(int var0 = param8; var0 >= param8 - param5; --var0) {
            int var1 = Math.max(param6 + param4.radiusOffset() - 1 - var0 / 2, 0);
            this.placeLeavesRow(param0, param1, param2, param4.foliagePos(), var1, param7, var0, param4.doubleTrunk(), param9);
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
