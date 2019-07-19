package net.minecraft.client.resources.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractTickableSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
    protected boolean stopped;

    protected AbstractTickableSoundInstance(SoundEvent param0, SoundSource param1) {
        super(param0, param1);
    }

    @Override
    public boolean isStopped() {
        return this.stopped;
    }
}
