package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class BushFeature extends Feature<BushConfiguration> {
    public BushFeature(Function<Dynamic<?>, ? extends BushConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, BushConfiguration param4
    ) {
        int var0 = 0;
        BlockState var1 = param4.state;

        for(int var2 = 0; var2 < 64; ++var2) {
            BlockPos var3 = param3.offset(param2.nextInt(8) - param2.nextInt(8), param2.nextInt(4) - param2.nextInt(4), param2.nextInt(8) - param2.nextInt(8));
            if (param0.isEmptyBlock(var3) && (!param0.getDimension().isHasCeiling() || var3.getY() < 255) && var1.canSurvive(param0, var3)) {
                param0.setBlock(var3, var1, 2);
                ++var0;
            }
        }

        return var0 > 0;
    }
}
