package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;

public class FillLayerFeature extends Feature<LayerConfiguration> {
    public FillLayerFeature(Codec<LayerConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, LayerConfiguration param5) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(int var1 = 0; var1 < 16; ++var1) {
            for(int var2 = 0; var2 < 16; ++var2) {
                int var3 = param4.getX() + var1;
                int var4 = param4.getZ() + var2;
                int var5 = param5.height;
                var0.set(var3, var5, var4);
                if (param0.getBlockState(var0).isAir()) {
                    param0.setBlock(var0, param5.state, 2);
                }
            }
        }

        return true;
    }
}
