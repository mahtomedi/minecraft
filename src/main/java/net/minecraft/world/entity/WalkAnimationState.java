package net.minecraft.world.entity;

import net.minecraft.util.Mth;

public class WalkAnimationState {
    private float speedOld;
    private float speed;
    private float position;

    public void setSpeed(float param0) {
        this.speed = param0;
    }

    public void update(float param0, float param1) {
        this.speedOld = this.speed;
        this.speed += (param0 - this.speed) * param1;
        this.position += this.speed;
    }

    public float speed() {
        return this.speed;
    }

    public float speed(float param0) {
        return Mth.lerp(param0, this.speedOld, this.speed);
    }

    public float position() {
        return this.position;
    }

    public float position(float param0) {
        return this.position - this.speed * (1.0F - param0);
    }

    public boolean isMoving() {
        return this.speed > 1.0E-5F;
    }
}
