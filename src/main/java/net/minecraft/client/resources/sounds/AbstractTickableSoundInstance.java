package net.minecraft.client.resources.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractTickableSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
    private boolean stopped;

    protected AbstractTickableSoundInstance(SoundEvent param0, SoundSource param1) {
        super(param0, param1);
    }

    @Override
    public boolean isStopped() {
        return this.stopped;
    }

    protected final void stop() {
        this.stopped = true;
        this.looping = false;
    }
}
