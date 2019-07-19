package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class LakeWaterPlacementDecorator extends FeatureDecorator<LakeChanceDecoratorConfig> {
    public LakeWaterPlacementDecorator(Function<Dynamic<?>, ? extends LakeChanceDecoratorConfig> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, LakeChanceDecoratorConfig param3, BlockPos param4
    ) {
        if (param2.nextInt(param3.chance) == 0) {
            int var0 = param2.nextInt(16);
            int var1 = param2.nextInt(param1.getGenDepth());
            int var2 = param2.nextInt(16);
            return Stream.of(param4.offset(var0, var1, var2));
        } else {
            return Stream.empty();
        }
    }
}
