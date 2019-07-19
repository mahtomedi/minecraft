package net.minecraft.world.inventory;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ContainerLevelAccess {
    ContainerLevelAccess NULL = new ContainerLevelAccess() {
        @Override
        public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> param0) {
            return Optional.empty();
        }
    };

    static ContainerLevelAccess create(final Level param0, final BlockPos param1) {
        return new ContainerLevelAccess() {
            @Override
            public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> param0x) {
                return Optional.of(param0.apply(param0, param1));
            }
        };
    }

    <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> var1);

    default <T> T evaluate(BiFunction<Level, BlockPos, T> param0, T param1) {
        return this.evaluate(param0).orElse(param1);
    }

    default void execute(BiConsumer<Level, BlockPos> param0) {
        this.evaluate((param1, param2) -> {
            param0.accept(param1, param2);
            return Optional.empty();
        });
    }
}
