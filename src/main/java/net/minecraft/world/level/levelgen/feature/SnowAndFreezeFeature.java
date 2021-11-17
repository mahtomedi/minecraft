package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SnowAndFreezeFeature extends Feature<NoneFeatureConfiguration> {
    public SnowAndFreezeFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();

        for(int var4 = 0; var4 < 16; ++var4) {
            for(int var5 = 0; var5 < 16; ++var5) {
                int var6 = var1.getX() + var4;
                int var7 = var1.getZ() + var5;
                int var8 = var0.getHeight(Heightmap.Types.MOTION_BLOCKING, var6, var7);
                var2.set(var6, var8, var7);
                var3.set(var2).move(Direction.DOWN, 1);
                Biome var9 = var0.getBiome(var2);
                if (var9.shouldFreeze(var0, var3, false)) {
                    var0.setBlock(var3, Blocks.ICE.defaultBlockState(), 2);
                }

                if (var9.shouldSnow(var0, var2)) {
                    var0.setBlock(var2, Blocks.SNOW.defaultBlockState(), 2);
                    BlockState var10 = var0.getBlockState(var3);
                    if (var10.hasProperty(SnowyDirtBlock.SNOWY)) {
                        var0.setBlock(var3, var10.setValue(SnowyDirtBlock.SNOWY, Boolean.valueOf(true)), 2);
                    }
                }
            }
        }

        return true;
    }
}
