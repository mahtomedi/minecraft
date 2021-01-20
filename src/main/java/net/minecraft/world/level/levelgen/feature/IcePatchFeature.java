package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class IcePatchFeature extends BaseDiskFeature {
    public IcePatchFeature(Codec<DiskConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, DiskConfiguration param4) {
        while(param0.isEmptyBlock(param3) && param3.getY() > param0.getMinBuildHeight() + 2) {
            param3 = param3.below();
        }

        return !param0.getBlockState(param3).is(Blocks.SNOW_BLOCK) ? false : super.place(param0, param1, param2, param3, param4);
    }
}
