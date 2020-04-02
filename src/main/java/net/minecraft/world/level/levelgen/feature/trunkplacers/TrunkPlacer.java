package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public abstract class TrunkPlacer {
    private final int baseHeight;
    private final int heightRandA;
    private final int heightRandB;
    protected final TrunkPlacerType<?> type;

    public TrunkPlacer(int param0, int param1, int param2, TrunkPlacerType<?> param3) {
        this.baseHeight = param0;
        this.heightRandA = param1;
        this.heightRandB = param2;
        this.type = param3;
    }

    public abstract Map<BlockPos, Integer> placeTrunk(
        LevelSimulatedRW var1, Random var2, int var3, BlockPos var4, int var5, Set<BlockPos> var6, BoundingBox var7, SmallTreeConfiguration var8
    );

    public int getBaseHeight() {
        return this.baseHeight;
    }

    public int getTreeHeight(Random param0, SmallTreeConfiguration param1) {
        return this.baseHeight + param0.nextInt(this.heightRandA + 1) + param0.nextInt(this.heightRandB + 1);
    }

    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("type"), param0.createString(Registry.TRUNK_PLACER_TYPES.getKey(this.type).toString()))
            .put(param0.createString("base_height"), param0.createInt(this.baseHeight))
            .put(param0.createString("height_rand_a"), param0.createInt(this.heightRandA))
            .put(param0.createString("height_rand_b"), param0.createInt(this.heightRandB));
        return new Dynamic<>(param0, param0.createMap(var0.build())).getValue();
    }
}
