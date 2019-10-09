package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;

public class EntityPosWrapper implements PositionWrapper {
    private final Entity entity;

    public EntityPosWrapper(Entity param0) {
        this.entity = param0;
    }

    @Override
    public BlockPos getPos() {
        return new BlockPos(this.entity);
    }

    @Override
    public Vec3 getLookAtPos() {
        return new Vec3(this.entity.getX(), this.entity.getEyeY(), this.entity.getZ());
    }

    @Override
    public boolean isVisible(LivingEntity param0) {
        Optional<List<LivingEntity>> var0 = param0.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES);
        return this.entity.isAlive() && var0.isPresent() && var0.get().contains(this.entity);
    }

    @Override
    public String toString() {
        return "EntityPosWrapper for " + this.entity;
    }
}
