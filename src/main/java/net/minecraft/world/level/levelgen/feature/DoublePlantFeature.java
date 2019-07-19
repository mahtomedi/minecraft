package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class DoublePlantFeature extends Feature<DoublePlantConfiguration> {
    public DoublePlantFeature(Function<Dynamic<?>, ? extends DoublePlantConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, DoublePlantConfiguration param4
    ) {
        boolean var0 = false;

        for(int var1 = 0; var1 < 64; ++var1) {
            BlockPos var2 = param3.offset(param2.nextInt(8) - param2.nextInt(8), param2.nextInt(4) - param2.nextInt(4), param2.nextInt(8) - param2.nextInt(8));
            if (param0.isEmptyBlock(var2) && var2.getY() < 254 && param4.state.canSurvive(param0, var2)) {
                ((DoublePlantBlock)param4.state.getBlock()).placeAt(param0, var2, 2);
                var0 = true;
            }
        }

        return var0;
    }
}
