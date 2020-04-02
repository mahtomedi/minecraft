package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
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
        } else if (param0.getBlockState(param4.above()).getBlock() != Blocks.NETHERRACK) {
            return false;
        } else {
            param0.setBlock(param4, Blocks.GLOWSTONE.defaultBlockState(), 2);

            for(int var0 = 0; var0 < 1500; ++var0) {
                BlockPos var1 = param4.offset(param3.nextInt(8) - param3.nextInt(8), -param3.nextInt(12), param3.nextInt(8) - param3.nextInt(8));
                if (param0.getBlockState(var1).isAir()) {
                    int var2 = 0;

                    for(Direction var3 : Direction.values()) {
                        if (param0.getBlockState(var1.relative(var3)).getBlock() == Blocks.GLOWSTONE) {
                            ++var2;
                        }

                        if (var2 > 1) {
                            break;
                        }
                    }

                    if (var2 == 1) {
                        param0.setBlock(var1, Blocks.GLOWSTONE.defaultBlockState(), 2);
                    }
                }
            }

            return true;
        }
    }
}
