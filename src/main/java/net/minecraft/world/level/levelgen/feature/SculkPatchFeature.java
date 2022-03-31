package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkBehaviour;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.SculkPatchConfiguration;

public class SculkPatchFeature extends Feature<SculkPatchConfiguration> {
    public SculkPatchFeature(Codec<SculkPatchConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<SculkPatchConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        if (!this.canSpreadFrom(var0, var1)) {
            return false;
        } else {
            SculkPatchConfiguration var2 = param0.config();
            Random var3 = param0.random();
            SculkSpreader var4 = SculkSpreader.createWorldGenSpreader();
            int var5 = var2.spreadRounds() + var2.growthRounds();

            for(int var6 = 0; var6 < var5; ++var6) {
                for(int var7 = 0; var7 < var2.chargeCount(); ++var7) {
                    var4.addCursors(var1, var2.amountPerCharge());
                }

                boolean var8 = var6 < var2.spreadRounds();

                for(int var9 = 0; var9 < var2.spreadAttempts(); ++var9) {
                    var4.updateCursors(var0, var1, var3, var8);
                }

                var4.clear();
            }

            BlockPos var10 = var1.below();
            if (var3.nextFloat() <= var2.catalystChance() && var0.getBlockState(var10).isCollisionShapeFullBlock(var0, var10)) {
                var0.setBlock(var1, Blocks.SCULK_CATALYST.defaultBlockState(), 3);
            }

            int var11 = var2.extraRareGrowths().sample(var3);

            for(int var12 = 0; var12 < var11; ++var12) {
                BlockPos var13 = var1.offset(var3.nextInt(5) - 2, 0, var3.nextInt(5) - 2);
                if (var0.getBlockState(var13).isAir() && var0.getBlockState(var13.below()).isFaceSturdy(var0, var13.below(), Direction.UP)) {
                    var0.setBlock(var13, Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON, Boolean.valueOf(true)), 3);
                }
            }

            return true;
        }
    }

    private boolean canSpreadFrom(LevelAccessor param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        if (var0.getBlock() instanceof SculkBehaviour) {
            return true;
        } else {
            return !var0.isAir() && (!var0.is(Blocks.WATER) || !var0.getFluidState().isSource())
                ? false
                : Direction.stream().map(param1::relative).anyMatch(param1x -> param0.getBlockState(param1x).isCollisionShapeFullBlock(param0, param1x));
        }
    }
}
