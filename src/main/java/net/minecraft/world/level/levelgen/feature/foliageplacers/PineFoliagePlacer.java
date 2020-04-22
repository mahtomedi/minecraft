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

public class PineFoliagePlacer extends FoliagePlacer {
    private final int height;
    private final int heightRandom;

    public PineFoliagePlacer(int param0, int param1, int param2, int param3, int param4, int param5) {
        super(param0, param1, param2, param3, FoliagePlacerType.PINE_FOLIAGE_PLACER);
        this.height = param4;
        this.heightRandom = param5;
    }

    public <T> PineFoliagePlacer(Dynamic<T> param0) {
        this(
            param0.get("radius").asInt(0),
            param0.get("radius_random").asInt(0),
            param0.get("offset").asInt(0),
            param0.get("offset_random").asInt(0),
            param0.get("height").asInt(0),
            param0.get("height_random").asInt(0)
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
        int var0 = 0;

        for(int var1 = param8; var1 >= param8 - param5; --var1) {
            this.placeLeavesRow(param0, param1, param2, param4.foliagePos(), var0, param7, var1, param4.doubleTrunk());
            if (var0 >= 1 && var1 == param8 - param5 + 1) {
                --var0;
            } else if (var0 < param6 + param4.radiusOffset()) {
                ++var0;
            }
        }

    }

    @Override
    public int foliageRadius(Random param0, int param1) {
        return super.foliageRadius(param0, param1) + param0.nextInt(param1 + 1);
    }

    @Override
    public int foliageHeight(Random param0, int param1, TreeConfiguration param2) {
        return this.height + param0.nextInt(this.heightRandom + 1);
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
        return param1 == param4 && param3 == param4 && param4 > 0;
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("height"), param0.createInt(this.height)).put(param0.createString("height_random"), param0.createInt(this.heightRandom));
        return param0.merge(super.serialize(param0), param0.createMap(var0.build()));
    }
}
