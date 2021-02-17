package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BubbleColumnBlock extends Block implements BucketPickup {
    public static final BooleanProperty DRAG_DOWN = BlockStateProperties.DRAG;

    public BubbleColumnBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(DRAG_DOWN, Boolean.valueOf(true)));
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        BlockState var0 = param1.getBlockState(param2.above());
        if (var0.isAir()) {
            param3.onAboveBubbleCol(param0.getValue(DRAG_DOWN));
            if (!param1.isClientSide) {
                ServerLevel var1 = (ServerLevel)param1;

                for(int var2 = 0; var2 < 2; ++var2) {
                    var1.sendParticles(
                        ParticleTypes.SPLASH,
                        (double)param2.getX() + param1.random.nextDouble(),
                        (double)(param2.getY() + 1),
                        (double)param2.getZ() + param1.random.nextDouble(),
                        1,
                        0.0,
                        0.0,
                        0.0,
                        1.0
                    );
                    var1.sendParticles(
                        ParticleTypes.BUBBLE,
                        (double)param2.getX() + param1.random.nextDouble(),
                        (double)(param2.getY() + 1),
                        (double)param2.getZ() + param1.random.nextDouble(),
                        1,
                        0.0,
                        0.01,
                        0.0,
                        0.2
                    );
                }
            }
        } else {
            param3.onInsideBubbleColumn(param0.getValue(DRAG_DOWN));
        }

    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        growColumn(param1, param2.above(), getDrag(param1, param2));
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return Fluids.WATER.getSource(false);
    }

    public static void growColumn(LevelAccessor param0, BlockPos param1, boolean param2) {
        BlockPos.MutableBlockPos var0 = param1.mutable();

        while(canExistIn(param0, var0)) {
            param0.setBlock(var0, Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, Boolean.valueOf(param2)), 2);
            var0.move(Direction.UP);
        }

    }

    public static boolean canExistIn(LevelAccessor param0, BlockPos param1) {
        FluidState var0 = param0.getFluidState(param1);
        return param0.getBlockState(param1).is(Blocks.WATER) && var0.getAmount() >= 8 && var0.isSource();
    }

    private static boolean getDrag(BlockGetter param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        if (var0.is(Blocks.BUBBLE_COLUMN)) {
            return var0.getValue(DRAG_DOWN);
        } else {
            return !var0.is(Blocks.SOUL_SAND);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        double var0 = (double)param2.getX();
        double var1 = (double)param2.getY();
        double var2 = (double)param2.getZ();
        if (param0.getValue(DRAG_DOWN)) {
            param1.addAlwaysVisibleParticle(ParticleTypes.CURRENT_DOWN, var0 + 0.5, var1 + 0.8, var2, 0.0, 0.0, 0.0);
            if (param3.nextInt(200) == 0) {
                param1.playLocalSound(
                    var0,
                    var1,
                    var2,
                    SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT,
                    SoundSource.BLOCKS,
                    0.2F + param3.nextFloat() * 0.2F,
                    0.9F + param3.nextFloat() * 0.15F,
                    false
                );
            }
        } else {
            param1.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, var0 + 0.5, var1, var2 + 0.5, 0.0, 0.04, 0.0);
            param1.addAlwaysVisibleParticle(
                ParticleTypes.BUBBLE_COLUMN_UP,
                var0 + (double)param3.nextFloat(),
                var1 + (double)param3.nextFloat(),
                var2 + (double)param3.nextFloat(),
                0.0,
                0.04,
                0.0
            );
            if (param3.nextInt(200) == 0) {
                param1.playLocalSound(
                    var0,
                    var1,
                    var2,
                    SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT,
                    SoundSource.BLOCKS,
                    0.2F + param3.nextFloat() * 0.2F,
                    0.9F + param3.nextFloat() * 0.15F,
                    false
                );
            }
        }

    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (!param0.canSurvive(param3, param4)) {
            return Blocks.WATER.defaultBlockState();
        } else {
            if (param1 == Direction.DOWN) {
                param3.setBlock(param4, Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, Boolean.valueOf(getDrag(param3, param5))), 2);
            } else if (param1 == Direction.UP && !param2.is(Blocks.BUBBLE_COLUMN) && canExistIn(param3, param5)) {
                param3.getBlockTicks().scheduleTick(param4, this, 5);
            }

            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
            return super.updateShape(param0, param1, param2, param3, param4, param5);
        }
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockState var0 = param1.getBlockState(param2.below());
        return var0.is(Blocks.BUBBLE_COLUMN) || var0.is(Blocks.MAGMA_BLOCK) || var0.is(Blocks.SOUL_SAND);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(DRAG_DOWN);
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor param0, BlockPos param1, BlockState param2) {
        param0.setBlock(param1, Blocks.AIR.defaultBlockState(), 11);
        return new ItemStack(Items.WATER_BUCKET);
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Fluids.WATER.getPickupSound();
    }
}
