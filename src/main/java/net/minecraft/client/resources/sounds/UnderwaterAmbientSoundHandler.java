package net.minecraft.client.resources.sounds;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UnderwaterAmbientSoundHandler implements AmbientSoundHandler {
    private final LocalPlayer player;
    private final SoundManager soundManager;
    private int tick_delay = 0;

    public UnderwaterAmbientSoundHandler(LocalPlayer param0, SoundManager param1) {
        this.player = param0;
        this.soundManager = param1;
    }

    @Override
    public void tick() {
        --this.tick_delay;
        if (this.tick_delay <= 0 && this.player.isUnderWater()) {
            float var0 = this.player.level.random.nextFloat();
            if (var0 < 1.0E-4F) {
                this.tick_delay = 0;
                this.soundManager.play(new UnderwaterAmbientSoundInstances.SubSound(this.player, SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE));
            } else if (var0 < 0.001F) {
                this.tick_delay = 0;
                this.soundManager.play(new UnderwaterAmbientSoundInstances.SubSound(this.player, SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_RARE));
            } else if (var0 < 0.01F) {
                this.tick_delay = 0;
                this.soundManager.play(new UnderwaterAmbientSoundInstances.SubSound(this.player, SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS));
            }
        }

    }
}
