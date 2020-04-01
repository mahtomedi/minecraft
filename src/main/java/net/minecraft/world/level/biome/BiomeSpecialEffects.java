package net.minecraft.world.level.biome;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BiomeSpecialEffects {
    private final int fogColor;
    private final int waterColor;
    private final int waterFogColor;
    private final Optional<AmbientParticleSettings> ambientParticleSettings;
    private final Optional<SoundEvent> ambientLoopSoundEvent;
    private final Optional<AmbientMoodSettings> ambientMoodSettings;
    private final Optional<AmbientAdditionsSettings> ambientAdditionsSettings;

    private BiomeSpecialEffects(
        int param0,
        int param1,
        int param2,
        Optional<AmbientParticleSettings> param3,
        Optional<SoundEvent> param4,
        Optional<AmbientMoodSettings> param5,
        Optional<AmbientAdditionsSettings> param6
    ) {
        this.fogColor = param0;
        this.waterColor = param1;
        this.waterFogColor = param2;
        this.ambientParticleSettings = param3;
        this.ambientLoopSoundEvent = param4;
        this.ambientMoodSettings = param5;
        this.ambientAdditionsSettings = param6;
    }

    @OnlyIn(Dist.CLIENT)
    public int getFogColor() {
        return this.fogColor;
    }

    @OnlyIn(Dist.CLIENT)
    public int getWaterColor() {
        return this.waterColor;
    }

    @OnlyIn(Dist.CLIENT)
    public int getWaterFogColor() {
        return this.waterFogColor;
    }

    @OnlyIn(Dist.CLIENT)
    public Optional<AmbientParticleSettings> getAmbientParticleSettings() {
        return this.ambientParticleSettings;
    }

    @OnlyIn(Dist.CLIENT)
    public Optional<SoundEvent> getAmbientLoopSoundEvent() {
        return this.ambientLoopSoundEvent;
    }

    @OnlyIn(Dist.CLIENT)
    public Optional<AmbientMoodSettings> getAmbientMoodSettings() {
        return this.ambientMoodSettings;
    }

    @OnlyIn(Dist.CLIENT)
    public Optional<AmbientAdditionsSettings> getAmbientAdditionsSettings() {
        return this.ambientAdditionsSettings;
    }

    public static BiomeSpecialEffects random(Random param0) {
        BiomeSpecialEffects.Builder var0 = new BiomeSpecialEffects.Builder()
            .fogColor(param0.nextInt())
            .waterColor(param0.nextInt())
            .waterFogColor(param0.nextInt());
        if (param0.nextInt(5) == 0) {
            var0.ambientParticle(AmbientParticleSettings.random(param0));
        }

        if (param0.nextInt(10) == 0) {
            var0.ambientAdditionsSound(new AmbientAdditionsSettings(Registry.SOUND_EVENT.getRandom(param0), (double)(param0.nextFloat() / 3.0F)));
        }

        if (param0.nextInt(10) == 0) {
            var0.ambientAdditionsSound(new AmbientAdditionsSettings(Registry.SOUND_EVENT.getRandom(param0), (double)(param0.nextFloat() / 2.0F)));
        }

        return var0.build();
    }

    public static class Builder {
        private OptionalInt fogColor = OptionalInt.empty();
        private OptionalInt waterColor = OptionalInt.empty();
        private OptionalInt waterFogColor = OptionalInt.empty();
        private Optional<AmbientParticleSettings> ambientParticle = Optional.empty();
        private Optional<SoundEvent> ambientLoopSoundEvent = Optional.empty();
        private Optional<AmbientMoodSettings> ambientMoodSettings = Optional.empty();
        private Optional<AmbientAdditionsSettings> ambientAdditionsSettings = Optional.empty();

        public BiomeSpecialEffects.Builder fogColor(int param0) {
            this.fogColor = OptionalInt.of(param0);
            return this;
        }

        public BiomeSpecialEffects.Builder waterColor(int param0) {
            this.waterColor = OptionalInt.of(param0);
            return this;
        }

        public BiomeSpecialEffects.Builder waterFogColor(int param0) {
            this.waterFogColor = OptionalInt.of(param0);
            return this;
        }

        public BiomeSpecialEffects.Builder ambientParticle(AmbientParticleSettings param0) {
            this.ambientParticle = Optional.of(param0);
            return this;
        }

        public BiomeSpecialEffects.Builder ambientLoopSound(SoundEvent param0) {
            this.ambientLoopSoundEvent = Optional.of(param0);
            return this;
        }

        public BiomeSpecialEffects.Builder ambientMoodSound(AmbientMoodSettings param0) {
            this.ambientMoodSettings = Optional.of(param0);
            return this;
        }

        public BiomeSpecialEffects.Builder ambientAdditionsSound(AmbientAdditionsSettings param0) {
            this.ambientAdditionsSettings = Optional.of(param0);
            return this;
        }

        public BiomeSpecialEffects build() {
            return new BiomeSpecialEffects(
                this.fogColor.orElseThrow(() -> new IllegalStateException("Missing 'fog' color.")),
                this.waterColor.orElseThrow(() -> new IllegalStateException("Missing 'water' color.")),
                this.waterFogColor.orElseThrow(() -> new IllegalStateException("Missing 'water fog' color.")),
                this.ambientParticle,
                this.ambientLoopSoundEvent,
                this.ambientMoodSettings,
                this.ambientAdditionsSettings
            );
        }
    }
}
