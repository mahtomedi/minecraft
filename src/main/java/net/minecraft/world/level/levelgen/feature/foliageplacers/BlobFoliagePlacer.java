package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class BlobFoliagePlacer extends FoliagePlacer {
    private final int height;

    public BlobFoliagePlacer(int param0, int param1, int param2, int param3, int param4) {
        super(param0, param1, param2, param3, FoliagePlacerType.BLOB_FOLIAGE_PLACER);
        this.height = param4;
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
    public void createFoliage(
        LevelSimulatedRW param0, Random param1, SmallTreeConfiguration param2, int param3, BlockPos param4, int param5, int param6, Set<BlockPos> param7
    ) {
        int var0 = this.offset + param1.nextInt(this.offsetRandom + 1);

        for(int var1 = param5 + var0; var1 >= var0; --var1) {
            int var2 = Math.max(param6 - 1 - (var1 - param5) / 2, 0);
            this.placeLeavesRow(param0, param1, param2, param4, param5, var1, var2, param7);
        }

    }

    @Override
    public int foliageRadius(Random param0, int param1, SmallTreeConfiguration param2) {
        return this.radius + param0.nextInt(this.radiusRandom + 1);
    }

    @Override
    public int foliageHeight(Random param0, int param1) {
        return this.height;
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, int param5) {
        return Math.abs(param2) == param5 && Math.abs(param4) == param5 && (param0.nextInt(2) == 0 || param3 == param1);
    }

    @Override
    public int getTreeRadiusForHeight(int param0, int param1, int param2) {
        return param2 == 0 ? 0 : 1;
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("height"), param0.createInt(this.height));
        return param0.merge(super.serialize(param0), param0.createMap(var0.build()));
    }
}
