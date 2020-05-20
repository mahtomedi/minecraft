package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;

public class ReplaceBlockFeature extends Feature<ReplaceBlockConfiguration> {
    public ReplaceBlockFeature(Codec<ReplaceBlockConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, ReplaceBlockConfiguration param5
    ) {
        if (param0.getBlockState(param4).is(param5.target.getBlock())) {
            param0.setBlock(param4, param5.state, 2);
        }

        return true;
    }
}
