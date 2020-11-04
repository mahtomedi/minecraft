package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class LightningRodBlock extends RodBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public LightningRodBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(POWERED, Boolean.valueOf(false)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(FACING, param0.getClickedFace());
    }

    @Override
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param0.getValue(POWERED) ? 15 : 0;
    }

    public void onLightningStrike(BlockState param0, Level param1, BlockPos param2) {
        param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(true)), 3);
        param1.getBlockTicks().scheduleTick(param2, this, 8);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(false)), 3);
    }

    @Override
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
        if (param0.isThundering() && param3 instanceof ThrownTrident && ((ThrownTrident)param3).isChanneling()) {
            BlockPos var0 = param2.getBlockPos();
            if (param0.canSeeSky(var0)) {
                LightningBolt var1 = EntityType.LIGHTNING_BOLT.create(param0);
                var1.moveTo(Vec3.atBottomCenterOf(var0));
                Entity var2 = param3.getOwner();
                var1.setCause(var2 instanceof ServerPlayer ? (ServerPlayer)var2 : null);
                param0.addFreshEntity(var1);
                param0.playSound(null, var0, SoundEvents.TRIDENT_THUNDER, SoundSource.WEATHER, 5.0F, 1.0F);
            }
        }

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, POWERED);
    }

    @Override
    public boolean isSignalSource(BlockState param0) {
        return true;
    }
}
