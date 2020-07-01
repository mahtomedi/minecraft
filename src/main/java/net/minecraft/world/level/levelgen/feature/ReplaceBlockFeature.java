package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;

public class ReplaceBlockFeature extends Feature<ReplaceBlockConfiguration> {
    public ReplaceBlockFeature(Codec<ReplaceBlockConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, ReplaceBlockConfiguration param4) {
        if (param0.getBlockState(param3).is(param4.target.getBlock())) {
            param0.setBlock(param3, param4.state, 2);
        }

        return true;
    }
}
