package net.minecraft.world.damagesource;

import net.minecraft.world.phys.Vec3;

public class PointDamageSource extends DamageSource {
    private final Vec3 damageSourcePosition;

    public PointDamageSource(String param0, Vec3 param1) {
        super(param0);
        this.damageSourcePosition = param1;
    }

    @Override
    public Vec3 getSourcePosition() {
        return this.damageSourcePosition;
    }
}
