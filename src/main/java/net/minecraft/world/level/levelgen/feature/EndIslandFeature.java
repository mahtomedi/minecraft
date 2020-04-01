package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class EndIslandFeature extends Feature<BlockStateConfiguration> {
    public EndIslandFeature(Function<Dynamic<?>, ? extends BlockStateConfiguration> param0, Function<Random, ? extends BlockStateConfiguration> param1) {
        super(param0, param1);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, BlockStateConfiguration param4
    ) {
        float var0 = (float)(param2.nextInt(3) + 4);

        for(int var1 = 0; var0 > 0.5F; --var1) {
            for(int var2 = Mth.floor(-var0); var2 <= Mth.ceil(var0); ++var2) {
                for(int var3 = Mth.floor(-var0); var3 <= Mth.ceil(var0); ++var3) {
                    if ((float)(var2 * var2 + var3 * var3) <= (var0 + 1.0F) * (var0 + 1.0F)) {
                        this.setBlock(param0, param3.offset(var2, var1, var3), param4.state);
                    }
                }
            }

            var0 = (float)((double)var0 - ((double)param2.nextInt(2) + 0.5));
        }

        return true;
    }
}
