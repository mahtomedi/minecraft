package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public abstract class RootPlacer {
    public static final Codec<RootPlacer> CODEC = Registry.ROOT_PLACER_TYPES.byNameCodec().dispatch(RootPlacer::type, RootPlacerType::codec);
    protected final IntProvider trunkOffsetY;
    protected final BlockStateProvider rootProvider;
    protected final Optional<AboveRootPlacement> aboveRootPlacement;

    protected static <P extends RootPlacer> P3<Mu<P>, IntProvider, BlockStateProvider, Optional<AboveRootPlacement>> rootPlacerParts(Instance<P> param0) {
        return param0.group(
            IntProvider.CODEC.fieldOf("trunk_offset_y").forGetter(param0x -> param0x.trunkOffsetY),
            BlockStateProvider.CODEC.fieldOf("root_provider").forGetter(param0x -> param0x.rootProvider),
            AboveRootPlacement.CODEC.optionalFieldOf("above_root_placement").forGetter(param0x -> param0x.aboveRootPlacement)
        );
    }

    public RootPlacer(IntProvider param0, BlockStateProvider param1, Optional<AboveRootPlacement> param2) {
        this.trunkOffsetY = param0;
        this.rootProvider = param1;
        this.aboveRootPlacement = param2;
    }

    protected abstract RootPlacerType<?> type();

    public abstract boolean placeRoots(
        LevelSimulatedReader var1, BiConsumer<BlockPos, BlockState> var2, RandomSource var3, BlockPos var4, BlockPos var5, TreeConfiguration var6
    );

    protected void placeRoot(
        LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, RandomSource param2, BlockPos param3, TreeConfiguration param4
    ) {
        param1.accept(param3, this.getPotentiallyWaterloggedState(param0, param3, this.rootProvider.getState(param2, param3)));
        if (this.aboveRootPlacement.isPresent()) {
            AboveRootPlacement var0 = this.aboveRootPlacement.get();
            BlockPos var1 = param3.above();
            if (param2.nextFloat() < var0.aboveRootPlacementChance() && param0.isStateAtPosition(var1, BlockBehaviour.BlockStateBase::isAir)) {
                param1.accept(var1, this.getPotentiallyWaterloggedState(param0, var1, var0.aboveRootProvider().getState(param2, var1)));
            }
        }

    }

    protected BlockState getPotentiallyWaterloggedState(LevelSimulatedReader param0, BlockPos param1, BlockState param2) {
        if (param2.hasProperty(BlockStateProperties.WATERLOGGED)) {
            boolean var0 = param0.isFluidAtPosition(param1, param0x -> param0x.is(FluidTags.WATER));
            return param2.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(var0));
        } else {
            return param2;
        }
    }

    public BlockPos getTrunkOrigin(BlockPos param0, RandomSource param1) {
        return param0.above(this.trunkOffsetY.sample(param1));
    }
}
