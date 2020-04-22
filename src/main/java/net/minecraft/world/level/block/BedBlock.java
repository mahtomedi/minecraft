package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BedBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;
    protected static final VoxelShape BASE = Block.box(0.0, 3.0, 0.0, 16.0, 9.0, 16.0);
    protected static final VoxelShape LEG_NORTH_WEST = Block.box(0.0, 0.0, 0.0, 3.0, 3.0, 3.0);
    protected static final VoxelShape LEG_SOUTH_WEST = Block.box(0.0, 0.0, 13.0, 3.0, 3.0, 16.0);
    protected static final VoxelShape LEG_NORTH_EAST = Block.box(13.0, 0.0, 0.0, 16.0, 3.0, 3.0);
    protected static final VoxelShape LEG_SOUTH_EAST = Block.box(13.0, 0.0, 13.0, 16.0, 3.0, 16.0);
    protected static final VoxelShape NORTH_SHAPE = Shapes.or(BASE, LEG_NORTH_WEST, LEG_NORTH_EAST);
    protected static final VoxelShape SOUTH_SHAPE = Shapes.or(BASE, LEG_SOUTH_WEST, LEG_SOUTH_EAST);
    protected static final VoxelShape WEST_SHAPE = Shapes.or(BASE, LEG_NORTH_WEST, LEG_SOUTH_WEST);
    protected static final VoxelShape EAST_SHAPE = Shapes.or(BASE, LEG_NORTH_EAST, LEG_SOUTH_EAST);
    private final DyeColor color;

    public BedBlock(DyeColor param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.color = param0;
        this.registerDefaultState(this.stateDefinition.any().setValue(PART, BedPart.FOOT).setValue(OCCUPIED, Boolean.valueOf(false)));
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static Direction getBedOrientation(BlockGetter param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        return var0.getBlock() instanceof BedBlock ? var0.getValue(FACING) : null;
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param1.isClientSide) {
            return InteractionResult.CONSUME;
        } else {
            if (param0.getValue(PART) != BedPart.HEAD) {
                param2 = param2.relative(param0.getValue(FACING));
                param0 = param1.getBlockState(param2);
                if (param0.getBlock() != this) {
                    return InteractionResult.CONSUME;
                }
            }

            if (!canSetSpawn(param1, param2)) {
                param1.removeBlock(param2, false);
                BlockPos var0 = param2.relative(param0.getValue(FACING).getOpposite());
                if (param1.getBlockState(var0).getBlock() == this) {
                    param1.removeBlock(var0, false);
                }

                param1.explode(
                    null,
                    DamageSource.badRespawnPointExplosion(),
                    (double)param2.getX() + 0.5,
                    (double)param2.getY() + 0.5,
                    (double)param2.getZ() + 0.5,
                    5.0F,
                    true,
                    Explosion.BlockInteraction.DESTROY
                );
                return InteractionResult.SUCCESS;
            } else if (param0.getValue(OCCUPIED)) {
                if (!this.kickVillagerOutOfBed(param1, param2)) {
                    param3.displayClientMessage(new TranslatableComponent("block.minecraft.bed.occupied"), true);
                }

                return InteractionResult.SUCCESS;
            } else {
                param3.startSleepInBed(param2).ifLeft(param1x -> {
                    if (param1x != null) {
                        param3.displayClientMessage(param1x.getMessage(), true);
                    }

                });
                return InteractionResult.SUCCESS;
            }
        }
    }

    public static boolean canSetSpawn(Level param0, BlockPos param1) {
        return param0.dimension.mayRespawn() && param0.getBiome(param1) != Biomes.NETHER_WASTES;
    }

    private boolean kickVillagerOutOfBed(Level param0, BlockPos param1) {
        List<Villager> var0 = param0.getEntitiesOfClass(Villager.class, new AABB(param1), LivingEntity::isSleeping);
        if (var0.isEmpty()) {
            return false;
        } else {
            var0.get(0).stopSleeping();
            return true;
        }
    }

    @Override
    public void fallOn(Level param0, BlockPos param1, Entity param2, float param3) {
        super.fallOn(param0, param1, param2, param3 * 0.5F);
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter param0, Entity param1) {
        if (param1.isSuppressingBounce()) {
            super.updateEntityAfterFallOn(param0, param1);
        } else {
            this.bounceUp(param1);
        }

    }

    private void bounceUp(Entity param0) {
        Vec3 var0 = param0.getDeltaMovement();
        if (var0.y < 0.0) {
            double var1 = param0 instanceof LivingEntity ? 1.0 : 0.8;
            param0.setDeltaMovement(var0.x, -var0.y * 0.66F * var1, var0.z);
        }

    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == getNeighbourDirection(param0.getValue(PART), param0.getValue(FACING))) {
            return param2.getBlock() == this && param2.getValue(PART) != param0.getValue(PART)
                ? param0.setValue(OCCUPIED, param2.getValue(OCCUPIED))
                : Blocks.AIR.defaultBlockState();
        } else {
            return super.updateShape(param0, param1, param2, param3, param4, param5);
        }
    }

    private static Direction getNeighbourDirection(BedPart param0, Direction param1) {
        return param0 == BedPart.FOOT ? param1 : param1.getOpposite();
    }

    @Override
    public void playerDestroy(Level param0, Player param1, BlockPos param2, BlockState param3, @Nullable BlockEntity param4, ItemStack param5) {
        super.playerDestroy(param0, param1, param2, Blocks.AIR.defaultBlockState(), param4, param5);
    }

    @Override
    public void playerWillDestroy(Level param0, BlockPos param1, BlockState param2, Player param3) {
        BedPart var0 = param2.getValue(PART);
        BlockPos var1 = param1.relative(getNeighbourDirection(var0, param2.getValue(FACING)));
        BlockState var2 = param0.getBlockState(var1);
        if (var2.getBlock() == this && var2.getValue(PART) != var0) {
            param0.setBlock(var1, Blocks.AIR.defaultBlockState(), 35);
            param0.levelEvent(param3, 2001, var1, Block.getId(var2));
            if (!param0.isClientSide && !param3.isCreative()) {
                ItemStack var3 = param3.getMainHandItem();
                dropResources(param2, param0, param1, null, param3, var3);
                dropResources(var2, param0, var1, null, param3, var3);
            }

            param3.awardStat(Stats.BLOCK_MINED.get(this));
        }

        super.playerWillDestroy(param0, param1, param2, param3);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        Direction var0 = param0.getHorizontalDirection();
        BlockPos var1 = param0.getClickedPos();
        BlockPos var2 = var1.relative(var0);
        return param0.getLevel().getBlockState(var2).canBeReplaced(param0) ? this.defaultBlockState().setValue(FACING, var0) : null;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        Direction var0 = getConnectedDirection(param0).getOpposite();
        switch(var0) {
            case NORTH:
                return NORTH_SHAPE;
            case SOUTH:
                return SOUTH_SHAPE;
            case WEST:
                return WEST_SHAPE;
            default:
                return EAST_SHAPE;
        }
    }

    public static Direction getConnectedDirection(BlockState param0) {
        Direction var0 = param0.getValue(FACING);
        return param0.getValue(PART) == BedPart.HEAD ? var0.getOpposite() : var0;
    }

    @OnlyIn(Dist.CLIENT)
    public static DoubleBlockCombiner.BlockType getBlockType(BlockState param0) {
        BedPart var0 = param0.getValue(PART);
        return var0 == BedPart.HEAD ? DoubleBlockCombiner.BlockType.FIRST : DoubleBlockCombiner.BlockType.SECOND;
    }

    public static Optional<Vec3> findStandUpPosition(EntityType<?> param0, LevelReader param1, BlockPos param2, int param3) {
        Direction var0 = param1.getBlockState(param2).getValue(FACING);
        int var1 = param2.getX();
        int var2 = param2.getY();
        int var3 = param2.getZ();

        for(int var4 = 0; var4 <= 1; ++var4) {
            int var5 = var1 - var0.getStepX() * var4 - 1;
            int var6 = var3 - var0.getStepZ() * var4 - 1;
            int var7 = var5 + 2;
            int var8 = var6 + 2;

            for(int var9 = var5; var9 <= var7; ++var9) {
                for(int var10 = var6; var10 <= var8; ++var10) {
                    BlockPos var11 = new BlockPos(var9, var2, var10);
                    Optional<Vec3> var12 = getStandingLocationAtOrBelow(param0, param1, var11);
                    if (var12.isPresent()) {
                        if (param3 <= 0) {
                            return var12;
                        }

                        --param3;
                    }
                }
            }
        }

        return Optional.empty();
    }

    public static Optional<Vec3> getStandingLocationAtOrBelow(EntityType<?> param0, LevelReader param1, BlockPos param2) {
        VoxelShape var0 = param1.getBlockState(param2).getCollisionShape(param1, param2);
        if (var0.max(Direction.Axis.Y) > 0.4375) {
            return Optional.empty();
        } else {
            BlockPos.MutableBlockPos var1 = param2.mutable();

            while(var1.getY() >= 0 && param2.getY() - var1.getY() <= 2 && param1.getBlockState(var1).getCollisionShape(param1, var1).isEmpty()) {
                var1.move(Direction.DOWN);
            }

            VoxelShape var2 = param1.getBlockState(var1).getCollisionShape(param1, var1);
            if (var2.isEmpty()) {
                return Optional.empty();
            } else {
                double var3 = (double)var1.getY() + var2.max(Direction.Axis.Y) + 2.0E-7;
                if ((double)param2.getY() - var3 > 2.0) {
                    return Optional.empty();
                } else {
                    float var4 = param0.getWidth() / 2.0F;
                    Vec3 var5 = new Vec3((double)var1.getX() + 0.5, var3, (double)var1.getZ() + 0.5);
                    return param1.noCollision(
                            new AABB(
                                var5.x - (double)var4,
                                var5.y,
                                var5.z - (double)var4,
                                var5.x + (double)var4,
                                var5.y + (double)param0.getHeight(),
                                var5.z + (double)var4
                            )
                        )
                        ? Optional.of(var5)
                        : Optional.empty();
                }
            }
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState param0) {
        return PushReaction.DESTROY;
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, PART, OCCUPIED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return new BedBlockEntity(this.color);
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, @Nullable LivingEntity param3, ItemStack param4) {
        super.setPlacedBy(param0, param1, param2, param3, param4);
        if (!param0.isClientSide) {
            BlockPos var0 = param1.relative(param2.getValue(FACING));
            param0.setBlock(var0, param2.setValue(PART, BedPart.HEAD), 3);
            param0.blockUpdated(param1, Blocks.AIR);
            param2.updateNeighbourShapes(param0, param1, 3);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public DyeColor getColor() {
        return this.color;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public long getSeed(BlockState param0, BlockPos param1) {
        BlockPos var0 = param1.relative(param0.getValue(FACING), param0.getValue(PART) == BedPart.HEAD ? 0 : 1);
        return Mth.getSeed(var0.getX(), param1.getY(), var0.getZ());
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}
