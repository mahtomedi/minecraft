package net.minecraft.client.resources.sounds;

import javax.annotation.Nullable;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.SampledFloat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Sound implements Weighted<Sound> {
    public static final FileToIdConverter SOUND_LISTER = new FileToIdConverter("sounds", ".ogg");
    private final ResourceLocation location;
    private final SampledFloat volume;
    private final SampledFloat pitch;
    private final int weight;
    private final Sound.Type type;
    private final boolean stream;
    private final boolean preload;
    private final int attenuationDistance;

    public Sound(String param0, SampledFloat param1, SampledFloat param2, int param3, Sound.Type param4, boolean param5, boolean param6, int param7) {
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
        return SOUND_LISTER.idToFile(this.location);
    }

    public SampledFloat getVolume() {
        return this.volume;
    }

    public SampledFloat getPitch() {
        return this.pitch;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    public Sound getSound(RandomSource param0) {
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

        @Nullable
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
