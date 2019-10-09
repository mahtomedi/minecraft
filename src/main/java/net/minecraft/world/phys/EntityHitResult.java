package net.minecraft.world.phys;

import net.minecraft.world.entity.Entity;

public class EntityHitResult extends HitResult {
    private final Entity entity;

    public EntityHitResult(Entity param0) {
        this(param0, param0.position());
    }

    public EntityHitResult(Entity param0, Vec3 param1) {
        super(param1);
        this.entity = param0;
    }

    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public HitResult.Type getType() {
        return HitResult.Type.ENTITY;
    }
}
