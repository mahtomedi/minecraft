package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

public class SculkCatalystBlockEntity extends BlockEntity implements GameEventListener.Holder<SculkCatalystBlockEntity.CatalystListener> {
    private final SculkCatalystBlockEntity.CatalystListener catalystListener;

    public SculkCatalystBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.SCULK_CATALYST, param0, param1);
        this.catalystListener = new SculkCatalystBlockEntity.CatalystListener(param1, new BlockPositionSource(param0));
    }

    public static void serverTick(Level param0, BlockPos param1, BlockState param2, SculkCatalystBlockEntity param3) {
        param3.catalystListener.getSculkSpreader().updateCursors(param0, param1, param0.getRandom(), true);
    }

    @Override
    public void load(CompoundTag param0) {
        this.catalystListener.sculkSpreader.load(param0);
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        this.catalystListener.sculkSpreader.save(param0);
        super.saveAdditional(param0);
    }

    public SculkCatalystBlockEntity.CatalystListener getListener() {
        return this.catalystListener;
    }

    public static class CatalystListener implements GameEventListener {
        public static final int PULSE_TICKS = 8;
        final SculkSpreader sculkSpreader;
        private final BlockState blockState;
        private final PositionSource positionSource;

        public CatalystListener(BlockState param0, PositionSource param1) {
            this.blockState = param0;
            this.positionSource = param1;
            this.sculkSpreader = SculkSpreader.createLevelSpreader();
        }

        @Override
        public PositionSource getListenerSource() {
            return this.positionSource;
        }

        @Override
        public int getListenerRadius() {
            return 8;
        }

        @Override
        public GameEventListener.DeliveryMode getDeliveryMode() {
            return GameEventListener.DeliveryMode.BY_DISTANCE;
        }

        @Override
        public boolean handleGameEvent(ServerLevel param0, GameEvent param1, GameEvent.Context param2, Vec3 param3) {
            if (param1 == GameEvent.ENTITY_DIE) {
                Entity var1 = param2.sourceEntity();
                if (var1 instanceof LivingEntity var0) {
                    if (!var0.wasExperienceConsumed()) {
                        int var1x = var0.getExperienceReward();
                        if (var0.shouldDropExperience() && var1x > 0) {
                            this.sculkSpreader.addCursors(BlockPos.containing(param3.relative(Direction.UP, 0.5)), var1x);
                            this.tryAwardItSpreadsAdvancement(param0, var0);
                        }

                        var0.skipDropExperience();
                        this.positionSource
                            .getPosition(param0)
                            .ifPresent(param1x -> this.bloom(param0, BlockPos.containing(param1x), this.blockState, param0.getRandom()));
                    }

                    return true;
                }
            }

            return false;
        }

        @VisibleForTesting
        public SculkSpreader getSculkSpreader() {
            return this.sculkSpreader;
        }

        private void bloom(ServerLevel param0, BlockPos param1, BlockState param2, RandomSource param3) {
            param0.setBlock(param1, param2.setValue(SculkCatalystBlock.PULSE, Boolean.valueOf(true)), 3);
            param0.scheduleTick(param1, param2.getBlock(), 8);
            param0.sendParticles(
                ParticleTypes.SCULK_SOUL, (double)param1.getX() + 0.5, (double)param1.getY() + 1.15, (double)param1.getZ() + 0.5, 2, 0.2, 0.0, 0.2, 0.0
            );
            param0.playSound(null, param1, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + param3.nextFloat() * 0.4F);
        }

        private void tryAwardItSpreadsAdvancement(Level param0, LivingEntity param1) {
            LivingEntity var0 = param1.getLastHurtByMob();
            if (var0 instanceof ServerPlayer var1) {
                DamageSource var2 = param1.getLastDamageSource() == null ? param0.damageSources().playerAttack(var1) : param1.getLastDamageSource();
                CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.trigger(var1, param1, var2);
            }

        }
    }
}
