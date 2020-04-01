package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class PineFoliagePlacer extends FoliagePlacer {
    public PineFoliagePlacer(int param0, int param1) {
        super(param0, param1, FoliagePlacerType.PINE_FOLIAGE_PLACER);
    }

    public <T> PineFoliagePlacer(Dynamic<T> param0) {
        this(param0.get("radius").asInt(0), param0.get("radius_random").asInt(0));
    }

    @Override
    public void createFoliage(
        LevelSimulatedRW param0, Random param1, SmallTreeConfiguration param2, int param3, int param4, int param5, BlockPos param6, Set<BlockPos> param7
    ) {
        int var0 = 0;

        for(int var1 = param3; var1 >= param4; --var1) {
            this.placeLeavesRow(param0, param1, param2, param3, param6, var1, var0, param7);
            if (var0 >= 1 && var1 == param4 + 1) {
                --var0;
            } else if (var0 < param5) {
                ++var0;
            }
        }

    }

    @Override
    public int foliageRadius(Random param0, int param1, int param2, SmallTreeConfiguration param3) {
        return this.radius + param0.nextInt(this.radiusRandom + 1) + param0.nextInt(Math.max(1, param2 - param1 + 1));
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, int param5) {
        return Math.abs(param2) == param5 && Math.abs(param4) == param5 && param5 > 0;
    }

    @Override
    public int getTreeRadiusForHeight(int param0, int param1, int param2, int param3) {
        return param3 <= 1 ? 0 : 2;
    }

    public static PineFoliagePlacer random(Random param0) {
        return new PineFoliagePlacer(param0.nextInt(10) + 1, param0.nextInt(5) + 1);
    }
}
