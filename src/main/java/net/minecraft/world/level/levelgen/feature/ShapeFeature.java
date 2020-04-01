package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.ShapeConfiguration;

public class ShapeFeature extends Feature<ShapeConfiguration> {
    public ShapeFeature(Function<Dynamic<?>, ? extends ShapeConfiguration> param0, Function<Random, ? extends ShapeConfiguration> param1) {
        super(param0, param1);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, ShapeConfiguration param4
    ) {
        float var0 = Mth.lerp(param2.nextFloat(), param4.radiusMin, param4.radiusMax);
        int var1 = Mth.ceil(var0);
        BlockPos.betweenClosedStream(param3.offset(-var1, -var1, -var1), param3.offset(var1, var1, var1))
            .filter(param3x -> param4.metric.distance(param3x, param3) < var0)
            .forEach(param3x -> this.setBlock(param0, param3x, param4.material.getState(param2, param3x)));
        return true;
    }
}
