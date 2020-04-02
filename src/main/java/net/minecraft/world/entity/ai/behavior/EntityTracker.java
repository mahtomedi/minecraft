package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;

public class EntityTracker implements PositionTracker {
    private final Entity entity;

    public EntityTracker(Entity param0) {
        this.entity = param0;
    }

    @Override
    public Vec3 currentPosition() {
        return this.entity.position();
    }

    @Override
    public BlockPos currentBlockPosition() {
        return this.entity.blockPosition();
    }

    @Override
    public boolean isVisibleBy(LivingEntity param0) {
        if (!(this.entity instanceof LivingEntity)) {
            return true;
        } else {
            Optional<List<LivingEntity>> var0 = param0.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES);
            return this.entity.isAlive() && var0.isPresent() && var0.get().contains(this.entity);
        }
    }

    @Override
    public String toString() {
        return "EntityTracker for " + this.entity;
    }
}
