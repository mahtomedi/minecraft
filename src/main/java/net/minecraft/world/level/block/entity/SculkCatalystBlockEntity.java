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

public class SculkCatalystBlockEntity extends BlockEntity implements GameEventListener {
    private final BlockPositionSource blockPosSource = new BlockPositionSource(this.worldPosition);
    private final SculkSpreader sculkSpreader = SculkSpreader.createLevelSpreader();

    public SculkCatalystBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.SCULK_CATALYST, param0, param1);
    }

    @Override
    public boolean handleEventsImmediately() {
        return true;
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
    public boolean handleGameEvent(ServerLevel param0, GameEvent.Message param1) {
        if (this.isRemoved()) {
            return false;
        } else {
            GameEvent.Context var0 = param1.context();
            if (param1.gameEvent() == GameEvent.ENTITY_DIE) {
                Entity var2 = var0.sourceEntity();
                if (var2 instanceof LivingEntity var1) {
                    if (!var1.wasExperienceConsumed()) {
                        int var2x = var1.getExperienceReward();
                        if (var1.shouldDropExperience() && var2x > 0) {
                            this.sculkSpreader.addCursors(new BlockPos(param1.source().relative(Direction.UP, 0.5)), var2x);
                        }

                        var1.skipDropExperience();
                        LivingEntity var3 = var1.getLastHurtByMob();
                        if (var3 instanceof ServerPlayer var4) {
                            DamageSource var5x = var1.getLastDamageSource() == null ? DamageSource.playerAttack(var4) : var1.getLastDamageSource();
                            CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.trigger(var4, var0.sourceEntity(), var5x);
                        }

                        SculkCatalystBlock.bloom(param0, this.worldPosition, this.getBlockState(), param0.getRandom());
                    }

                    return true;
                }
            }

            return false;
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
