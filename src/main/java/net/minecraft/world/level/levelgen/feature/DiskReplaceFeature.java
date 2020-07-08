package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskReplaceFeature extends BaseDiskFeature {
    public DiskReplaceFeature(Codec<DiskConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, DiskConfiguration param4) {
        return !param0.getFluidState(param3).is(FluidTags.WATER) ? false : super.place(param0, param1, param2, param3, param4);
    }
}
