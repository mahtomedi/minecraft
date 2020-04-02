package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EndIslandFeature extends Feature<NoneFeatureConfiguration> {
    public EndIslandFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0,
        StructureFeatureManager param1,
        ChunkGenerator<? extends ChunkGeneratorSettings> param2,
        Random param3,
        BlockPos param4,
        NoneFeatureConfiguration param5
    ) {
        float var0 = (float)(param3.nextInt(3) + 4);

        for(int var1 = 0; var0 > 0.5F; --var1) {
            for(int var2 = Mth.floor(-var0); var2 <= Mth.ceil(var0); ++var2) {
                for(int var3 = Mth.floor(-var0); var3 <= Mth.ceil(var0); ++var3) {
                    if ((float)(var2 * var2 + var3 * var3) <= (var0 + 1.0F) * (var0 + 1.0F)) {
                        this.setBlock(param0, param4.offset(var2, var1, var3), Blocks.END_STONE.defaultBlockState());
                    }
                }
            }

            var0 = (float)((double)var0 - ((double)param3.nextInt(2) + 0.5));
        }

        return true;
    }
}
