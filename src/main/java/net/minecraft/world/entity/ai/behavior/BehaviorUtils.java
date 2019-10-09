package net.minecraft.world.entity.ai.behavior;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
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
        walkToEachOther(param0, param1);
    }

    public static boolean entityIsVisible(Brain<?> param0, LivingEntity param1) {
        return param0.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).filter(param1x -> param1x.contains(param1)).isPresent();
    }

    public static boolean targetIsValid(Brain<?> param0, MemoryModuleType<? extends LivingEntity> param1, EntityType<?> param2) {
        return param0.getMemory(param1)
            .filter(param1x -> param1x.getType() == param2)
            .filter(LivingEntity::isAlive)
            .filter(param1x -> entityIsVisible(param0, param1x))
            .isPresent();
    }

    public static void lookAtEachOther(LivingEntity param0, LivingEntity param1) {
        lookAtEntity(param0, param1);
        lookAtEntity(param1, param0);
    }

    public static void lookAtEntity(LivingEntity param0, LivingEntity param1) {
        param0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(param1));
    }

    public static void walkToEachOther(LivingEntity param0, LivingEntity param1) {
        int var0 = 2;
        walkToEntity(param0, param1, 2);
        walkToEntity(param1, param0, 2);
    }

    public static void walkToEntity(LivingEntity param0, LivingEntity param1, int param2) {
        float var0 = (float)param0.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
        EntityPosWrapper var1 = new EntityPosWrapper(param1);
        WalkTarget var2 = new WalkTarget(var1, var0, param2);
        param0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, var1);
        param0.getBrain().setMemory(MemoryModuleType.WALK_TARGET, var2);
    }

    public static void throwItem(LivingEntity param0, ItemStack param1, LivingEntity param2) {
        double var0 = param0.getEyeY() - 0.3F;
        ItemEntity var1 = new ItemEntity(param0.level, param0.getX(), var0, param0.getZ(), param1);
        BlockPos var2 = new BlockPos(param2);
        BlockPos var3 = new BlockPos(param0);
        float var4 = 0.3F;
        Vec3 var5 = new Vec3(var2.subtract(var3));
        var5 = var5.normalize().scale(0.3F);
        var1.setDeltaMovement(var5);
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
}
