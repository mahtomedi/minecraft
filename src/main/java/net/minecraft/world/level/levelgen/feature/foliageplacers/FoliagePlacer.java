package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Serializable;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public abstract class FoliagePlacer implements Serializable {
    protected final int radius;
    protected final int radiusRandom;
    protected final FoliagePlacerType<?> type;

    public FoliagePlacer(int param0, int param1, FoliagePlacerType<?> param2) {
        this.radius = param0;
        this.radiusRandom = param1;
        this.type = param2;
    }

    public abstract void createFoliage(
        LevelSimulatedRW var1, Random var2, SmallTreeConfiguration var3, int var4, int var5, int var6, BlockPos var7, Set<BlockPos> var8
    );

    public abstract int foliageRadius(Random var1, int var2, int var3, SmallTreeConfiguration var4);

    protected abstract boolean shouldSkipLocation(Random var1, int var2, int var3, int var4, int var5, int var6);

    public abstract int getTreeRadiusForHeight(int var1, int var2, int var3, int var4);

    protected void placeLeavesRow(
        LevelSimulatedRW param0, Random param1, SmallTreeConfiguration param2, int param3, BlockPos param4, int param5, int param6, Set<BlockPos> param7
    ) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(int var1 = -param6; var1 <= param6; ++var1) {
            for(int var2 = -param6; var2 <= param6; ++var2) {
                if (!this.shouldSkipLocation(param1, param3, var1, param5, var2, param6)) {
                    var0.set(var1 + param4.getX(), param5 + param4.getY(), var2 + param4.getZ());
                    this.placeLeaf(param0, param1, var0, param2, param7);
                }
            }
        }

    }

    protected void placeLeaf(LevelSimulatedRW param0, Random param1, BlockPos param2, SmallTreeConfiguration param3, Set<BlockPos> param4) {
        if (AbstractTreeFeature.isAirOrLeaves(param0, param2)
            || AbstractTreeFeature.isReplaceablePlant(param0, param2)
            || AbstractTreeFeature.isBlockWater(param0, param2)) {
            param0.setBlock(param2, param3.leavesProvider.getState(param1, param2), 19);
            param4.add(param2);
        }

    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("type"), param0.createString(Registry.FOLIAGE_PLACER_TYPES.getKey(this.type).toString()))
            .put(param0.createString("radius"), param0.createInt(this.radius))
            .put(param0.createString("radius_random"), param0.createInt(this.radius));
        return new Dynamic<>(param0, param0.createMap(var0.build())).getValue();
    }
}
