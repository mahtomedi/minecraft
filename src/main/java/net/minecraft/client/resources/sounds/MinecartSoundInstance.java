package net.minecraft.client.resources.sounds;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MinecartSoundInstance extends AbstractTickableSoundInstance {
    private static final float VOLUME_MIN = 0.0F;
    private static final float VOLUME_MAX = 0.7F;
    private static final float PITCH_MIN = 0.0F;
    private static final float PITCH_MAX = 1.0F;
    private static final float PITCH_DELTA = 0.0025F;
    private final AbstractMinecart minecart;
    private float pitch = 0.0F;

    public MinecartSoundInstance(AbstractMinecart param0) {
        super(SoundEvents.MINECART_RIDING, SoundSource.NEUTRAL);
        this.minecart = param0;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.x = (double)((float)param0.getX());
        this.y = (double)((float)param0.getY());
        this.z = (double)((float)param0.getZ());
    }

    @Override
    public boolean canPlaySound() {
        return !this.minecart.isSilent();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (this.minecart.isRemoved()) {
            this.stop();
        } else {
            this.x = (double)((float)this.minecart.getX());
            this.y = (double)((float)this.minecart.getY());
            this.z = (double)((float)this.minecart.getZ());
            float var0 = Mth.sqrt(Entity.getHorizontalDistanceSqr(this.minecart.getDeltaMovement()));
            if ((double)var0 >= 0.01) {
                this.pitch = Mth.clamp(this.pitch + 0.0025F, 0.0F, 1.0F);
                this.volume = Mth.lerp(Mth.clamp(var0, 0.0F, 0.5F), 0.0F, 0.7F);
            } else {
                this.pitch = 0.0F;
                this.volume = 0.0F;
            }

        }
    }
}
