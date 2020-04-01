package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class AcaciaFoliagePlacer extends FoliagePlacer {
    public AcaciaFoliagePlacer(int param0, int param1) {
        super(param0, param1, FoliagePlacerType.ACACIA_FOLIAGE_PLACER);
    }

    public <T> AcaciaFoliagePlacer(Dynamic<T> param0) {
        this(param0.get("radius").asInt(0), param0.get("radius_random").asInt(0));
    }

    @Override
    public void createFoliage(
        LevelSimulatedRW param0, Random param1, SmallTreeConfiguration param2, int param3, int param4, int param5, BlockPos param6, Set<BlockPos> param7
    ) {
        param2.foliagePlacer.placeLeavesRow(param0, param1, param2, param3, param6, 0, param5, param7);
        param2.foliagePlacer.placeLeavesRow(param0, param1, param2, param3, param6, 1, 1, param7);
        BlockPos var0 = param6.above();

        for(int var1 = -1; var1 <= 1; ++var1) {
            for(int var2 = -1; var2 <= 1; ++var2) {
                this.placeLeaf(param0, param1, var0.offset(var1, 0, var2), param2, param7);
            }
        }

        for(int var3 = 2; var3 <= param5 - 1; ++var3) {
            this.placeLeaf(param0, param1, var0.east(var3), param2, param7);
            this.placeLeaf(param0, param1, var0.west(var3), param2, param7);
            this.placeLeaf(param0, param1, var0.south(var3), param2, param7);
            this.placeLeaf(param0, param1, var0.north(var3), param2, param7);
        }

    }

    @Override
    public int foliageRadius(Random param0, int param1, int param2, SmallTreeConfiguration param3) {
        return this.radius + param0.nextInt(this.radiusRandom + 1);
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, int param5) {
        return Math.abs(param2) == param5 && Math.abs(param4) == param5 && param5 > 0;
    }

    @Override
    public int getTreeRadiusForHeight(int param0, int param1, int param2, int param3) {
        return param3 == 0 ? 0 : 2;
    }

    public static AcaciaFoliagePlacer random(Random param0) {
        return new AcaciaFoliagePlacer(param0.nextInt(10) + 1, param0.nextInt(5));
    }
}
