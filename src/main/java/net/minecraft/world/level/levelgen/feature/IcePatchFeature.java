package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class IcePatchFeature extends Feature<FeatureRadius> {
    private final Block block = Blocks.PACKED_ICE;

    public IcePatchFeature(Function<Dynamic<?>, ? extends FeatureRadius> param0) {
        super(param0);
    }

    public boolean place(LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, FeatureRadius param4) {
        while(param0.isEmptyBlock(param3) && param3.getY() > 2) {
            param3 = param3.below();
        }

        if (param0.getBlockState(param3).getBlock() != Blocks.SNOW_BLOCK) {
            return false;
        } else {
            int var0 = param2.nextInt(param4.radius) + 2;
            int var1 = 1;

            for(int var2 = param3.getX() - var0; var2 <= param3.getX() + var0; ++var2) {
                for(int var3 = param3.getZ() - var0; var3 <= param3.getZ() + var0; ++var3) {
                    int var4 = var2 - param3.getX();
                    int var5 = var3 - param3.getZ();
                    if (var4 * var4 + var5 * var5 <= var0 * var0) {
                        for(int var6 = param3.getY() - 1; var6 <= param3.getY() + 1; ++var6) {
                            BlockPos var7 = new BlockPos(var2, var6, var3);
                            Block var8 = param0.getBlockState(var7).getBlock();
                            if (Block.equalsDirt(var8) || var8 == Blocks.SNOW_BLOCK || var8 == Blocks.ICE) {
                                param0.setBlock(var7, this.block.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }

            return true;
        }
    }
}
