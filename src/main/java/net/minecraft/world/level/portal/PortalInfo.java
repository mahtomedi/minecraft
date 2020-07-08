package net.minecraft.world.level.portal;

import net.minecraft.world.phys.Vec3;

public class PortalInfo {
    public final Vec3 pos;
    public final Vec3 speed;
    public final float yRot;
    public final float xRot;

    public PortalInfo(Vec3 param0, Vec3 param1, float param2, float param3) {
        this.pos = param0;
        this.speed = param1;
        this.yRot = param2;
        this.xRot = param3;
    }
}
