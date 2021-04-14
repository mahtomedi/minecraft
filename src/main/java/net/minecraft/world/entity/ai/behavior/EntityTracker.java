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
    private final boolean trackEyeHeight;

    public EntityTracker(Entity param0, boolean param1) {
        this.entity = param0;
        this.trackEyeHeight = param1;
    }

    @Override
    public Vec3 currentPosition() {
        return this.trackEyeHeight ? this.entity.position().add(0.0, (double)this.entity.getEyeHeight(), 0.0) : this.entity.position();
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
            Optional<List<LivingEntity>> var0 = param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
            return this.entity.isAlive() && var0.isPresent() && var0.get().contains(this.entity);
        }
    }

    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public String toString() {
        return "EntityTracker for " + this.entity;
    }
}
