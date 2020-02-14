package net.minecraft.world.entity.ai.behavior;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.SerializableUUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class BehaviorUtils {
    public static void lockGazeAndWalkToEachOther(LivingEntity param0, LivingEntity param1) {
        lookAtEachOther(param0, param1);
        setWalkAndLookTargetMemoriesToEachOther(param0, param1);
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
        param0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(param1));
    }

    private static void setWalkAndLookTargetMemoriesToEachOther(LivingEntity param0, LivingEntity param1) {
        int var0 = 2;
        setWalkAndLookTargetMemories(param0, param1, 2);
        setWalkAndLookTargetMemories(param1, param0, 2);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity param0, Entity param1, int param2) {
        PositionWrapper var0 = new EntityPosWrapper(param1);
        setWalkAndLookTargetMemories(param0, var0, param2);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity param0, BlockPos param1, int param2) {
        PositionWrapper var0 = new BlockPosWrapper(param1);
        setWalkAndLookTargetMemories(param0, var0, param2);
    }

    private static void setWalkAndLookTargetMemories(LivingEntity param0, PositionWrapper param1, int param2) {
        float var0 = (float)param0.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
        WalkTarget var1 = new WalkTarget(param1, var0, param2);
        param0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, param1);
        param0.getBrain().setMemory(MemoryModuleType.WALK_TARGET, var1);
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

    public static boolean isAttackTargetVisibleAndInRange(LivingEntity param0, double param1) {
        Brain<?> var0 = param0.getBrain();
        if (!var0.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        } else {
            LivingEntity var1 = var0.getMemory(MemoryModuleType.ATTACK_TARGET).get();
            return !canSee(param0, var1) ? false : var1.closerThan(param0, param1);
        }
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

    public static Optional<LivingEntity> getLivingEntityFromUUIDMemory(LivingEntity param0, MemoryModuleType<SerializableUUID> param1) {
        Optional<SerializableUUID> var0 = param0.getBrain().getMemory(param1);
        return var0.map(SerializableUUID::value).map(param1x -> (LivingEntity)((ServerLevel)param0.level).getEntity(param1x));
    }
}
