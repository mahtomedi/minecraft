package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public abstract class AbstractFlowerFeature<U extends FeatureConfiguration> extends Feature<U> {
    public AbstractFlowerFeature(Function<Dynamic<?>, ? extends U> param0) {
        super(param0);
    }

    @Override
    public boolean place(LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, U param4) {
        BlockState var0 = this.getRandomFlower(param2, param3, param4);
        int var1 = 0;

        for(int var2 = 0; var2 < this.getCount(param4); ++var2) {
            BlockPos var3 = this.getPos(param2, param3, param4);
            if (param0.isEmptyBlock(var3) && var3.getY() < 255 && var0.canSurvive(param0, var3) && this.isValid(param0, var3, param4)) {
                param0.setBlock(var3, var0, 2);
                ++var1;
            }
        }

        return var1 > 0;
    }

    public abstract boolean isValid(LevelAccessor var1, BlockPos var2, U var3);

    public abstract int getCount(U var1);

    public abstract BlockPos getPos(Random var1, BlockPos var2, U var3);

    public abstract BlockState getRandomFlower(Random var1, BlockPos var2, U var3);
}
