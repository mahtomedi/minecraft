package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class IceSpikeFeature extends Feature<NoneFeatureConfiguration> {
    public IceSpikeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, NoneFeatureConfiguration param5
    ) {
        while(param0.isEmptyBlock(param4) && param4.getY() > 2) {
            param4 = param4.below();
        }

        if (!param0.getBlockState(param4).is(Blocks.SNOW_BLOCK)) {
            return false;
        } else {
            param4 = param4.above(param3.nextInt(4));
            int var0 = param3.nextInt(4) + 7;
            int var1 = var0 / 4 + param3.nextInt(2);
            if (var1 > 1 && param3.nextInt(60) == 0) {
                param4 = param4.above(10 + param3.nextInt(30));
            }

            for(int var2 = 0; var2 < var0; ++var2) {
                float var3 = (1.0F - (float)var2 / (float)var0) * (float)var1;
                int var4 = Mth.ceil(var3);

                for(int var5 = -var4; var5 <= var4; ++var5) {
                    float var6 = (float)Mth.abs(var5) - 0.25F;

                    for(int var7 = -var4; var7 <= var4; ++var7) {
                        float var8 = (float)Mth.abs(var7) - 0.25F;
                        if ((var5 == 0 && var7 == 0 || !(var6 * var6 + var8 * var8 > var3 * var3))
                            && (var5 != -var4 && var5 != var4 && var7 != -var4 && var7 != var4 || !(param3.nextFloat() > 0.75F))) {
                            BlockState var9 = param0.getBlockState(param4.offset(var5, var2, var7));
                            Block var10 = var9.getBlock();
                            if (var9.isAir() || isDirt(var10) || var10 == Blocks.SNOW_BLOCK || var10 == Blocks.ICE) {
                                this.setBlock(param0, param4.offset(var5, var2, var7), Blocks.PACKED_ICE.defaultBlockState());
                            }

                            if (var2 != 0 && var4 > 1) {
                                var9 = param0.getBlockState(param4.offset(var5, -var2, var7));
                                var10 = var9.getBlock();
                                if (var9.isAir() || isDirt(var10) || var10 == Blocks.SNOW_BLOCK || var10 == Blocks.ICE) {
                                    this.setBlock(param0, param4.offset(var5, -var2, var7), Blocks.PACKED_ICE.defaultBlockState());
                                }
                            }
                        }
                    }
                }
            }

            int var11 = var1 - 1;
            if (var11 < 0) {
                var11 = 0;
            } else if (var11 > 1) {
                var11 = 1;
            }

            for(int var12 = -var11; var12 <= var11; ++var12) {
                for(int var13 = -var11; var13 <= var11; ++var13) {
                    BlockPos var14 = param4.offset(var12, -1, var13);
                    int var15 = 50;
                    if (Math.abs(var12) == 1 && Math.abs(var13) == 1) {
                        var15 = param3.nextInt(5);
                    }

                    while(var14.getY() > 50) {
                        BlockState var16 = param0.getBlockState(var14);
                        Block var17 = var16.getBlock();
                        if (!var16.isAir() && !isDirt(var17) && var17 != Blocks.SNOW_BLOCK && var17 != Blocks.ICE && var17 != Blocks.PACKED_ICE) {
                            break;
                        }

                        this.setBlock(param0, var14, Blocks.PACKED_ICE.defaultBlockState());
                        var14 = var14.below();
                        if (--var15 <= 0) {
                            var14 = var14.below(param3.nextInt(5) + 1);
                            var15 = param3.nextInt(5);
                        }
                    }
                }
            }

            return true;
        }
    }
}
