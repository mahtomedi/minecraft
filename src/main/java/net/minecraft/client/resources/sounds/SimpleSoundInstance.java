package net.minecraft.client.resources.sounds;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleSoundInstance extends AbstractSoundInstance {
    public SimpleSoundInstance(SoundEvent param0, SoundSource param1, float param2, float param3, BlockPos param4) {
        this(param0, param1, param2, param3, (float)param4.getX() + 0.5F, (float)param4.getY() + 0.5F, (float)param4.getZ() + 0.5F);
    }

    public static SimpleSoundInstance forUI(SoundEvent param0, float param1) {
        return forUI(param0, param1, 0.25F);
    }

    public static SimpleSoundInstance forUI(SoundEvent param0, float param1, float param2) {
        return new SimpleSoundInstance(
            param0.getLocation(), SoundSource.MASTER, param2, param1, false, 0, SoundInstance.Attenuation.NONE, 0.0F, 0.0F, 0.0F, true
        );
    }

    public static SimpleSoundInstance forMusic(SoundEvent param0) {
        return new SimpleSoundInstance(param0.getLocation(), SoundSource.MUSIC, 1.0F, 1.0F, false, 0, SoundInstance.Attenuation.NONE, 0.0F, 0.0F, 0.0F, true);
    }

    public static SimpleSoundInstance forRecord(SoundEvent param0, float param1, float param2, float param3) {
        return new SimpleSoundInstance(param0, SoundSource.RECORDS, 4.0F, 1.0F, false, 0, SoundInstance.Attenuation.LINEAR, param1, param2, param3);
    }

    public static SimpleSoundInstance forLocalAmbience(SoundEvent param0, float param1, float param2) {
        return new SimpleSoundInstance(
            param0.getLocation(), SoundSource.AMBIENT, param2, param1, false, 0, SoundInstance.Attenuation.NONE, 0.0F, 0.0F, 0.0F, true
        );
    }

    public SimpleSoundInstance(SoundEvent param0, SoundSource param1, float param2, float param3, float param4, float param5, float param6) {
        this(param0, param1, param2, param3, false, 0, SoundInstance.Attenuation.LINEAR, param4, param5, param6);
    }

    private SimpleSoundInstance(
        SoundEvent param0,
        SoundSource param1,
        float param2,
        float param3,
        boolean param4,
        int param5,
        SoundInstance.Attenuation param6,
        float param7,
        float param8,
        float param9
    ) {
        this(param0.getLocation(), param1, param2, param3, param4, param5, param6, param7, param8, param9, false);
    }

    public SimpleSoundInstance(
        ResourceLocation param0,
        SoundSource param1,
        float param2,
        float param3,
        boolean param4,
        int param5,
        SoundInstance.Attenuation param6,
        float param7,
        float param8,
        float param9,
        boolean param10
    ) {
        super(param0, param1);
        this.volume = param2;
        this.pitch = param3;
        this.x = param7;
        this.y = param8;
        this.z = param9;
        this.looping = param4;
        this.delay = param5;
        this.attenuation = param6;
        this.relative = param10;
    }
}
