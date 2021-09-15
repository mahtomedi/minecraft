package net.minecraft.world.level;

import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public interface WorldGenLevel extends ServerLevelAccessor {
    long getSeed();

    Stream<? extends StructureStart<?>> startsForFeature(SectionPos var1, StructureFeature<?> var2);

    default boolean ensureCanWrite(BlockPos param0) {
        return true;
    }

    default void setCurrentlyGenerating(@Nullable Supplier<String> param0) {
    }
}
