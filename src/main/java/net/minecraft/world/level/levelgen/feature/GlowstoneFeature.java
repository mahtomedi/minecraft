package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GlowstoneFeature extends Feature<NoneFeatureConfiguration> {
    public GlowstoneFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, NoneFeatureConfiguration param5
    ) {
        if (!param0.isEmptyBlock(param4)) {
            return false;
        } else {
            BlockState var0 = param0.getBlockState(param4.above());
            if (!var0.is(Blocks.NETHERRACK) && !var0.is(Blocks.BASALT) && !var0.is(Blocks.BLACKSTONE)) {
                return false;
            } else {
                param0.setBlock(param4, Blocks.GLOWSTONE.defaultBlockState(), 2);

                for(int var1 = 0; var1 < 1500; ++var1) {
                    BlockPos var2 = param4.offset(param3.nextInt(8) - param3.nextInt(8), -param3.nextInt(12), param3.nextInt(8) - param3.nextInt(8));
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
