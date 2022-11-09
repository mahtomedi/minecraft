package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public abstract class TreeDecorator {
    public static final Codec<TreeDecorator> CODEC = BuiltInRegistries.TREE_DECORATOR_TYPE
        .byNameCodec()
        .dispatch(TreeDecorator::type, TreeDecoratorType::codec);

    protected abstract TreeDecoratorType<?> type();

    public abstract void place(TreeDecorator.Context var1);

    public static final class Context {
        private final LevelSimulatedReader level;
        private final BiConsumer<BlockPos, BlockState> decorationSetter;
        private final RandomSource random;
        private final ObjectArrayList<BlockPos> logs;
        private final ObjectArrayList<BlockPos> leaves;
        private final ObjectArrayList<BlockPos> roots;

        public Context(
            LevelSimulatedReader param0,
            BiConsumer<BlockPos, BlockState> param1,
            RandomSource param2,
            Set<BlockPos> param3,
            Set<BlockPos> param4,
            Set<BlockPos> param5
        ) {
            this.level = param0;
            this.decorationSetter = param1;
            this.random = param2;
            this.roots = new ObjectArrayList<>(param5);
            this.logs = new ObjectArrayList<>(param3);
            this.leaves = new ObjectArrayList<>(param4);
            this.logs.sort(Comparator.comparingInt(Vec3i::getY));
            this.leaves.sort(Comparator.comparingInt(Vec3i::getY));
            this.roots.sort(Comparator.comparingInt(Vec3i::getY));
        }

        public void placeVine(BlockPos param0, BooleanProperty param1) {
            this.setBlock(param0, Blocks.VINE.defaultBlockState().setValue(param1, Boolean.valueOf(true)));
        }

        public void setBlock(BlockPos param0, BlockState param1) {
            this.decorationSetter.accept(param0, param1);
        }

        public boolean isAir(BlockPos param0) {
            return this.level.isStateAtPosition(param0, BlockBehaviour.BlockStateBase::isAir);
        }

        public LevelSimulatedReader level() {
            return this.level;
        }

        public RandomSource random() {
            return this.random;
        }

        public ObjectArrayList<BlockPos> logs() {
            return this.logs;
        }

        public ObjectArrayList<BlockPos> leaves() {
            return this.leaves;
        }

        public ObjectArrayList<BlockPos> roots() {
            return this.roots;
        }
    }
}
