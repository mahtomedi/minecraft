package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.allay.AllayAi;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class GoAndGiveItemsToTarget<E extends LivingEntity & InventoryCarrier> extends Behavior<E> {
    private static final int CLOSE_ENOUGH_DISTANCE_TO_TARGET = 3;
    private static final int ITEM_PICKUP_COOLDOWN_AFTER_THROWING = 100;
    private final Function<LivingEntity, Optional<PositionTracker>> targetPositionGetter;
    private final float speedModifier;

    public GoAndGiveItemsToTarget(Function<LivingEntity, Optional<PositionTracker>> param0, float param1) {
        super(
            Map.of(
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
                MemoryStatus.REGISTERED
            )
        );
        this.targetPositionGetter = param0;
        this.speedModifier = param1;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        return this.canThrowItemToTarget(param1);
    }

    @Override
    protected boolean canStillUse(ServerLevel param0, E param1, long param2) {
        return this.canThrowItemToTarget(param1);
    }

    @Override
    protected void start(ServerLevel param0, E param1, long param2) {
        this.targetPositionGetter.apply(param1).ifPresent(param1x -> BehaviorUtils.setWalkAndLookTargetMemories(param1, param1x, this.speedModifier, 3));
    }

    @Override
    protected void tick(ServerLevel param0, E param1, long param2) {
        Optional<PositionTracker> var0 = this.targetPositionGetter.apply(param1);
        if (!var0.isEmpty()) {
            PositionTracker var1 = var0.get();
            double var2 = var1.currentPosition().distanceTo(param1.getEyePosition());
            if (var2 < 3.0) {
                ItemStack var3 = param1.getInventory().removeItem(0, 1);
                if (!var3.isEmpty()) {
                    BehaviorUtils.throwItem(param1, var3, getThrowPosition(var1));
                    if (var1 instanceof EntityTracker var4) {
                        Entity var12 = var4.getEntity();
                        if (var12 instanceof ServerPlayer var5) {
                            CriteriaTriggers.ITEM_DELIVERED_TO_PLAYER.trigger(var5);
                        }
                    }

                    if (param1 instanceof Allay var6) {
                        AllayAi.getLikedPlayer(var6).ifPresent(param2x -> this.triggerDropItemOnBlock(var1, var3, param2x));
                    }

                    param1.getBrain().setMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, 100);
                }
            }

        }
    }

    private void triggerDropItemOnBlock(PositionTracker param0, ItemStack param1, ServerPlayer param2) {
        BlockPos var0 = param0.currentBlockPosition().below();
        CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.trigger(param2, var0, param1);
    }

    private boolean canThrowItemToTarget(E param0) {
        return !param0.getInventory().isEmpty() && this.targetPositionGetter.apply(param0).isPresent();
    }

    private static Vec3 getThrowPosition(PositionTracker param0) {
        return param0.currentPosition().add(0.0, 1.0, 0.0);
    }
}
