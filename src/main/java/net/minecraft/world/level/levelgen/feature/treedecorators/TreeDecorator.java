package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public abstract class TreeDecorator {
    public static final Codec<TreeDecorator> CODEC = Registry.TREE_DECORATOR_TYPES.byNameCodec().dispatch(TreeDecorator::type, TreeDecoratorType::codec);

    protected abstract TreeDecoratorType<?> type();

    public abstract void place(LevelSimulatedReader var1, BiConsumer<BlockPos, BlockState> var2, Random var3, List<BlockPos> var4, List<BlockPos> var5);

    protected static void placeVine(BiConsumer<BlockPos, BlockState> param0, BlockPos param1, BooleanProperty param2) {
        param0.accept(param1, Blocks.VINE.defaultBlockState().setValue(param2, Boolean.valueOf(true)));
    }
}
