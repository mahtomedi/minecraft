package net.minecraft.world.entity.ai.behavior;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public class BehaviorUtils {
    private BehaviorUtils() {
    }

    public static void lockGazeAndWalkToEachOther(LivingEntity param0, LivingEntity param1, float param2) {
        lookAtEachOther(param0, param1);
        setWalkAndLookTargetMemoriesToEachOther(param0, param1, param2);
    }

    public static boolean entityIsVisible(Brain<?> param0, LivingEntity param1) {
        Optional<NearestVisibleLivingEntities> var0 = param0.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        return var0.isPresent() && var0.get().contains(param1);
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
        setWalkAndLookTargetMemories(param0, new EntityTracker(param1, true), param2, param3);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity param0, BlockPos param1, float param2, int param3) {
        setWalkAndLookTargetMemories(param0, new BlockPosTracker(param1), param2, param3);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity param0, PositionTracker param1, float param2, int param3) {
        WalkTarget var0 = new WalkTarget(param1, param2, param3);
        param0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, param1);
        param0.getBrain().setMemory(MemoryModuleType.WALK_TARGET, var0);
    }

    public static void throwItem(LivingEntity param0, ItemStack param1, Vec3 param2) {
        Vec3 var0 = new Vec3(0.3F, 0.3F, 0.3F);
        throwItem(param0, param1, param2, var0, 0.3F);
    }

    public static void throwItem(LivingEntity param0, ItemStack param1, Vec3 param2, Vec3 param3, float param4) {
        double var0 = param0.getEyeY() - (double)param4;
        ItemEntity var1 = new ItemEntity(param0.level(), param0.getX(), var0, param0.getZ(), param1);
        var1.setThrower(param0.getUUID());
        Vec3 var2 = param2.subtract(param0.position());
        var2 = var2.normalize().multiply(param3.x, param3.y, param3.z);
        var1.setDeltaMovement(var2);
        var1.setDefaultPickUpDelay();
        param0.level().addFreshEntity(var1);
    }

    public static SectionPos findSectionClosestToVillage(ServerLevel param0, SectionPos param1, int param2) {
        int var0 = param0.sectionsToVillage(param1);
        return SectionPos.cube(param1, param2)
            .filter(param2x -> param0.sectionsToVillage(param2x) < var0)
            .min(Comparator.comparingInt(param0::sectionsToVillage))
            .orElse(param1);
    }

    public static boolean isWithinAttackRange(Mob param0, LivingEntity param1, int param2) {
        Item var1 = param0.getMainHandItem().getItem();
        if (var1 instanceof ProjectileWeaponItem var0 && param0.canFireProjectileWeapon(var0)) {
            int var1x = var0.getDefaultProjectileRange() - param2;
            return param0.closerThan(param1, (double)var1x);
        }

        return param0.isWithinMeleeAttackRange(param1);
    }

    public static boolean isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(LivingEntity param0, LivingEntity param1, double param2) {
        Optional<LivingEntity> var0 = param0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (var0.isEmpty()) {
            return false;
        } else {
            double var1 = param0.distanceToSqr(var0.get().position());
            double var2 = param0.distanceToSqr(param1.position());
            return var2 > var1 + param2 * param2;
        }
    }

    public static boolean canSee(LivingEntity param0, LivingEntity param1) {
        Brain<?> var0 = param0.getBrain();
        return !var0.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            ? false
            : var0.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().contains(param1);
    }

    public static LivingEntity getNearestTarget(LivingEntity param0, Optional<LivingEntity> param1, LivingEntity param2) {
        return param1.isEmpty() ? param2 : getTargetNearestMe(param0, param1.get(), param2);
    }

    public static LivingEntity getTargetNearestMe(LivingEntity param0, LivingEntity param1, LivingEntity param2) {
        Vec3 var0 = param1.position();
        Vec3 var1 = param2.position();
        return param0.distanceToSqr(var0) < param0.distanceToSqr(var1) ? param1 : param2;
    }

    public static Optional<LivingEntity> getLivingEntityFromUUIDMemory(LivingEntity param0, MemoryModuleType<UUID> param1) {
        Optional<UUID> var0 = param0.getBrain().getMemory(param1);
        return var0.<Entity>map(param1x -> ((ServerLevel)param0.level()).getEntity(param1x))
            .map(param0x -> param0x instanceof LivingEntity var0x ? var0x : null);
    }

    @Nullable
    public static Vec3 getRandomSwimmablePos(PathfinderMob param0, int param1, int param2) {
        Vec3 var0 = DefaultRandomPos.getPos(param0, param1, param2);
        int var1 = 0;

        while(
            var0 != null
                && !param0.level()
                    .getBlockState(BlockPos.containing(var0))
                    .isPathfindable(param0.level(), BlockPos.containing(var0), PathComputationType.WATER)
                && var1++ < 10
        ) {
            var0 = DefaultRandomPos.getPos(param0, param1, param2);
        }

        return var0;
    }

    public static boolean isBreeding(LivingEntity param0) {
        return param0.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }
}
