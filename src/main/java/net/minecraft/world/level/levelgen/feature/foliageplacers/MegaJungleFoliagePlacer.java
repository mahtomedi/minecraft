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

public class MegaJungleFoliagePlacer extends FoliagePlacer {
    protected final int height;

    public MegaJungleFoliagePlacer(int param0, int param1, int param2, int param3, int param4) {
        super(param0, param1, param2, param3, FoliagePlacerType.MEGA_JUNGLE_FOLIAGE_PLACER);
        this.height = param4;
    }

    public <T> MegaJungleFoliagePlacer(Dynamic<T> param0) {
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
        int var0 = param4.doubleTrunk() ? param5 : 1 + param1.nextInt(2);

        for(int var1 = param8; var1 >= param8 - var0; --var1) {
            int var2 = param6 + param4.radiusOffset() + 1 - var1;
            this.placeLeavesRow(param0, param1, param2, param4.foliagePos(), var2, param7, var1, param4.doubleTrunk());
        }

    }

    @Override
    public int foliageHeight(Random param0, int param1, TreeConfiguration param2) {
        return this.height;
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
        if (param1 + param3 >= 7) {
            return true;
        } else {
            return param1 * param1 + param3 * param3 > param4 * param4;
        }
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("height"), param0.createInt(this.height));
        return param0.merge(super.serialize(param0), param0.createMap(var0.build()));
    }
}
