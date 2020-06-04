package net.minecraft.world.entity.ai.behavior;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.phys.Vec3;

public class BehaviorUtils {
    public static void lockGazeAndWalkToEachOther(LivingEntity param0, LivingEntity param1, float param2) {
        lookAtEachOther(param0, param1);
        setWalkAndLookTargetMemoriesToEachOther(param0, param1, param2);
    }

    public static boolean entityIsVisible(Brain<?> param0, LivingEntity param1) {
        return param0.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).filter(param1x -> param1x.contains(param1)).isPresent();
    }

    public static boolean targetIsValid(Brain<?> param0, MemoryModuleType<? extends LivingEntity> param1, EntityType<?> param2) {
        return targetIsValid(param0, param1, param1x -> param1x.getType() == param2);
    }

    private static boolean targetIsValid(Brain<?> param0, MemoryModuleType<? extends LivingEntity> param1, Predicate<LivingEntity> param2) {
        return param0.getMemory(param1).filter(param2).filter(LivingEntity::isAlive).filter(param1x -> entityIsVisible(param0, param1x)).isPresent();
    }

    private static void lookAtEachOther(LivingEntity param0, LivingEntity param1) {
        lookAtEntity(param0, param1);
        lookAtEntity(param1, param0);
    }

    public static void lookAtEntity(LivingEntity param0, LivingEntity param1) {
        param0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(param1, true));
    }

    private static void setWalkAndLookTargetMemoriesToEachOther(LivingEntity param0, LivingEntity param1, float param2) {
        int var0 = 2;
        setWalkAndLookTargetMemories(param0, param1, param2, 2);
        setWalkAndLookTargetMemories(param1, param0, param2, 2);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity param0, Entity param1, float param2, int param3) {
        WalkTarget var0 = new WalkTarget(new EntityTracker(param1, false), param2, param3);
        param0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(param1, true));
        param0.getBrain().setMemory(MemoryModuleType.WALK_TARGET, var0);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity param0, BlockPos param1, float param2, int param3) {
        WalkTarget var0 = new WalkTarget(new BlockPosTracker(param1), param2, param3);
        param0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(param1));
        param0.getBrain().setMemory(MemoryModuleType.WALK_TARGET, var0);
    }

    public static void throwItem(LivingEntity param0, ItemStack param1, Vec3 param2) {
        double var0 = param0.getEyeY() - 0.3F;
        ItemEntity var1 = new ItemEntity(param0.level, param0.getX(), var0, param0.getZ(), param1);
        float var2 = 0.3F;
        Vec3 var3 = param2.subtract(param0.position());
        var3 = var3.normalize().scale(0.3F);
        var1.setDeltaMovement(var3);
        var1.setDefaultPickUpDelay();
        param0.level.addFreshEntity(var1);
    }

    public static SectionPos findSectionClosestToVillage(ServerLevel param0, SectionPos param1, int param2) {
        int var0 = param0.sectionsToVillage(param1);
        return SectionPos.cube(param1, param2)
            .filter(param2x -> param0.sectionsToVillage(param2x) < var0)
            .min(Comparator.comparingInt(param0::sectionsToVillage))
            .orElse(param1);
    }

    public static boolean isWithinAttackRange(Mob param0, LivingEntity param1, int param2) {
        Item var0 = param0.getMainHandItem().getItem();
        if (var0 instanceof ProjectileWeaponItem && param0.canFireProjectileWeapon((ProjectileWeaponItem)var0)) {
            int var1 = ((ProjectileWeaponItem)var0).getDefaultProjectileRange() - param2;
            return param0.closerThan(param1, (double)var1);
        } else {
            return isWithinMeleeAttackRange(param0, param1);
        }
    }

    public static boolean isWithinMeleeAttackRange(LivingEntity param0, LivingEntity param1) {
        double var0 = param0.distanceToSqr(param1.getX(), param1.getY(), param1.getZ());
        double var1 = (double)(param0.getBbWidth() * 2.0F * param0.getBbWidth() * 2.0F + param1.getBbWidth());
        return var0 <= var1;
    }

    public static boolean isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(LivingEntity param0, LivingEntity param1, double param2) {
        Optional<LivingEntity> var0 = param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (!var0.isPresent()) {
            return false;
        } else {
            double var1 = param0.distanceToSqr(var0.get().position());
            double var2 = param0.distanceToSqr(param1.position());
            return var2 > var1 + param2 * param2;
        }
    }

    public static boolean canSee(LivingEntity param0, LivingEntity param1) {
        Brain<?> var0 = param0.getBrain();
        return !var0.hasMemoryValue(MemoryModuleType.VISIBLE_LIVING_ENTITIES)
            ? false
            : var0.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get().contains(param1);
    }

    public static LivingEntity getNearestTarget(LivingEntity param0, Optional<LivingEntity> param1, LivingEntity param2) {
        return !param1.isPresent() ? param2 : getTargetNearestMe(param0, param1.get(), param2);
    }

    public static LivingEntity getTargetNearestMe(LivingEntity param0, LivingEntity param1, LivingEntity param2) {
        Vec3 var0 = param1.position();
        Vec3 var1 = param2.position();
        return param0.distanceToSqr(var0) < param0.distanceToSqr(var1) ? param1 : param2;
    }

    public static Optional<LivingEntity> getLivingEntityFromUUIDMemory(LivingEntity param0, MemoryModuleType<UUID> param1) {
        Optional<UUID> var0 = param0.getBrain().getMemory(param1);
        return var0.map(param1x -> (LivingEntity)((ServerLevel)param0.level).getEntity(param1x));
    }

    public static Stream<Villager> getNearbyVillagersWithCondition(Villager param0, Predicate<Villager> param1) {
        return param0.getBrain()
            .getMemory(MemoryModuleType.LIVING_ENTITIES)
            .map(
                param2 -> param2.stream()
                        .filter(param1x -> param1x instanceof Villager && param1x != param0)
                        .map(param0x -> (Villager)param0x)
                        .filter(LivingEntity::isAlive)
                        .filter(param1)
            )
            .orElseGet(Stream::empty);
    }
}
