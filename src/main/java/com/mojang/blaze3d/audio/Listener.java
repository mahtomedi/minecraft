package com.mojang.blaze3d.audio;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.openal.AL10;

@OnlyIn(Dist.CLIENT)
public class Listener {
    public static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);
    private float gain = 1.0F;

    public void setListenerPosition(Vec3 param0) {
        AL10.alListener3f(4100, (float)param0.x, (float)param0.y, (float)param0.z);
    }

    public void setListenerOrientation(Vec3 param0, Vec3 param1) {
        AL10.alListenerfv(4111, new float[]{(float)param0.x, (float)param0.y, (float)param0.z, (float)param1.x, (float)param1.y, (float)param1.z});
    }

    public void setGain(float param0) {
        AL10.alListenerf(4106, param0);
        this.gain = param0;
    }

    public float getGain() {
        return this.gain;
    }

    public void reset() {
        this.setListenerPosition(Vec3.ZERO);
        this.setListenerOrientation(new Vec3(0.0, 0.0, -1.0), UP);
    }
}
