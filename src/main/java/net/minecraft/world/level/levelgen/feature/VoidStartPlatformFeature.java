package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VoidStartPlatformFeature extends Feature<NoneFeatureConfiguration> {
    private static final BlockPos PLATFORM_OFFSET = new BlockPos(8, 3, 8);
    private static final ChunkPos PLATFORM_ORIGIN_CHUNK = new ChunkPos(PLATFORM_OFFSET);
    private static final int PLATFORM_RADIUS = 16;
    private static final int PLATFORM_RADIUS_CHUNKS = 1;

    public VoidStartPlatformFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    private static int checkerboardDistance(int param0, int param1, int param2, int param3) {
        return Math.max(Math.abs(param0 - param2), Math.abs(param1 - param3));
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        ChunkPos var1 = new ChunkPos(param0.origin());
        if (checkerboardDistance(var1.x, var1.z, PLATFORM_ORIGIN_CHUNK.x, PLATFORM_ORIGIN_CHUNK.z) > 1) {
            return true;
        } else {
            BlockPos var2 = param0.origin().offset(PLATFORM_OFFSET);
            BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();

            for(int var4 = var1.getMinBlockZ(); var4 <= var1.getMaxBlockZ(); ++var4) {
                for(int var5 = var1.getMinBlockX(); var5 <= var1.getMaxBlockX(); ++var5) {
                    if (checkerboardDistance(var2.getX(), var2.getZ(), var5, var4) <= 16) {
                        var3.set(var5, var2.getY(), var4);
                        if (var3.equals(var2)) {
                            var0.setBlock(var3, Blocks.COBBLESTONE.defaultBlockState(), 2);
                        } else {
                            var0.setBlock(var3, Blocks.STONE.defaultBlockState(), 2);
                        }
                    }
                }
            }

            return true;
        }
    }
}
