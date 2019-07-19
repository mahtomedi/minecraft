package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MelonBlockPileFeature extends BlockPileFeature {
    public MelonBlockPileFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    protected BlockState getBlockState(LevelAccessor param0) {
        return Blocks.MELON.defaultBlockState();
    }
}
