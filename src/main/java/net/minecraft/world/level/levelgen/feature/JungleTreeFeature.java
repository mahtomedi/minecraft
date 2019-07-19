package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.level.block.state.BlockState;

public class JungleTreeFeature extends TreeFeature {
    public JungleTreeFeature(
        Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0, boolean param1, int param2, BlockState param3, BlockState param4, boolean param5
    ) {
        super(param0, param1, param2, param3, param4, param5);
    }

    @Override
    protected int getTreeHeight(Random param0) {
        return this.baseHeight + param0.nextInt(7);
    }
}
