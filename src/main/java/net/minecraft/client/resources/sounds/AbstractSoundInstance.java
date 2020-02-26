package net.minecraft.client.resources.sounds;

import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSoundInstance implements SoundInstance {
    protected Sound sound;
    protected final SoundSource source;
    protected final ResourceLocation location;
    protected float volume = 1.0F;
    protected float pitch = 1.0F;
    protected float x;
    protected float y;
    protected float z;
    protected boolean looping;
    protected int delay;
    protected SoundInstance.Attenuation attenuation = SoundInstance.Attenuation.LINEAR;
    protected boolean priority;
    protected boolean relative;

    protected AbstractSoundInstance(SoundEvent param0, SoundSource param1) {
        this(param0.getLocation(), param1);
    }

    protected AbstractSoundInstance(ResourceLocation param0, SoundSource param1) {
        this.location = param0;
        this.source = param1;
    }

    @Override
    public ResourceLocation getLocation() {
        return this.location;
    }

    @Override
    public WeighedSoundEvents resolve(SoundManager param0) {
        WeighedSoundEvents var0 = param0.getSoundEvent(this.location);
        if (var0 == null) {
            this.sound = SoundManager.EMPTY_SOUND;
        } else {
            this.sound = var0.getSound();
        }

        return var0;
    }

    @Override
    public Sound getSound() {
        return this.sound;
    }

    @Override
    public SoundSource getSource() {
        return this.source;
    }

    @Override
    public boolean isLooping() {
        return this.looping;
    }

    @Override
    public int getDelay() {
        return this.delay;
    }

    @Override
    public float getVolume() {
        return this.volume * this.sound.getVolume();
    }

    @Override
    public float getPitch() {
        return this.pitch * this.sound.getPitch();
    }

    @Override
    public float getX() {
        return this.x;
    }

    @Override
    public float getY() {
        return this.y;
    }

    @Override
    public float getZ() {
        return this.z;
    }

    @Override
    public SoundInstance.Attenuation getAttenuation() {
        return this.attenuation;
    }

    @Override
    public boolean isRelative() {
        return this.relative;
    }
}
