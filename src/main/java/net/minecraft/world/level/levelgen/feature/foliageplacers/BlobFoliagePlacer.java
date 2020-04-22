package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class BlobFoliagePlacer extends FoliagePlacer {
    protected final int height;

    protected BlobFoliagePlacer(int param0, int param1, int param2, int param3, int param4, FoliagePlacerType<?> param5) {
        super(param0, param1, param2, param3, param5);
        this.height = param4;
    }

    public BlobFoliagePlacer(int param0, int param1, int param2, int param3, int param4) {
        this(param0, param1, param2, param3, param4, FoliagePlacerType.BLOB_FOLIAGE_PLACER);
    }

    public <T> BlobFoliagePlacer(Dynamic<T> param0) {
        this(
            param0.get("radius").asInt(0),
            param0.get("radius_random").asInt(0),
            param0.get("offset").asInt(0),
            param0.get("offset_random").asInt(0),
            param0.get("height").asInt(0)
        );
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

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("height"), param0.createInt(this.height));
        return param0.merge(super.serialize(param0), param0.createMap(var0.build()));
    }
}
