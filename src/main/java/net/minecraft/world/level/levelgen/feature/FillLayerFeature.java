package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;

public class FillLayerFeature extends Feature<LayerConfiguration> {
    public FillLayerFeature(Codec<LayerConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<LayerConfiguration> param0) {
        BlockPos var0 = param0.origin();
        LayerConfiguration var1 = param0.config();
        WorldGenLevel var2 = param0.level();
        BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();

        for(int var4 = 0; var4 < 16; ++var4) {
            for(int var5 = 0; var5 < 16; ++var5) {
                int var6 = var0.getX() + var4;
                int var7 = var0.getZ() + var5;
                int var8 = var2.getMinBuildHeight() + var1.height;
                var3.set(var6, var8, var7);
                if (var2.getBlockState(var3).isAir()) {
                    var2.setBlock(var3, var1.state, 2);
                }
            }
        }

        return true;
    }
}
