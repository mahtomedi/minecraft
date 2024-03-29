package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class TntBlock extends Block {
    public static final MapCodec<TntBlock> CODEC = simpleCodec(TntBlock::new);
    public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;

    @Override
    public MapCodec<TntBlock> codec() {
        return CODEC;
    }

    public TntBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.defaultBlockState().setValue(UNSTABLE, Boolean.valueOf(false)));
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param3.is(param0.getBlock())) {
            if (param1.hasNeighborSignal(param2)) {
                explode(param1, param2);
                param1.removeBlock(param2, false);
            }

        }
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        if (param1.hasNeighborSignal(param2)) {
            explode(param1, param2);
            param1.removeBlock(param2, false);
        }

    }

    @Override
    public BlockState playerWillDestroy(Level param0, BlockPos param1, BlockState param2, Player param3) {
        if (!param0.isClientSide() && !param3.isCreative() && param2.getValue(UNSTABLE)) {
            explode(param0, param1);
        }

        return super.playerWillDestroy(param0, param1, param2, param3);
    }

    @Override
    public void wasExploded(Level param0, BlockPos param1, Explosion param2) {
        if (!param0.isClientSide) {
            PrimedTnt var0 = new PrimedTnt(
                param0, (double)param1.getX() + 0.5, (double)param1.getY(), (double)param1.getZ() + 0.5, param2.getIndirectSourceEntity()
            );
            int var1 = var0.getFuse();
            var0.setFuse((short)(param0.random.nextInt(var1 / 4) + var1 / 8));
            param0.addFreshEntity(var0);
        }
    }

    public static void explode(Level param0, BlockPos param1) {
        explode(param0, param1, null);
    }

    private static void explode(Level param0, BlockPos param1, @Nullable LivingEntity param2) {
        if (!param0.isClientSide) {
            PrimedTnt var0 = new PrimedTnt(param0, (double)param1.getX() + 0.5, (double)param1.getY(), (double)param1.getZ() + 0.5, param2);
            param0.addFreshEntity(var0);
            param0.playSound(null, var0.getX(), var0.getY(), var0.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            param0.gameEvent(param2, GameEvent.PRIME_FUSE, param1);
        }
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        ItemStack var0 = param3.getItemInHand(param4);
        if (!var0.is(Items.FLINT_AND_STEEL) && !var0.is(Items.FIRE_CHARGE)) {
            return super.use(param0, param1, param2, param3, param4, param5);
        } else {
            explode(param1, param2, param3);
            param1.setBlock(param2, Blocks.AIR.defaultBlockState(), 11);
            Item var1 = var0.getItem();
            if (!param3.isCreative()) {
                if (var0.is(Items.FLINT_AND_STEEL)) {
                    var0.hurtAndBreak(1, param3, param1x -> param1x.broadcastBreakEvent(param4));
                } else {
                    var0.shrink(1);
                }
            }

            param3.awardStat(Stats.ITEM_USED.get(var1));
            return InteractionResult.sidedSuccess(param1.isClientSide);
        }
    }

    @Override
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
        if (!param0.isClientSide) {
            BlockPos var0 = param2.getBlockPos();
            Entity var1 = param3.getOwner();
            if (param3.isOnFire() && param3.mayInteract(param0, var0)) {
                explode(param0, var0, var1 instanceof LivingEntity ? (LivingEntity)var1 : null);
                param0.removeBlock(var0, false);
            }
        }

    }

    @Override
    public boolean dropFromExplosion(Explosion param0) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(UNSTABLE);
    }
}
