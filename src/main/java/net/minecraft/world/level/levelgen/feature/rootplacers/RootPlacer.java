package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public abstract class RootPlacer {
    public static final Codec<RootPlacer> CODEC = Registry.ROOT_PLACER_TYPES.byNameCodec().dispatch(RootPlacer::type, RootPlacerType::codec);
    protected final BlockStateProvider rootProvider;

    protected static <P extends RootPlacer> P1<Mu<P>, BlockStateProvider> rootPlacerParts(Instance<P> param0) {
        return param0.group(BlockStateProvider.CODEC.fieldOf("root_provider").forGetter(param0x -> param0x.rootProvider));
    }

    public RootPlacer(BlockStateProvider param0) {
        this.rootProvider = param0;
    }

    protected abstract RootPlacerType<?> type();

    public abstract Optional<BlockPos> placeRoots(
        LevelSimulatedReader var1, BiConsumer<BlockPos, BlockState> var2, RandomSource var3, BlockPos var4, TreeConfiguration var5
    );

    protected void placeRoot(
        LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, RandomSource param2, BlockPos param3, TreeConfiguration param4
    ) {
        param1.accept(param3, this.getPotentiallyWaterloggedState(param0, param3, this.rootProvider.getState(param2, param3)));
    }

    protected BlockState getPotentiallyWaterloggedState(LevelSimulatedReader param0, BlockPos param1, BlockState param2) {
        if (param2.hasProperty(BlockStateProperties.WATERLOGGED)) {
            boolean var0 = param0.isFluidAtPosition(param1, param0x -> param0x.is(FluidTags.WATER));
            return param2.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(var0));
        } else {
            return param2;
        }
    }
}
