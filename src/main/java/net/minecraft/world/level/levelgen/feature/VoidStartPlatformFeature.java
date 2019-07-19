package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class VoidStartPlatformFeature extends Feature<NoneFeatureConfiguration> {
    private static final BlockPos PLATFORM_ORIGIN = new BlockPos(8, 3, 8);
    private static final ChunkPos PLATFORM_ORIGIN_CHUNK = new ChunkPos(PLATFORM_ORIGIN);

    public VoidStartPlatformFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    private static int checkerboardDistance(int param0, int param1, int param2, int param3) {
        return Math.max(Math.abs(param0 - param2), Math.abs(param1 - param3));
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        ChunkPos var0 = new ChunkPos(param3);
        if (checkerboardDistance(var0.x, var0.z, PLATFORM_ORIGIN_CHUNK.x, PLATFORM_ORIGIN_CHUNK.z) > 1) {
            return true;
        } else {
            BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

            for(int var2 = var0.getMinBlockZ(); var2 <= var0.getMaxBlockZ(); ++var2) {
                for(int var3 = var0.getMinBlockX(); var3 <= var0.getMaxBlockX(); ++var3) {
                    if (checkerboardDistance(PLATFORM_ORIGIN.getX(), PLATFORM_ORIGIN.getZ(), var3, var2) <= 16) {
                        var1.set(var3, PLATFORM_ORIGIN.getY(), var2);
                        if (var1.equals(PLATFORM_ORIGIN)) {
                            param0.setBlock(var1, Blocks.COBBLESTONE.defaultBlockState(), 2);
                        } else {
                            param0.setBlock(var1, Blocks.STONE.defaultBlockState(), 2);
                        }
                    }
                }
            }

            return true;
        }
    }
}
