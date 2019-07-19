package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public class HayBlockPileFeature extends BlockPileFeature {
    public HayBlockPileFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    protected BlockState getBlockState(LevelAccessor param0) {
        Direction.Axis var0 = Direction.Axis.getRandomAxis(param0.getRandom());
        return Blocks.HAY_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, var0);
    }
}
