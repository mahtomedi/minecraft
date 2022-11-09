package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;

public class MeleeAttack {
    public static OneShot<Mob> create(int param0) {
        return BehaviorBuilder.create(
            param1 -> param1.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param1.registered(MemoryModuleType.LOOK_TARGET),
                        param1.present(MemoryModuleType.ATTACK_TARGET),
                        param1.absent(MemoryModuleType.ATTACK_COOLING_DOWN),
                        param1.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                    )
                    .apply(
                        param1,
                        (param2, param3, param4, param5) -> (param6, param7, param8) -> {
                                LivingEntity var0x = param1.get(param3);
                                if (!isHoldingUsableProjectileWeapon(param7)
                                    && param7.isWithinMeleeAttackRange(var0x)
                                    && param1.<NearestVisibleLivingEntities>get(param5).contains(var0x)) {
                                    param2.set(new EntityTracker(var0x, true));
                                    param7.swing(InteractionHand.MAIN_HAND);
                                    param7.doHurtTarget(var0x);
                                    param4.setWithExpiry(true, (long)param0);
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                    )
        );
    }

    private static boolean isHoldingUsableProjectileWeapon(Mob param0) {
        return param0.isHolding(param1 -> {
            Item var0x = param1.getItem();
            return var0x instanceof ProjectileWeaponItem && param0.canFireProjectileWeapon((ProjectileWeaponItem)var0x);
        });
    }
}
