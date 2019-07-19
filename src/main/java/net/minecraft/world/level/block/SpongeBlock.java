package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.Queue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;

public class SpongeBlock extends Block {
    protected SpongeBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (param3.getBlock() != param0.getBlock()) {
            this.tryAbsorbWater(param1, param2);
        }
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        this.tryAbsorbWater(param1, param2);
        super.neighborChanged(param0, param1, param2, param3, param4, param5);
    }

    protected void tryAbsorbWater(Level param0, BlockPos param1) {
        if (this.removeWaterBreadthFirstSearch(param0, param1)) {
            param0.setBlock(param1, Blocks.WET_SPONGE.defaultBlockState(), 2);
            param0.levelEvent(2001, param1, Block.getId(Blocks.WATER.defaultBlockState()));
        }

    }

    private boolean removeWaterBreadthFirstSearch(Level param0, BlockPos param1) {
        Queue<Tuple<BlockPos, Integer>> var0 = Lists.newLinkedList();
        var0.add(new Tuple<>(param1, 0));
        int var1 = 0;

        while(!var0.isEmpty()) {
            Tuple<BlockPos, Integer> var2 = var0.poll();
            BlockPos var3 = var2.getA();
            int var4 = var2.getB();

            for(Direction var5 : Direction.values()) {
                BlockPos var6 = var3.relative(var5);
                BlockState var7 = param0.getBlockState(var6);
                FluidState var8 = param0.getFluidState(var6);
                Material var9 = var7.getMaterial();
                if (var8.is(FluidTags.WATER)) {
                    if (var7.getBlock() instanceof BucketPickup && ((BucketPickup)var7.getBlock()).takeLiquid(param0, var6, var7) != Fluids.EMPTY) {
                        ++var1;
                        if (var4 < 6) {
                            var0.add(new Tuple<>(var6, var4 + 1));
                        }
                    } else if (var7.getBlock() instanceof LiquidBlock) {
                        param0.setBlock(var6, Blocks.AIR.defaultBlockState(), 3);
                        ++var1;
                        if (var4 < 6) {
                            var0.add(new Tuple<>(var6, var4 + 1));
                        }
                    } else if (var9 == Material.WATER_PLANT || var9 == Material.REPLACEABLE_WATER_PLANT) {
                        BlockEntity var10 = var7.getBlock().isEntityBlock() ? param0.getBlockEntity(var6) : null;
                        dropResources(var7, param0, var6, var10);
                        param0.setBlock(var6, Blocks.AIR.defaultBlockState(), 3);
                        ++var1;
                        if (var4 < 6) {
                            var0.add(new Tuple<>(var6, var4 + 1));
                        }
                    }
                }
            }

            if (var1 > 64) {
                break;
            }
        }

        return var1 > 0;
    }
}
