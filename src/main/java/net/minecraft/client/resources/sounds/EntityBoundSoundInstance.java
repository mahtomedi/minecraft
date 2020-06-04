package net.minecraft.client.resources.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityBoundSoundInstance extends AbstractTickableSoundInstance {
    private final Entity entity;

    public EntityBoundSoundInstance(SoundEvent param0, SoundSource param1, Entity param2) {
        this(param0, param1, 1.0F, 1.0F, param2);
    }

    public EntityBoundSoundInstance(SoundEvent param0, SoundSource param1, float param2, float param3, Entity param4) {
        super(param0, param1);
        this.volume = param2;
        this.pitch = param3;
        this.entity = param4;
        this.x = (double)((float)this.entity.getX());
        this.y = (double)((float)this.entity.getY());
        this.z = (double)((float)this.entity.getZ());
    }

    @Override
    public boolean canPlaySound() {
        return !this.entity.isSilent();
    }

    @Override
    public void tick() {
        if (this.entity.removed) {
            this.stop();
        } else {
            this.x = (double)((float)this.entity.getX());
            this.y = (double)((float)this.entity.getY());
            this.z = (double)((float)this.entity.getZ());
        }
    }
}
