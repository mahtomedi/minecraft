package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class SimpleRandomSelectorFeature extends Feature<SimpleRandomFeatureConfig> {
    public SimpleRandomSelectorFeature(Function<Dynamic<?>, ? extends SimpleRandomFeatureConfig> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, SimpleRandomFeatureConfig param4
    ) {
        int var0 = param2.nextInt(param4.features.size());
        ConfiguredFeature<?> var1 = param4.features.get(var0);
        return var1.place(param0, param1, param2, param3);
    }
}
