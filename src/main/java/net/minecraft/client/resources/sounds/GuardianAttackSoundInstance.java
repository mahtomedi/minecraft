package net.minecraft.client.resources.sounds;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuardianAttackSoundInstance extends AbstractTickableSoundInstance {
    private final Guardian guardian;

    public GuardianAttackSoundInstance(Guardian param0) {
        super(SoundEvents.GUARDIAN_ATTACK, SoundSource.HOSTILE);
        this.guardian = param0;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.looping = true;
        this.delay = 0;
    }

    @Override
    public void tick() {
        if (!this.guardian.removed && this.guardian.getTarget() == null) {
            this.x = (float)this.guardian.getX();
            this.y = (float)this.guardian.getY();
            this.z = (float)this.guardian.getZ();
            float var0 = this.guardian.getAttackAnimationScale(0.0F);
            this.volume = 0.0F + 1.0F * var0 * var0;
            this.pitch = 0.7F + 0.5F * var0;
        } else {
            this.stop();
        }
    }
}
