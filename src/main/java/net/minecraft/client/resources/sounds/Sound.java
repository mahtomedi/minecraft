package net.minecraft.client.resources.sounds;

import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Sound implements Weighted<Sound> {
    private final ResourceLocation location;
    private final float volume;
    private final float pitch;
    private final int weight;
    private final Sound.Type type;
    private final boolean stream;
    private final boolean preload;
    private final int attenuationDistance;

    public Sound(String param0, float param1, float param2, int param3, Sound.Type param4, boolean param5, boolean param6, int param7) {
        this.location = new ResourceLocation(param0);
        this.volume = param1;
        this.pitch = param2;
        this.weight = param3;
        this.type = param4;
        this.stream = param5;
        this.preload = param6;
        this.attenuationDistance = param7;
    }

    public ResourceLocation getLocation() {
        return this.location;
    }

    public ResourceLocation getPath() {
        return new ResourceLocation(this.location.getNamespace(), "sounds/" + this.location.getPath() + ".ogg");
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    public Sound getSound() {
        return this;
    }

    @Override
    public void preloadIfRequired(SoundEngine param0) {
        if (this.preload) {
            param0.requestPreload(this);
        }

    }

    public Sound.Type getType() {
        return this.type;
    }

    public boolean shouldStream() {
        return this.stream;
    }

    public boolean shouldPreload() {
        return this.preload;
    }

    public int getAttenuationDistance() {
        return this.attenuationDistance;
    }

    @Override
    public String toString() {
        return "Sound[" + this.location + "]";
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Type {
        FILE("file"),
        SOUND_EVENT("event");

        private final String name;

        private Type(String param0) {
            this.name = param0;
        }

        public static Sound.Type getByName(String param0) {
            for(Sound.Type var0 : values()) {
                if (var0.name.equals(param0)) {
                    return var0;
                }
            }

            return null;
        }
    }
}
