package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class LakeLavaPlacementDecorator extends FeatureDecorator<LakeChanceDecoratorConfig> {
    public LakeLavaPlacementDecorator(Function<Dynamic<?>, ? extends LakeChanceDecoratorConfig> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, LakeChanceDecoratorConfig param3, BlockPos param4
    ) {
        if (param2.nextInt(param3.chance / 10) == 0) {
            int var0 = param2.nextInt(16);
            int var1 = param2.nextInt(param2.nextInt(param1.getGenDepth() - 8) + 8);
            int var2 = param2.nextInt(16);
            if (var1 < param0.getSeaLevel() || param2.nextInt(param3.chance / 8) == 0) {
                return Stream.of(param4.offset(var0, var1, var2));
            }
        }

        return Stream.empty();
    }
}
