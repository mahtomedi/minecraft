package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GlowstoneFeature extends Feature<NoneFeatureConfiguration> {
    public GlowstoneFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0,
        StructureFeatureManager param1,
        ChunkGenerator<? extends ChunkGeneratorSettings> param2,
        Random param3,
        BlockPos param4,
        NoneFeatureConfiguration param5
    ) {
        if (!param0.isEmptyBlock(param4)) {
            return false;
        } else {
            Block var0 = param0.getBlockState(param4.above()).getBlock();
            if (var0 != Blocks.NETHERRACK && var0 != Blocks.BASALT && var0 != Blocks.BLACKSTONE) {
                return false;
            } else {
                param0.setBlock(param4, Blocks.GLOWSTONE.defaultBlockState(), 2);

                for(int var1 = 0; var1 < 1500; ++var1) {
                    BlockPos var2 = param4.offset(param3.nextInt(8) - param3.nextInt(8), -param3.nextInt(12), param3.nextInt(8) - param3.nextInt(8));
                    if (param0.getBlockState(var2).isAir()) {
                        int var3 = 0;

                        for(Direction var4 : Direction.values()) {
                            if (param0.getBlockState(var2.relative(var4)).getBlock() == Blocks.GLOWSTONE) {
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
