package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public abstract class TrunkPlacer {
    public static final Codec<TrunkPlacer> CODEC = Registry.TRUNK_PLACER_TYPES.byNameCodec().dispatch(TrunkPlacer::type, TrunkPlacerType::codec);
    private static final int MAX_BASE_HEIGHT = 32;
    private static final int MAX_RAND = 24;
    public static final int MAX_HEIGHT = 80;
    protected final int baseHeight;
    protected final int heightRandA;
    protected final int heightRandB;

    protected static <P extends TrunkPlacer> P3<Mu<P>, Integer, Integer, Integer> trunkPlacerParts(Instance<P> param0) {
        return param0.group(
            Codec.intRange(0, 32).fieldOf("base_height").forGetter(param0x -> param0x.baseHeight),
            Codec.intRange(0, 24).fieldOf("height_rand_a").forGetter(param0x -> param0x.heightRandA),
            Codec.intRange(0, 24).fieldOf("height_rand_b").forGetter(param0x -> param0x.heightRandB)
        );
    }

    public TrunkPlacer(int param0, int param1, int param2) {
        this.baseHeight = param0;
        this.heightRandA = param1;
        this.heightRandB = param2;
    }

    protected abstract TrunkPlacerType<?> type();

    public abstract List<FoliagePlacer.FoliageAttachment> placeTrunk(
        LevelSimulatedReader var1, BiConsumer<BlockPos, BlockState> var2, Random var3, int var4, BlockPos var5, TreeConfiguration var6
    );

    public int getTreeHeight(Random param0) {
        return this.baseHeight + param0.nextInt(this.heightRandA + 1) + param0.nextInt(this.heightRandB + 1);
    }

    private static boolean isDirt(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> Feature.isDirt(param0x) && !param0x.is(Blocks.GRASS_BLOCK) && !param0x.is(Blocks.MYCELIUM));
    }

    protected static void setDirtAt(
        LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, Random param2, BlockPos param3, TreeConfiguration param4
    ) {
        if (param4.forceDirt || !isDirt(param0, param3)) {
            param1.accept(param3, param4.dirtProvider.getState(param2, param3));
        }

    }

    protected static boolean placeLog(
        LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, Random param2, BlockPos param3, TreeConfiguration param4
    ) {
        return placeLog(param0, param1, param2, param3, param4, Function.identity());
    }

    protected static boolean placeLog(
        LevelSimulatedReader param0,
        BiConsumer<BlockPos, BlockState> param1,
        Random param2,
        BlockPos param3,
        TreeConfiguration param4,
        Function<BlockState, BlockState> param5
    ) {
        if (TreeFeature.validTreePos(param0, param3)) {
            param1.accept(param3, param5.apply(param4.trunkProvider.getState(param2, param3)));
            return true;
        } else {
            return false;
        }
    }

    protected static void placeLogIfFree(
        LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, Random param2, BlockPos.MutableBlockPos param3, TreeConfiguration param4
    ) {
        if (TreeFeature.isFree(param0, param3)) {
            placeLog(param0, param1, param2, param3, param4);
        }

    }
}
