package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class IcePatchFeature extends BaseDiskFeature {
    public IcePatchFeature(Codec<DiskConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<DiskConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        ChunkGenerator var1 = param0.chunkGenerator();
        RandomSource var2 = param0.random();
        DiskConfiguration var3 = param0.config();
        BlockPos var4 = param0.origin();

        while(var0.isEmptyBlock(var4) && var4.getY() > var0.getMinBuildHeight() + 2) {
            var4 = var4.below();
        }

        return !param0.level().getBlockState(var4).is(var3.canOriginReplace())
            ? false
            : super.place(new FeaturePlaceContext<>(param0.topFeature(), var0, param0.chunkGenerator(), param0.random(), var4, param0.config()));
    }
}
