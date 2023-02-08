package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public class SculkCatalystBlockEntity extends BlockEntity implements GameEventListener {
    private final BlockPositionSource blockPosSource = new BlockPositionSource(this.worldPosition);
    private final SculkSpreader sculkSpreader = SculkSpreader.createLevelSpreader();

    public SculkCatalystBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.SCULK_CATALYST, param0, param1);
    }

    @Override
    public PositionSource getListenerSource() {
        return this.blockPosSource;
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
                        this.sculkSpreader.addCursors(new BlockPos(param3.relative(Direction.UP, 0.5)), var1x);
                        this.tryAwardItSpreadsAdvancement(var0);
                    }

                    var0.skipDropExperience();
                    SculkCatalystBlock.bloom(param0, this.worldPosition, this.getBlockState(), param0.getRandom());
                }

                return true;
            }
        }

        return false;
    }

    private void tryAwardItSpreadsAdvancement(LivingEntity param0) {
        LivingEntity var0 = param0.getLastHurtByMob();
        if (var0 instanceof ServerPlayer var1) {
            DamageSource var2 = param0.getLastDamageSource() == null ? this.level.damageSources().playerAttack(var1) : param0.getLastDamageSource();
            CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.trigger(var1, param0, var2);
        }

    }

    public static void serverTick(Level param0, BlockPos param1, BlockState param2, SculkCatalystBlockEntity param3) {
        param3.sculkSpreader.updateCursors(param0, param1, param0.getRandom(), true);
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.sculkSpreader.load(param0);
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        this.sculkSpreader.save(param0);
        super.saveAdditional(param0);
    }

    @VisibleForTesting
    public SculkSpreader getSculkSpreader() {
        return this.sculkSpreader;
    }
}
