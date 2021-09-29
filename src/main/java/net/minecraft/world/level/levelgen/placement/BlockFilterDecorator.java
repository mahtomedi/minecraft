package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;

public class BlockFilterDecorator extends FilterDecorator<BlockFilterConfiguration> {
    public BlockFilterDecorator(Codec<BlockFilterConfiguration> param0) {
        super(param0);
    }

    protected boolean shouldPlace(DecorationContext param0, Random param1, BlockFilterConfiguration param2, BlockPos param3) {
        return param2.predicate().test(param0.getLevel(), param3);
    }
}
