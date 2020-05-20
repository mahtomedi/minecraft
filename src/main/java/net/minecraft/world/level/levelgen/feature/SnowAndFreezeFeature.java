package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SnowAndFreezeFeature extends Feature<NoneFeatureConfiguration> {
    public SnowAndFreezeFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, NoneFeatureConfiguration param5
    ) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

        for(int var2 = 0; var2 < 16; ++var2) {
            for(int var3 = 0; var3 < 16; ++var3) {
                int var4 = param4.getX() + var2;
                int var5 = param4.getZ() + var3;
                int var6 = param0.getHeight(Heightmap.Types.MOTION_BLOCKING, var4, var5);
                var0.set(var4, var6, var5);
                var1.set(var0).move(Direction.DOWN, 1);
                Biome var7 = param0.getBiome(var0);
                if (var7.shouldFreeze(param0, var1, false)) {
                    param0.setBlock(var1, Blocks.ICE.defaultBlockState(), 2);
                }

                if (var7.shouldSnow(param0, var0)) {
                    param0.setBlock(var0, Blocks.SNOW.defaultBlockState(), 2);
                    BlockState var8 = param0.getBlockState(var1);
                    if (var8.hasProperty(SnowyDirtBlock.SNOWY)) {
                        param0.setBlock(var1, var8.setValue(SnowyDirtBlock.SNOWY, Boolean.valueOf(true)), 2);
                    }
                }
            }
        }

        return true;
    }
}
