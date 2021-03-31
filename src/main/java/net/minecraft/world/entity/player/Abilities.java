package net.minecraft.world.entity.player;

import net.minecraft.nbt.CompoundTag;

public class Abilities {
    public boolean invulnerable;
    public boolean flying;
    public boolean mayfly;
    public boolean instabuild;
    public boolean mayBuild = true;
    private float flyingSpeed = 0.05F;
    private float walkingSpeed = 0.1F;

    public void addSaveData(CompoundTag param0) {
        CompoundTag var0 = new CompoundTag();
        var0.putBoolean("invulnerable", this.invulnerable);
        var0.putBoolean("flying", this.flying);
        var0.putBoolean("mayfly", this.mayfly);
        var0.putBoolean("instabuild", this.instabuild);
        var0.putBoolean("mayBuild", this.mayBuild);
        var0.putFloat("flySpeed", this.flyingSpeed);
        var0.putFloat("walkSpeed", this.walkingSpeed);
        param0.put("abilities", var0);
    }

    public void loadSaveData(CompoundTag param0) {
        if (param0.contains("abilities", 10)) {
            CompoundTag var0 = param0.getCompound("abilities");
            this.invulnerable = var0.getBoolean("invulnerable");
            this.flying = var0.getBoolean("flying");
            this.mayfly = var0.getBoolean("mayfly");
            this.instabuild = var0.getBoolean("instabuild");
            if (var0.contains("flySpeed", 99)) {
                this.flyingSpeed = var0.getFloat("flySpeed");
                this.walkingSpeed = var0.getFloat("walkSpeed");
            }

            if (var0.contains("mayBuild", 1)) {
                this.mayBuild = var0.getBoolean("mayBuild");
            }
        }

    }

    public float getFlyingSpeed() {
        return this.flyingSpeed;
    }

    public void setFlyingSpeed(float param0) {
        this.flyingSpeed = param0;
    }

    public float getWalkingSpeed() {
        return this.walkingSpeed;
    }

    public void setWalkingSpeed(float param0) {
        this.walkingSpeed = param0;
    }
}
