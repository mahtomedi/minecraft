package net.minecraft.client.resources.sounds;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleSoundInstance extends AbstractSoundInstance {
    public SimpleSoundInstance(SoundEvent param0, SoundSource param1, float param2, float param3, RandomSource param4, BlockPos param5) {
        this(param0, param1, param2, param3, param4, (double)param5.getX() + 0.5, (double)param5.getY() + 0.5, (double)param5.getZ() + 0.5);
    }

    public static SimpleSoundInstance forUI(SoundEvent param0, float param1) {
        return forUI(param0, param1, 0.25F);
    }

    public static SimpleSoundInstance forUI(SoundEvent param0, float param1, float param2) {
        return new SimpleSoundInstance(
            param0.getLocation(),
            SoundSource.MASTER,
            param2,
            param1,
            SoundInstance.createUnseededRandom(),
            false,
            0,
            SoundInstance.Attenuation.NONE,
            0.0,
            0.0,
            0.0,
            true
        );
    }

    public static SimpleSoundInstance forMusic(SoundEvent param0) {
        return new SimpleSoundInstance(
            param0.getLocation(),
            SoundSource.MUSIC,
            1.0F,
            1.0F,
            SoundInstance.createUnseededRandom(),
            false,
            0,
            SoundInstance.Attenuation.NONE,
            0.0,
            0.0,
            0.0,
            true
        );
    }

    public static SimpleSoundInstance forRecord(SoundEvent param0, Vec3 param1) {
        return new SimpleSoundInstance(
            param0,
            SoundSource.RECORDS,
            4.0F,
            1.0F,
            SoundInstance.createUnseededRandom(),
            false,
            0,
            SoundInstance.Attenuation.LINEAR,
            param1.x,
            param1.y,
            param1.z
        );
    }

    public static SimpleSoundInstance forLocalAmbience(SoundEvent param0, float param1, float param2) {
        return new SimpleSoundInstance(
            param0.getLocation(),
            SoundSource.AMBIENT,
            param2,
            param1,
            SoundInstance.createUnseededRandom(),
            false,
            0,
            SoundInstance.Attenuation.NONE,
            0.0,
            0.0,
            0.0,
            true
        );
    }

    public static SimpleSoundInstance forAmbientAddition(SoundEvent param0) {
        return forLocalAmbience(param0, 1.0F, 1.0F);
    }

    public static SimpleSoundInstance forAmbientMood(SoundEvent param0, RandomSource param1, double param2, double param3, double param4) {
        return new SimpleSoundInstance(param0, SoundSource.AMBIENT, 1.0F, 1.0F, param1, false, 0, SoundInstance.Attenuation.LINEAR, param2, param3, param4);
    }

    public SimpleSoundInstance(
        SoundEvent param0, SoundSource param1, float param2, float param3, RandomSource param4, double param5, double param6, double param7
    ) {
        this(param0, param1, param2, param3, param4, false, 0, SoundInstance.Attenuation.LINEAR, param5, param6, param7);
    }

    private SimpleSoundInstance(
        SoundEvent param0,
        SoundSource param1,
        float param2,
        float param3,
        RandomSource param4,
        boolean param5,
        int param6,
        SoundInstance.Attenuation param7,
        double param8,
        double param9,
        double param10
    ) {
        this(param0.getLocation(), param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, false);
    }

    public SimpleSoundInstance(
        ResourceLocation param0,
        SoundSource param1,
        float param2,
        float param3,
        RandomSource param4,
        boolean param5,
        int param6,
        SoundInstance.Attenuation param7,
        double param8,
        double param9,
        double param10,
        boolean param11
    ) {
        super(param0, param1, param4);
        this.volume = param2;
        this.pitch = param3;
        this.x = param8;
        this.y = param9;
        this.z = param10;
        this.looping = param5;
        this.delay = param6;
        this.attenuation = param7;
        this.relative = param11;
    }
}
