package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class BlobFoliagePlacer extends FoliagePlacer {
    public BlobFoliagePlacer(int param0, int param1) {
        super(param0, param1, FoliagePlacerType.BLOB_FOLIAGE_PLACER);
    }

    public <T> BlobFoliagePlacer(Dynamic<T> param0) {
        this(param0.get("radius").asInt(0), param0.get("radius_random").asInt(0));
    }

    @Override
    public void createFoliage(
        LevelSimulatedRW param0, Random param1, SmallTreeConfiguration param2, int param3, int param4, int param5, BlockPos param6, Set<BlockPos> param7
    ) {
        for(int var0 = param3; var0 >= param4; --var0) {
            int var1 = Math.max(param5 - 1 - (var0 - param3) / 2, 0);
            this.placeLeavesRow(param0, param1, param2, param3, param6, var0, var1, param7);
        }

    }

    @Override
    public int foliageRadius(Random param0, int param1, int param2, SmallTreeConfiguration param3) {
        return this.radius + param0.nextInt(this.radiusRandom + 1);
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, int param5) {
        return Math.abs(param2) == param5 && Math.abs(param4) == param5 && (param0.nextInt(2) == 0 || param3 == param1);
    }

    @Override
    public int getTreeRadiusForHeight(int param0, int param1, int param2, int param3) {
        return param3 == 0 ? 0 : 1;
    }

    public static BlobFoliagePlacer random(Random param0) {
        return new BlobFoliagePlacer(param0.nextInt(10) + 1, param0.nextInt(5));
    }
}
