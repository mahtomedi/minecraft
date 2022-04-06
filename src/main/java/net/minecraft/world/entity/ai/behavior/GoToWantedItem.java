package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;

public class GoToWantedItem<E extends LivingEntity> extends Behavior<E> {
    private final Predicate<E> predicate;
    private final int maxDistToWalk;
    private final float speedModifier;

    public GoToWantedItem(float param0, boolean param1, int param2) {
        this(param0x -> true, param0, param1, param2);
    }

    public GoToWantedItem(Predicate<E> param0, float param1, boolean param2, int param3) {
        super(
            ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET,
                param2 ? MemoryStatus.REGISTERED : MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
                MemoryStatus.VALUE_PRESENT
            )
        );
        this.predicate = param0;
        this.maxDistToWalk = param3;
        this.speedModifier = param1;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        return !this.isOnPickupCooldown(param1)
            && this.predicate.test(param1)
            && this.getClosestLovedItem(param1).closerThan(param1, (double)this.maxDistToWalk);
    }

    @Override
    protected void start(ServerLevel param0, E param1, long param2) {
        BehaviorUtils.setWalkAndLookTargetMemories(param1, this.getClosestLovedItem(param1), this.speedModifier, 0);
    }

    private boolean isOnPickupCooldown(E param0) {
        return param0.getBrain().checkMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryStatus.VALUE_PRESENT);
    }

    private ItemEntity getClosestLovedItem(E param0) {
        return param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).get();
    }
}
