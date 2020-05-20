package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
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
    public static final Codec<TrunkPlacer> CODEC = Registry.TRUNK_PLACER_TYPES.dispatch(TrunkPlacer::type, TrunkPlacerType::codec);
    protected final int baseHeight;
    protected final int heightRandA;
    protected final int heightRandB;

    protected static <P extends TrunkPlacer> P3<Mu<P>, Integer, Integer, Integer> trunkPlacerParts(Instance<P> param0) {
        return param0.group(
            Codec.INT.fieldOf("base_height").forGetter(param0x -> param0x.baseHeight),
            Codec.INT.fieldOf("height_rand_a").forGetter(param0x -> param0x.heightRandA),
            Codec.INT.fieldOf("height_rand_b").forGetter(param0x -> param0x.heightRandB)
        );
    }

    public TrunkPlacer(int param0, int param1, int param2) {
        this.baseHeight = param0;
        this.heightRandA = param1;
        this.heightRandB = param2;
    }

    protected abstract TrunkPlacerType<?> type();

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
}
