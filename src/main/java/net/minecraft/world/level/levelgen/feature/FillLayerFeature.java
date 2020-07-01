package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;

public class FillLayerFeature extends Feature<LayerConfiguration> {
    public FillLayerFeature(Codec<LayerConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, LayerConfiguration param4) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(int var1 = 0; var1 < 16; ++var1) {
            for(int var2 = 0; var2 < 16; ++var2) {
                int var3 = param3.getX() + var1;
                int var4 = param3.getZ() + var2;
                int var5 = param4.height;
                var0.set(var3, var5, var4);
                if (param0.getBlockState(var0).isAir()) {
                    param0.setBlock(var0, param4.state, 2);
                }
            }
        }

        return true;
    }
}
