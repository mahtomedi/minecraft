package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CampfireBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 7.0, 16.0);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty SIGNAL_FIRE = BlockStateProperties.SIGNAL_FIRE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape VIRTUAL_FENCE_POST = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
    private static final int SMOKE_DISTANCE = 5;
    private final boolean spawnParticles;
    private final int fireDamage;

    public CampfireBlock(boolean param0, int param1, BlockBehaviour.Properties param2) {
        super(param2);
        this.spawnParticles = param0;
        this.fireDamage = param1;
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(LIT, Boolean.valueOf(true))
                .setValue(SIGNAL_FIRE, Boolean.valueOf(false))
                .setValue(WATERLOGGED, Boolean.valueOf(false))
                .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (var0 instanceof CampfireBlockEntity) {
            CampfireBlockEntity var1 = (CampfireBlockEntity)var0;
            ItemStack var2 = param3.getItemInHand(param4);
            Optional<CampfireCookingRecipe> var3 = var1.getCookableRecipe(var2);
            if (var3.isPresent()) {
                if (!param1.isClientSide && var1.placeFood(param3.getAbilities().instabuild ? var2.copy() : var2, var3.get().getCookingTime())) {
                    param3.awardStat(Stats.INTERACT_WITH_CAMPFIRE);
                    return InteractionResult.SUCCESS;
                }

                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (!param3.fireImmune() && param0.getValue(LIT) && param3 instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)param3)) {
            param3.hurt(DamageSource.IN_FIRE, (float)this.fireDamage);
        }

        super.entityInside(param0, param1, param2, param3);
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param0.is(param3.getBlock())) {
            BlockEntity var0 = param1.getBlockEntity(param2);
            if (var0 instanceof CampfireBlockEntity) {
                Containers.dropContents(param1, param2, ((CampfireBlockEntity)var0).getItems());
            }

            super.onRemove(param0, param1, param2, param3, param4);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        LevelAccessor var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        boolean var2 = var0.getFluidState(var1).getType() == Fluids.WATER;
        return this.defaultBlockState()
            .setValue(WATERLOGGED, Boolean.valueOf(var2))
            .setValue(SIGNAL_FIRE, Boolean.valueOf(this.isSmokeSource(var0.getBlockState(var1.below()))))
            .setValue(LIT, Boolean.valueOf(!var2))
            .setValue(FACING, param0.getHorizontalDirection());
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return param1 == Direction.DOWN
            ? param0.setValue(SIGNAL_FIRE, Boolean.valueOf(this.isSmokeSource(param2)))
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    private boolean isSmokeSource(BlockState param0) {
        return param0.is(Blocks.HAY_BLOCK);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (param0.getValue(LIT)) {
            if (param3.nextInt(10) == 0) {
                param1.playLocalSound(
                    (double)param2.getX() + 0.5,
                    (double)param2.getY() + 0.5,
                    (double)param2.getZ() + 0.5,
                    SoundEvents.CAMPFIRE_CRACKLE,
                    SoundSource.BLOCKS,
                    0.5F + param3.nextFloat(),
                    param3.nextFloat() * 0.7F + 0.6F,
                    false
                );
            }

            if (this.spawnParticles && param3.nextInt(5) == 0) {
                for(int var0 = 0; var0 < param3.nextInt(1) + 1; ++var0) {
                    param1.addParticle(
                        ParticleTypes.LAVA,
                        (double)param2.getX() + 0.5,
                        (double)param2.getY() + 0.5,
                        (double)param2.getZ() + 0.5,
                        (double)(param3.nextFloat() / 2.0F),
                        5.0E-5,
                        (double)(param3.nextFloat() / 2.0F)
                    );
                }
            }

        }
    }

    public static void dowse(@Nullable Entity param0, LevelAccessor param1, BlockPos param2, BlockState param3) {
        if (param1.isClientSide()) {
            for(int var0 = 0; var0 < 20; ++var0) {
                makeParticles((Level)param1, param2, param3.getValue(SIGNAL_FIRE), true);
            }
        }

        BlockEntity var1 = param1.getBlockEntity(param2);
        if (var1 instanceof CampfireBlockEntity) {
            ((CampfireBlockEntity)var1).dowse();
        }

        param1.gameEvent(param0, GameEvent.BLOCK_CHANGE, param2);
    }

    @Override
    public boolean placeLiquid(LevelAccessor param0, BlockPos param1, BlockState param2, FluidState param3) {
        if (!param2.getValue(BlockStateProperties.WATERLOGGED) && param3.getType() == Fluids.WATER) {
            boolean var0 = param2.getValue(LIT);
            if (var0) {
                if (!param0.isClientSide()) {
                    param0.playSound(null, param1, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                dowse(null, param0, param1, param2);
            }

            param0.setBlock(param1, param2.setValue(WATERLOGGED, Boolean.valueOf(true)).setValue(LIT, Boolean.valueOf(false)), 3);
            param0.getLiquidTicks().scheduleTick(param1, param3.getType(), param3.getType().getTickDelay(param0));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
        if (!param0.isClientSide && param3.isOnFire()) {
            Entity var0 = param3.getOwner();
            boolean var1 = var0 == null || var0 instanceof Player || param0.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            if (var1 && !param1.getValue(LIT) && !param1.getValue(WATERLOGGED)) {
                BlockPos var2 = param2.getBlockPos();
                param0.setBlock(var2, param1.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
            }
        }

    }

    public static void makeParticles(Level param0, BlockPos param1, boolean param2, boolean param3) {
        Random var0 = param0.getRandom();
        SimpleParticleType var1 = param2 ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;
        param0.addAlwaysVisibleParticle(
            var1,
            true,
            (double)param1.getX() + 0.5 + var0.nextDouble() / 3.0 * (double)(var0.nextBoolean() ? 1 : -1),
            (double)param1.getY() + var0.nextDouble() + var0.nextDouble(),
            (double)param1.getZ() + 0.5 + var0.nextDouble() / 3.0 * (double)(var0.nextBoolean() ? 1 : -1),
            0.0,
            0.07,
            0.0
        );
        if (param3) {
            param0.addParticle(
                ParticleTypes.SMOKE,
                (double)param1.getX() + 0.5 + var0.nextDouble() / 4.0 * (double)(var0.nextBoolean() ? 1 : -1),
                (double)param1.getY() + 0.4,
                (double)param1.getZ() + 0.5 + var0.nextDouble() / 4.0 * (double)(var0.nextBoolean() ? 1 : -1),
                0.0,
                0.005,
                0.0
            );
        }

    }

    public static boolean isSmokeyPos(Level param0, BlockPos param1) {
        for(int var0 = 1; var0 <= 5; ++var0) {
            BlockPos var1 = param1.below(var0);
            BlockState var2 = param0.getBlockState(var1);
            if (isLitCampfire(var2)) {
                return true;
            }

            boolean var3 = Shapes.joinIsNotEmpty(VIRTUAL_FENCE_POST, var2.getCollisionShape(param0, param1, CollisionContext.empty()), BooleanOp.AND);
            if (var3) {
                BlockState var4 = param0.getBlockState(var1.below());
                return isLitCampfire(var4);
            }
        }

        return false;
    }

    public static boolean isLitCampfire(BlockState param0) {
        return param0.hasProperty(LIT) && param0.is(BlockTags.CAMPFIRES) && param0.getValue(LIT);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(FACING, param1.rotate(param0.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.rotate(param1.getRotation(param0.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(LIT, SIGNAL_FIRE, WATERLOGGED, FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new CampfireBlockEntity(param0, param1);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level param0, BlockState param1, BlockEntityType<T> param2) {
        if (param0.isClientSide) {
            return param1.getValue(LIT) ? createTickerHelper(param2, BlockEntityType.CAMPFIRE, CampfireBlockEntity::particleTick) : null;
        } else {
            return param1.getValue(LIT)
                ? createTickerHelper(param2, BlockEntityType.CAMPFIRE, CampfireBlockEntity::cookTick)
                : createTickerHelper(param2, BlockEntityType.CAMPFIRE, CampfireBlockEntity::cooldownTick);
        }
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }

    public static boolean canLight(BlockState param0) {
        return param0.is(BlockTags.CAMPFIRES, param0x -> param0x.hasProperty(WATERLOGGED) && param0x.hasProperty(LIT))
            && !param0.getValue(WATERLOGGED)
            && !param0.getValue(LIT);
    }
}
