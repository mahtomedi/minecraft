package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class SpongeBlock extends Block {
    public static final MapCodec<SpongeBlock> CODEC = simpleCodec(SpongeBlock::new);
    public static final int MAX_DEPTH = 6;
    public static final int MAX_COUNT = 64;
    private static final Direction[] ALL_DIRECTIONS = Direction.values();

    @Override
    public MapCodec<SpongeBlock> codec() {
        return CODEC;
    }

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
            param0.playSound(null, param1, SoundEvents.SPONGE_ABSORB, SoundSource.BLOCKS, 1.0F, 1.0F);
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
                if (!var1x.is(FluidTags.WATER)) {
                    return false;
                } else {
                    Block var2x = var0.getBlock();
                    if (var2x instanceof BucketPickup var3 && !var3.pickupBlock(null, param0, param2, var0).isEmpty()) {
                        return true;
                    }

                    if (var0.getBlock() instanceof LiquidBlock) {
                        param0.setBlock(param2, Blocks.AIR.defaultBlockState(), 3);
                    } else {
                        if (!var0.is(Blocks.KELP) && !var0.is(Blocks.KELP_PLANT) && !var0.is(Blocks.SEAGRASS) && !var0.is(Blocks.TALL_SEAGRASS)) {
                            return false;
                        }

                        BlockEntity var4 = var0.hasBlockEntity() ? param0.getBlockEntity(param2) : null;
                        dropResources(var0, param0, param2, var4);
                        param0.setBlock(param2, Blocks.AIR.defaultBlockState(), 3);
                    }

                    return true;
                }
            }
        }) > 1;
    }
}
