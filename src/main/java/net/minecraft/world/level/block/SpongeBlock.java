package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;

public class SpongeBlock extends Block {
    public static final int MAX_DEPTH = 6;
    public static final int MAX_COUNT = 64;
    private static final Direction[] ALL_DIRECTIONS = Direction.values();

    protected SpongeBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param3.is(param0.getBlock())) {
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
        return BlockPos.breadthFirstTraversal(param1, 6, 65, (param0x, param1x) -> {
            for(Direction var0 : ALL_DIRECTIONS) {
                param1x.accept(param0x.relative(var0));
            }

        }, param2 -> {
            if (param2.equals(param1)) {
                return true;
            } else {
                BlockState var0 = param0.getBlockState(param2);
                FluidState var1x = param0.getFluidState(param2);
                Material var2x = var0.getMaterial();
                if (!var1x.is(FluidTags.WATER)) {
                    return false;
                } else {
                    Block var3 = var0.getBlock();
                    if (var3 instanceof BucketPickup var4 && !var4.pickupBlock(param0, param2, var0).isEmpty()) {
                        return true;
                    }

                    if (var0.getBlock() instanceof LiquidBlock) {
                        param0.setBlock(param2, Blocks.AIR.defaultBlockState(), 3);
                    } else {
                        if (var2x != Material.WATER_PLANT && var2x != Material.REPLACEABLE_WATER_PLANT) {
                            return false;
                        }

                        BlockEntity var5 = var0.hasBlockEntity() ? param0.getBlockEntity(param2) : null;
                        dropResources(var0, param0, param2, var5);
                        param0.setBlock(param2, Blocks.AIR.defaultBlockState(), 3);
                    }

                    return true;
                }
            }
        }) > 1;
    }
}
