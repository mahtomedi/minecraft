package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.SingleBlockStateConfiguration;

public class BlockSurvivesFilterDecorator extends FeatureDecorator<SingleBlockStateConfiguration> {
    public BlockSurvivesFilterDecorator(Codec<SingleBlockStateConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, SingleBlockStateConfiguration param2, BlockPos param3) {
        return !param2.state().canSurvive(param0.getLevel(), param3) ? Stream.of() : Stream.of(param3);
    }
}
