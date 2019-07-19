package net.minecraft.world.entity.schedule;

public class Keyframe {
    private final int timeStamp;
    private final float value;

    public Keyframe(int param0, float param1) {
        this.timeStamp = param0;
        this.value = param1;
    }

    public int getTimeStamp() {
        return this.timeStamp;
    }

    public float getValue() {
        return this.value;
    }
}
