package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;

public class VegetationPatchFeature extends Feature<VegetationPatchConfiguration> {
    public VegetationPatchFeature(Codec<VegetationPatchConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<VegetationPatchConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        VegetationPatchConfiguration var1 = param0.config();
        Random var2 = param0.random();
        BlockPos var3 = param0.origin();
        Predicate<BlockState> var4 = param1 -> param1.is(var1.replaceable);
        int var5 = var1.xzRadius.sample(var2) + 1;
        int var6 = var1.xzRadius.sample(var2) + 1;
        Set<BlockPos> var7 = this.placeGroundPatch(var0, var1, var2, var3, var4, var5, var6);
        this.distributeVegetation(param0, var0, var1, var2, var7, var5, var6);
        return !var7.isEmpty();
    }

    protected Set<BlockPos> placeGroundPatch(
        WorldGenLevel param0, VegetationPatchConfiguration param1, Random param2, BlockPos param3, Predicate<BlockState> param4, int param5, int param6
    ) {
        BlockPos.MutableBlockPos var0 = param3.mutable();
        BlockPos.MutableBlockPos var1 = var0.mutable();
        Direction var2 = param1.surface.getDirection();
        Direction var3 = var2.getOpposite();
        Set<BlockPos> var4 = new HashSet<>();

        for(int var5 = -param5; var5 <= param5; ++var5) {
            boolean var6 = var5 == -param5 || var5 == param5;

            for(int var7 = -param6; var7 <= param6; ++var7) {
                boolean var8 = var7 == -param6 || var7 == param6;
                boolean var9 = var6 || var8;
                boolean var10 = var6 && var8;
                boolean var11 = var9 && !var10;
                if (!var10 && (!var11 || param1.extraEdgeColumnChance != 0.0F && !(param2.nextFloat() > param1.extraEdgeColumnChance))) {
                    var0.setWithOffset(param3, var5, 0, var7);

                    for(int var12 = 0; param0.isStateAtPosition(var0, BlockBehaviour.BlockStateBase::isAir) && var12 < param1.verticalRange; ++var12) {
                        var0.move(var2);
                    }

                    for(int var25 = 0; param0.isStateAtPosition(var0, param0x -> !param0x.isAir()) && var25 < param1.verticalRange; ++var25) {
                        var0.move(var3);
                    }

                    var1.setWithOffset(var0, param1.surface.getDirection());
                    BlockState var13 = param0.getBlockState(var1);
                    if (param0.isEmptyBlock(var0) && var13.isFaceSturdy(param0, var1, param1.surface.getDirection().getOpposite())) {
                        int var14 = param1.depth.sample(param2)
                            + (param1.extraBottomBlockChance > 0.0F && param2.nextFloat() < param1.extraBottomBlockChance ? 1 : 0);
                        BlockPos var15 = var1.immutable();
                        boolean var16 = this.placeGround(param0, param1, param4, param2, var1, var14);
                        if (var16) {
                            var4.add(var15);
                        }
                    }
                }
            }
        }

        return var4;
    }

    protected void distributeVegetation(
        FeaturePlaceContext<VegetationPatchConfiguration> param0,
        WorldGenLevel param1,
        VegetationPatchConfiguration param2,
        Random param3,
        Set<BlockPos> param4,
        int param5,
        int param6
    ) {
        for(BlockPos var0 : param4) {
            if (param2.vegetationChance > 0.0F && param3.nextFloat() < param2.vegetationChance) {
                this.placeVegetation(param1, param2, param0.chunkGenerator(), param3, var0);
            }
        }

    }

    protected boolean placeVegetation(WorldGenLevel param0, VegetationPatchConfiguration param1, ChunkGenerator param2, Random param3, BlockPos param4) {
        return param1.vegetationFeature.value().place(param0, param2, param3, param4.relative(param1.surface.getDirection().getOpposite()));
    }

    protected boolean placeGround(
        WorldGenLevel param0, VegetationPatchConfiguration param1, Predicate<BlockState> param2, Random param3, BlockPos.MutableBlockPos param4, int param5
    ) {
        for(int var0 = 0; var0 < param5; ++var0) {
            BlockState var1 = param1.groundState.getState(param3, param4);
            BlockState var2 = param0.getBlockState(param4);
            if (!var1.is(var2.getBlock())) {
                if (!param2.test(var2)) {
                    return var0 != 0;
                }

                param0.setBlock(param4, var1, 2);
                param4.move(param1.surface.getDirection());
            }
        }

        return true;
    }
}
