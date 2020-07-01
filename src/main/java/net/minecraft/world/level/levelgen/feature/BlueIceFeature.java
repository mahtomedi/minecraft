package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Material;

public class BlueIceFeature extends Feature<NoneFeatureConfiguration> {
    public BlueIceFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4) {
        if (param3.getY() > param0.getSeaLevel() - 1) {
            return false;
        } else if (!param0.getBlockState(param3).is(Blocks.WATER) && !param0.getBlockState(param3.below()).is(Blocks.WATER)) {
            return false;
        } else {
            boolean var0 = false;

            for(Direction var1 : Direction.values()) {
                if (var1 != Direction.DOWN && param0.getBlockState(param3.relative(var1)).is(Blocks.PACKED_ICE)) {
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
                        if (var6.getMaterial() == Material.AIR || var6.is(Blocks.WATER) || var6.is(Blocks.PACKED_ICE) || var6.is(Blocks.ICE)) {
                            for(Direction var7 : Direction.values()) {
                                BlockState var8 = param0.getBlockState(var5.relative(var7));
                                if (var8.is(Blocks.BLUE_ICE)) {
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
