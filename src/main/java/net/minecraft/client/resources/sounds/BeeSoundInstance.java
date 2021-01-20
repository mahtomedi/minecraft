package net.minecraft.client.resources.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class BeeSoundInstance extends AbstractTickableSoundInstance {
    protected final Bee bee;
    private boolean hasSwitched;

    public BeeSoundInstance(Bee param0, SoundEvent param1, SoundSource param2) {
        super(param1, param2);
        this.bee = param0;
        this.x = (double)((float)param0.getX());
        this.y = (double)((float)param0.getY());
        this.z = (double)((float)param0.getZ());
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
    }

    @Override
    public void tick() {
        boolean var0 = this.shouldSwitchSounds();
        if (var0 && !this.isStopped()) {
            Minecraft.getInstance().getSoundManager().queueTickingSound(this.getAlternativeSoundInstance());
            this.hasSwitched = true;
        }

        if (!this.bee.isRemoved() && !this.hasSwitched) {
            this.x = (double)((float)this.bee.getX());
            this.y = (double)((float)this.bee.getY());
            this.z = (double)((float)this.bee.getZ());
            float var1 = Mth.sqrt(Entity.getHorizontalDistanceSqr(this.bee.getDeltaMovement()));
            if ((double)var1 >= 0.01) {
                this.pitch = Mth.lerp(Mth.clamp(var1, this.getMinPitch(), this.getMaxPitch()), this.getMinPitch(), this.getMaxPitch());
                this.volume = Mth.lerp(Mth.clamp(var1, 0.0F, 0.5F), 0.0F, 1.2F);
            } else {
                this.pitch = 0.0F;
                this.volume = 0.0F;
            }

        } else {
            this.stop();
        }
    }

    private float getMinPitch() {
        return this.bee.isBaby() ? 1.1F : 0.7F;
    }

    private float getMaxPitch() {
        return this.bee.isBaby() ? 1.5F : 1.1F;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public boolean canPlaySound() {
        return !this.bee.isSilent();
    }

    protected abstract AbstractTickableSoundInstance getAlternativeSoundInstance();

    protected abstract boolean shouldSwitchSounds();
}
