package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class SpruceFoliagePlacer extends FoliagePlacer {
    public SpruceFoliagePlacer(int param0, int param1) {
        super(param0, param1, FoliagePlacerType.SPRUCE_FOLIAGE_PLACER);
    }

    public <T> SpruceFoliagePlacer(Dynamic<T> param0) {
        this(param0.get("radius").asInt(0), param0.get("radius_random").asInt(0));
    }

    @Override
    public void createFoliage(
        LevelSimulatedRW param0, Random param1, SmallTreeConfiguration param2, int param3, int param4, int param5, BlockPos param6, Set<BlockPos> param7
    ) {
        int var0 = param1.nextInt(2);
        int var1 = 1;
        int var2 = 0;

        for(int var3 = param3; var3 >= param4; --var3) {
            this.placeLeavesRow(param0, param1, param2, param3, param6, var3, var0, param7);
            if (var0 >= var1) {
                var0 = var2;
                var2 = 1;
                var1 = Math.min(var1 + 1, param5);
            } else {
                ++var0;
            }
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
        return param3 < param0 ? 0 : param2;
    }
}
