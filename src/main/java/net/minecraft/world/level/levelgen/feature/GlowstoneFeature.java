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

public class GlowstoneFeature extends Feature<NoneFeatureConfiguration> {
    public GlowstoneFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4) {
        if (!param0.isEmptyBlock(param3)) {
            return false;
        } else {
            BlockState var0 = param0.getBlockState(param3.above());
            if (!var0.is(Blocks.NETHERRACK) && !var0.is(Blocks.BASALT) && !var0.is(Blocks.BLACKSTONE)) {
                return false;
            } else {
                param0.setBlock(param3, Blocks.GLOWSTONE.defaultBlockState(), 2);

                for(int var1 = 0; var1 < 1500; ++var1) {
                    BlockPos var2 = param3.offset(param2.nextInt(8) - param2.nextInt(8), -param2.nextInt(12), param2.nextInt(8) - param2.nextInt(8));
                    if (param0.getBlockState(var2).isAir()) {
                        int var3 = 0;

                        for(Direction var4 : Direction.values()) {
                            if (param0.getBlockState(var2.relative(var4)).is(Blocks.GLOWSTONE)) {
                                ++var3;
                            }

                            if (var3 > 1) {
                                break;
                            }
                        }

                        if (var3 == 1) {
                            param0.setBlock(var2, Blocks.GLOWSTONE.defaultBlockState(), 2);
                        }
                    }
                }

                return true;
            }
        }
    }
}
