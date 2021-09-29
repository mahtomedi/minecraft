package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.SingleBlockStateConfiguration;

public class BlockSurvivesFilterDecorator extends FilterDecorator<SingleBlockStateConfiguration> {
    public BlockSurvivesFilterDecorator(Codec<SingleBlockStateConfiguration> param0) {
        super(param0);
    }

    protected boolean shouldPlace(DecorationContext param0, Random param1, SingleBlockStateConfiguration param2, BlockPos param3) {
        return param2.state().canSurvive(param0.getLevel(), param3);
    }
}
