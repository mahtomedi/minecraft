package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

public class SetWalkTargetFromAttackTargetIfTargetOutOfReach extends Behavior<LivingEntity> {
    private final float speed;

    public SetWalkTargetFromAttackTargetIfTargetOutOfReach(float param0) {
        super(
            ImmutableMap.of(
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.VISIBLE_LIVING_ENTITIES,
                MemoryStatus.REGISTERED
            )
        );
        this.speed = param0;
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        if (BehaviorUtils.isAttackTargetVisibleAndInRange(param1, this.getAttackRange(param1))) {
            this.clearWalkTarget(param1);
        } else {
            this.setWalkTarget(param1, getAttackTarget(param1));
        }

    }

    private void setWalkTarget(LivingEntity param0, LivingEntity param1) {
        PositionWrapper var0 = new EntityPosWrapper(param1);
        param0.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var0, this.speed, 0));
    }

    private void clearWalkTarget(LivingEntity param0) {
        param0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    private static LivingEntity getAttackTarget(LivingEntity param0) {
        return param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    private double getAttackRange(LivingEntity param0) {
        return Math.max(this.getAttackRange(param0.getMainHandItem()), this.getAttackRange(param0.getOffhandItem()));
    }

    private double getAttackRange(ItemStack param0) {
        Item var0 = param0.getItem();
        return var0 instanceof ProjectileWeaponItem ? (double)((ProjectileWeaponItem)var0).getDefaultProjectileRange() - 1.0 : 1.5;
    }
}
