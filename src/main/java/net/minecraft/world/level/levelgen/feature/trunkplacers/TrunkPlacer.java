package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
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

    public abstract List<FoliagePlacer.FoliageAttachment> placeTrunk(
        LevelSimulatedRW var1, Random var2, int var3, BlockPos var4, Set<BlockPos> var5, BoundingBox var6, TreeConfiguration var7
    );

    public int getTreeHeight(Random param0) {
        return this.baseHeight + param0.nextInt(this.heightRandA + 1) + param0.nextInt(this.heightRandB + 1);
    }

    protected static void setBlock(LevelWriter param0, BlockPos param1, BlockState param2, BoundingBox param3) {
        TreeFeature.setBlockKnownShape(param0, param1, param2);
        param3.expand(new BoundingBox(param1, param1));
    }

    private static boolean isDirt(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> {
            Block var0x = param0x.getBlock();
            return Feature.isDirt(var0x) && !param0x.is(Blocks.GRASS_BLOCK) && !param0x.is(Blocks.MYCELIUM);
        });
    }

    protected static void setDirtAt(LevelSimulatedRW param0, BlockPos param1) {
        if (!isDirt(param0, param1)) {
            TreeFeature.setBlockKnownShape(param0, param1, Blocks.DIRT.defaultBlockState());
        }

    }

    protected static boolean placeLog(
        LevelSimulatedRW param0, Random param1, BlockPos param2, Set<BlockPos> param3, BoundingBox param4, TreeConfiguration param5
    ) {
        if (TreeFeature.validTreePos(param0, param2)) {
            setBlock(param0, param2, param5.trunkProvider.getState(param1, param2), param4);
            param3.add(param2.immutable());
            return true;
        } else {
            return false;
        }
    }

    protected static void placeLogIfFree(
        LevelSimulatedRW param0, Random param1, BlockPos.MutableBlockPos param2, Set<BlockPos> param3, BoundingBox param4, TreeConfiguration param5
    ) {
        if (TreeFeature.isFree(param0, param2)) {
            placeLog(param0, param1, param2, param3, param4, param5);
        }

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
