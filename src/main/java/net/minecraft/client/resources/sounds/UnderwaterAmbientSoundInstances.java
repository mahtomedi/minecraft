package net.minecraft.client.resources.sounds;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UnderwaterAmbientSoundInstances {
    @OnlyIn(Dist.CLIENT)
    public static class SubSound extends AbstractTickableSoundInstance {
        private final LocalPlayer player;

        protected SubSound(LocalPlayer param0, SoundEvent param1) {
            super(param1, SoundSource.AMBIENT);
            this.player = param0;
            this.looping = false;
            this.delay = 0;
            this.volume = 1.0F;
            this.priority = true;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (this.player.removed || !this.player.isUnderWater()) {
                this.stopped = true;
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class UnderwaterAmbientSoundInstance extends AbstractTickableSoundInstance {
        private final LocalPlayer player;
        private int fade;

        public UnderwaterAmbientSoundInstance(LocalPlayer param0) {
            super(SoundEvents.AMBIENT_UNDERWATER_LOOP, SoundSource.AMBIENT);
            this.player = param0;
            this.looping = true;
            this.delay = 0;
            this.volume = 1.0F;
            this.priority = true;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (!this.player.removed && this.fade >= 0) {
                if (this.player.isUnderWater()) {
                    ++this.fade;
                } else {
                    this.fade -= 2;
                }

                this.fade = Math.min(this.fade, 40);
                this.volume = Math.max(0.0F, Math.min((float)this.fade / 40.0F, 1.0F));
            } else {
                this.stopped = true;
            }
        }
    }
}
