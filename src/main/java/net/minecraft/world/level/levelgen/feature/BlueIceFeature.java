package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.material.Material;

public class BlueIceFeature extends Feature<NoneFeatureConfiguration> {
    public BlueIceFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        if (param3.getY() > param0.getSeaLevel() - 1) {
            return false;
        } else if (param0.getBlockState(param3).getBlock() != Blocks.WATER && param0.getBlockState(param3.below()).getBlock() != Blocks.WATER) {
            return false;
        } else {
            boolean var0 = false;

            for(Direction var1 : Direction.values()) {
                if (var1 != Direction.DOWN && param0.getBlockState(param3.relative(var1)).getBlock() == Blocks.PACKED_ICE) {
                    var0 = true;
                    break;
                }
            }

            if (!var0) {
                return false;
            } else {
                param0.setBlock(param3, Blocks.BLUE_ICE.defaultBlockState(), 2);

                for(int var2 = 0; var2 < 200; ++var2) {
                    int var3 = param2.nextInt(5) - param2.nextInt(6);
                    int var4 = 3;
                    if (var3 < 2) {
                        var4 += var3 / 2;
                    }

                    if (var4 >= 1) {
                        BlockPos var5 = param3.offset(param2.nextInt(var4) - param2.nextInt(var4), var3, param2.nextInt(var4) - param2.nextInt(var4));
                        BlockState var6 = param0.getBlockState(var5);
                        Block var7 = var6.getBlock();
                        if (var6.getMaterial() == Material.AIR || var7 == Blocks.WATER || var7 == Blocks.PACKED_ICE || var7 == Blocks.ICE) {
                            for(Direction var8 : Direction.values()) {
                                Block var9 = param0.getBlockState(var5.relative(var8)).getBlock();
                                if (var9 == Blocks.BLUE_ICE) {
                                    param0.setBlock(var5, Blocks.BLUE_ICE.defaultBlockState(), 2);
                                    break;
                                }
                            }
                        }
                    }
                }

                return true;
            }
        }
    }
}
