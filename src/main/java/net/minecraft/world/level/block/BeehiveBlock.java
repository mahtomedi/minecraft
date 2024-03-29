package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BeehiveBlock extends BaseEntityBlock {
    public static final MapCodec<BeehiveBlock> CODEC = simpleCodec(BeehiveBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty HONEY_LEVEL = BlockStateProperties.LEVEL_HONEY;
    public static final int MAX_HONEY_LEVELS = 5;
    private static final int SHEARED_HONEYCOMB_COUNT = 3;

    @Override
    public MapCodec<BeehiveBlock> codec() {
        return CODEC;
    }

    public BeehiveBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(HONEY_LEVEL, Integer.valueOf(0)).setValue(FACING, Direction.NORTH));
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        return param0.getValue(HONEY_LEVEL);
    }

    @Override
    public void playerDestroy(Level param0, Player param1, BlockPos param2, BlockState param3, @Nullable BlockEntity param4, ItemStack param5) {
        super.playerDestroy(param0, param1, param2, param3, param4, param5);
        if (!param0.isClientSide && param4 instanceof BeehiveBlockEntity var0) {
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, param5) == 0) {
                var0.emptyAllLivingFromHive(param1, param3, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
                param0.updateNeighbourForOutputSignal(param2, this);
                this.angerNearbyBees(param0, param2);
            }

            CriteriaTriggers.BEE_NEST_DESTROYED.trigger((ServerPlayer)param1, param3, param5, var0.getOccupantCount());
        }

    }

    private void angerNearbyBees(Level param0, BlockPos param1) {
        AABB var0 = new AABB(param1).inflate(8.0, 6.0, 8.0);
        List<Bee> var1 = param0.getEntitiesOfClass(Bee.class, var0);
        if (!var1.isEmpty()) {
            List<Player> var2 = param0.getEntitiesOfClass(Player.class, var0);
            if (var2.isEmpty()) {
                return;
            }

            for(Bee var3 : var1) {
                if (var3.getTarget() == null) {
                    Player var4 = Util.getRandom(var2, param0.random);
                    var3.setTarget(var4);
                }
            }
        }

    }

    public static void dropHoneycomb(Level param0, BlockPos param1) {
        popResource(param0, param1, new ItemStack(Items.HONEYCOMB, 3));
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        ItemStack var0 = param3.getItemInHand(param4);
        int var1 = param0.getValue(HONEY_LEVEL);
        boolean var2 = false;
        if (var1 >= 5) {
            Item var3 = var0.getItem();
            if (var0.is(Items.SHEARS)) {
                param1.playSound(param3, param3.getX(), param3.getY(), param3.getZ(), SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
                dropHoneycomb(param1, param2);
                var0.hurtAndBreak(1, param3, param1x -> param1x.broadcastBreakEvent(param4));
                var2 = true;
                param1.gameEvent(param3, GameEvent.SHEAR, param2);
            } else if (var0.is(Items.GLASS_BOTTLE)) {
                var0.shrink(1);
                param1.playSound(param3, param3.getX(), param3.getY(), param3.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (var0.isEmpty()) {
                    param3.setItemInHand(param4, new ItemStack(Items.HONEY_BOTTLE));
                } else if (!param3.getInventory().add(new ItemStack(Items.HONEY_BOTTLE))) {
                    param3.drop(new ItemStack(Items.HONEY_BOTTLE), false);
                }

                var2 = true;
                param1.gameEvent(param3, GameEvent.FLUID_PICKUP, param2);
            }

            if (!param1.isClientSide() && var2) {
                param3.awardStat(Stats.ITEM_USED.get(var3));
            }
        }

        if (var2) {
            if (!CampfireBlock.isSmokeyPos(param1, param2)) {
                if (this.hiveContainsBees(param1, param2)) {
                    this.angerNearbyBees(param1, param2);
                }

                this.releaseBeesAndResetHoneyLevel(param1, param0, param2, param3, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
            } else {
                this.resetHoneyLevel(param1, param0, param2);
            }

            return InteractionResult.sidedSuccess(param1.isClientSide);
        } else {
            return super.use(param0, param1, param2, param3, param4, param5);
        }
    }

    private boolean hiveContainsBees(Level param0, BlockPos param1) {
        BlockEntity var0 = param0.getBlockEntity(param1);
        if (var0 instanceof BeehiveBlockEntity var1) {
            return !var1.isEmpty();
        } else {
            return false;
        }
    }

    public void releaseBeesAndResetHoneyLevel(
        Level param0, BlockState param1, BlockPos param2, @Nullable Player param3, BeehiveBlockEntity.BeeReleaseStatus param4
    ) {
        this.resetHoneyLevel(param0, param1, param2);
        BlockEntity var0 = param0.getBlockEntity(param2);
        if (var0 instanceof BeehiveBlockEntity var1) {
            var1.emptyAllLivingFromHive(param3, param1, param4);
        }

    }

    public void resetHoneyLevel(Level param0, BlockState param1, BlockPos param2) {
        param0.setBlock(param2, param1.setValue(HONEY_LEVEL, Integer.valueOf(0)), 3);
    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, RandomSource param3) {
        if (param0.getValue(HONEY_LEVEL) >= 5) {
            for(int var0 = 0; var0 < param3.nextInt(1) + 1; ++var0) {
                this.trySpawnDripParticles(param1, param2, param0);
            }
        }

    }

    private void trySpawnDripParticles(Level param0, BlockPos param1, BlockState param2) {
        if (param2.getFluidState().isEmpty() && !(param0.random.nextFloat() < 0.3F)) {
            VoxelShape var0 = param2.getCollisionShape(param0, param1);
            double var1 = var0.max(Direction.Axis.Y);
            if (var1 >= 1.0 && !param2.is(BlockTags.IMPERMEABLE)) {
                double var2 = var0.min(Direction.Axis.Y);
                if (var2 > 0.0) {
                    this.spawnParticle(param0, param1, var0, (double)param1.getY() + var2 - 0.05);
                } else {
                    BlockPos var3 = param1.below();
                    BlockState var4 = param0.getBlockState(var3);
                    VoxelShape var5 = var4.getCollisionShape(param0, var3);
                    double var6 = var5.max(Direction.Axis.Y);
                    if ((var6 < 1.0 || !var4.isCollisionShapeFullBlock(param0, var3)) && var4.getFluidState().isEmpty()) {
                        this.spawnParticle(param0, param1, var0, (double)param1.getY() - 0.05);
                    }
                }
            }

        }
    }

    private void spawnParticle(Level param0, BlockPos param1, VoxelShape param2, double param3) {
        this.spawnFluidParticle(
            param0,
            (double)param1.getX() + param2.min(Direction.Axis.X),
            (double)param1.getX() + param2.max(Direction.Axis.X),
            (double)param1.getZ() + param2.min(Direction.Axis.Z),
            (double)param1.getZ() + param2.max(Direction.Axis.Z),
            param3
        );
    }

    private void spawnFluidParticle(Level param0, double param1, double param2, double param3, double param4, double param5) {
        param0.addParticle(
            ParticleTypes.DRIPPING_HONEY,
            Mth.lerp(param0.random.nextDouble(), param1, param2),
            param5,
            Mth.lerp(param0.random.nextDouble(), param3, param4),
            0.0,
            0.0,
            0.0
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(FACING, param0.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(HONEY_LEVEL, FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new BeehiveBlockEntity(param0, param1);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level param0, BlockState param1, BlockEntityType<T> param2) {
        return param0.isClientSide ? null : createTickerHelper(param2, BlockEntityType.BEEHIVE, BeehiveBlockEntity::serverTick);
    }

    @Override
    public BlockState playerWillDestroy(Level param0, BlockPos param1, BlockState param2, Player param3) {
        if (!param0.isClientSide && param3.isCreative() && param0.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            BlockEntity var0 = param0.getBlockEntity(param1);
            if (var0 instanceof BeehiveBlockEntity var1) {
                ItemStack var2 = new ItemStack(this);
                int var3 = param2.getValue(HONEY_LEVEL);
                boolean var4 = !var1.isEmpty();
                if (var4 || var3 > 0) {
                    if (var4) {
                        CompoundTag var5 = new CompoundTag();
                        var5.put("Bees", var1.writeBees());
                        BlockItem.setBlockEntityData(var2, BlockEntityType.BEEHIVE, var5);
                    }

                    CompoundTag var6 = new CompoundTag();
                    var6.putInt("honey_level", var3);
                    var2.addTagElement("BlockStateTag", var6);
                    ItemEntity var7 = new ItemEntity(param0, (double)param1.getX(), (double)param1.getY(), (double)param1.getZ(), var2);
                    var7.setDefaultPickUpDelay();
                    param0.addFreshEntity(var7);
                }
            }
        }

        return super.playerWillDestroy(param0, param1, param2, param3);
    }

    @Override
    public List<ItemStack> getDrops(BlockState param0, LootParams.Builder param1) {
        Entity var0 = param1.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (var0 instanceof PrimedTnt || var0 instanceof Creeper || var0 instanceof WitherSkull || var0 instanceof WitherBoss || var0 instanceof MinecartTNT) {
            BlockEntity var1 = param1.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
            if (var1 instanceof BeehiveBlockEntity var2) {
                var2.emptyAllLivingFromHive(null, param0, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
            }
        }

        return super.getDrops(param0, param1);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param3.getBlockState(param5).getBlock() instanceof FireBlock) {
            BlockEntity var0 = param3.getBlockEntity(param4);
            if (var0 instanceof BeehiveBlockEntity var1) {
                var1.emptyAllLivingFromHive(null, param0, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
            }
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(FACING, param1.rotate(param0.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.rotate(param1.getRotation(param0.getValue(FACING)));
    }
}
