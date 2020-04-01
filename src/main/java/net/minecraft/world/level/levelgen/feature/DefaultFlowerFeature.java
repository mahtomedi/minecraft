package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

public class DefaultFlowerFeature extends AbstractFlowerFeature<RandomPatchConfiguration> {
    public DefaultFlowerFeature(Function<Dynamic<?>, ? extends RandomPatchConfiguration> param0, Function<Random, ? extends RandomPatchConfiguration> param1) {
        super(param0, param1);
    }

    public boolean isValid(LevelAccessor param0, BlockPos param1, RandomPatchConfiguration param2) {
        return !param2.blacklist.contains(param0.getBlockState(param1));
    }

    public int getCount(RandomPatchConfiguration param0) {
        return param0.tries;
    }

    public BlockPos getPos(Random param0, BlockPos param1, RandomPatchConfiguration param2) {
        return param1.offset(
            param0.nextInt(param2.xspread) - param0.nextInt(param2.xspread),
            param0.nextInt(param2.yspread) - param0.nextInt(param2.yspread),
            param0.nextInt(param2.zspread) - param0.nextInt(param2.zspread)
        );
    }

    public BlockState getRandomFlower(Random param0, BlockPos param1, RandomPatchConfiguration param2) {
        return param2.stateProvider.getState(param0, param1);
    }
}
