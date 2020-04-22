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

public class DarkOakFoliagePlacer extends FoliagePlacer {
    public DarkOakFoliagePlacer(int param0, int param1, int param2, int param3) {
        super(param0, param1, param2, param3, FoliagePlacerType.DARK_OAK_FOLIAGE_PLACER);
    }

    public <T> DarkOakFoliagePlacer(Dynamic<T> param0) {
        this(param0.get("radius").asInt(0), param0.get("radius_random").asInt(0), param0.get("offset").asInt(0), param0.get("offset_random").asInt(0));
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
        BlockPos var0 = param4.foliagePos().above(param8);
        boolean var1 = param4.doubleTrunk();
        if (var1) {
            this.placeLeavesRow(param0, param1, param2, var0, param6 + 2, param7, -1, var1);
            this.placeLeavesRow(param0, param1, param2, var0, param6 + 3, param7, 0, var1);
            this.placeLeavesRow(param0, param1, param2, var0, param6 + 2, param7, 1, var1);
            if (param1.nextBoolean()) {
                this.placeLeavesRow(param0, param1, param2, var0, param6, param7, 2, var1);
            }
        } else {
            this.placeLeavesRow(param0, param1, param2, var0, param6 + 2, param7, -1, var1);
            this.placeLeavesRow(param0, param1, param2, var0, param6 + 1, param7, 0, var1);
        }

    }

    @Override
    public int foliageHeight(Random param0, int param1, TreeConfiguration param2) {
        return 4;
    }

    @Override
    protected boolean shouldSkipLocationSigned(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
        return param2 != 0 || !param5 || param1 != -param4 && param1 < param4 || param3 != -param4 && param3 < param4
            ? super.shouldSkipLocationSigned(param0, param1, param2, param3, param4, param5)
            : true;
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
        if (param2 == -1 && !param5) {
            return param1 == param4 && param3 == param4;
        } else if (param2 == 1) {
            return param1 + param3 > param4 * 2 - 2;
        } else {
            return false;
        }
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        return param0.merge(super.serialize(param0), param0.createMap(var0.build()));
    }
}
