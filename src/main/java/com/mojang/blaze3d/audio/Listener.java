package com.mojang.blaze3d.audio;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;

@OnlyIn(Dist.CLIENT)
public class Listener {
    private float gain = 1.0F;
    private Vec3 position = Vec3.ZERO;

    public void setListenerPosition(Vec3 param0) {
        this.position = param0;
        AL10.alListener3f(4100, (float)param0.x, (float)param0.y, (float)param0.z);
    }

    public Vec3 getListenerPosition() {
        return this.position;
    }

    public void setListenerOrientation(Vector3f param0, Vector3f param1) {
        AL10.alListenerfv(4111, new float[]{param0.x(), param0.y(), param0.z(), param1.x(), param1.y(), param1.z()});
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
        this.setListenerOrientation(new Vector3f(0.0F, 0.0F, -1.0F), new Vector3f(0.0F, 1.0F, 0.0F));
    }
}
