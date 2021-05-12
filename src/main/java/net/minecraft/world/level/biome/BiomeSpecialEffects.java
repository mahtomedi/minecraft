package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringRepresentable;

public class BiomeSpecialEffects {
    public static final Codec<BiomeSpecialEffects> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.INT.fieldOf("fog_color").forGetter(param0x -> param0x.fogColor),
                    Codec.INT.fieldOf("water_color").forGetter(param0x -> param0x.waterColor),
                    Codec.INT.fieldOf("water_fog_color").forGetter(param0x -> param0x.waterFogColor),
                    Codec.INT.fieldOf("sky_color").forGetter(param0x -> param0x.skyColor),
                    Codec.INT.optionalFieldOf("foliage_color").forGetter(param0x -> param0x.foliageColorOverride),
                    Codec.INT.optionalFieldOf("grass_color").forGetter(param0x -> param0x.grassColorOverride),
                    BiomeSpecialEffects.GrassColorModifier.CODEC
                        .optionalFieldOf("grass_color_modifier", BiomeSpecialEffects.GrassColorModifier.NONE)
                        .forGetter(param0x -> param0x.grassColorModifier),
                    AmbientParticleSettings.CODEC.optionalFieldOf("particle").forGetter(param0x -> param0x.ambientParticleSettings),
                    SoundEvent.CODEC.optionalFieldOf("ambient_sound").forGetter(param0x -> param0x.ambientLoopSoundEvent),
                    AmbientMoodSettings.CODEC.optionalFieldOf("mood_sound").forGetter(param0x -> param0x.ambientMoodSettings),
                    AmbientAdditionsSettings.CODEC.optionalFieldOf("additions_sound").forGetter(param0x -> param0x.ambientAdditionsSettings),
                    Music.CODEC.optionalFieldOf("music").forGetter(param0x -> param0x.backgroundMusic)
                )
                .apply(param0, BiomeSpecialEffects::new)
    );
    private final int fogColor;
    private final int waterColor;
    private final int waterFogColor;
    private final int skyColor;
    private final Optional<Integer> foliageColorOverride;
    private final Optional<Integer> grassColorOverride;
    private final BiomeSpecialEffects.GrassColorModifier grassColorModifier;
    private final Optional<AmbientParticleSettings> ambientParticleSettings;
    private final Optional<SoundEvent> ambientLoopSoundEvent;
    private final Optional<AmbientMoodSettings> ambientMoodSettings;
    private final Optional<AmbientAdditionsSettings> ambientAdditionsSettings;
    private final Optional<Music> backgroundMusic;

    BiomeSpecialEffects(
        int param0,
        int param1,
        int param2,
        int param3,
        Optional<Integer> param4,
        Optional<Integer> param5,
        BiomeSpecialEffects.GrassColorModifier param6,
        Optional<AmbientParticleSettings> param7,
        Optional<SoundEvent> param8,
        Optional<AmbientMoodSettings> param9,
        Optional<AmbientAdditionsSettings> param10,
        Optional<Music> param11
    ) {
        this.fogColor = param0;
        this.waterColor = param1;
        this.waterFogColor = param2;
        this.skyColor = param3;
        this.foliageColorOverride = param4;
        this.grassColorOverride = param5;
        this.grassColorModifier = param6;
        this.ambientParticleSettings = param7;
        this.ambientLoopSoundEvent = param8;
        this.ambientMoodSettings = param9;
        this.ambientAdditionsSettings = param10;
        this.backgroundMusic = param11;
    }

    public int getFogColor() {
        return this.fogColor;
    }

    public int getWaterColor() {
        return this.waterColor;
    }

    public int getWaterFogColor() {
        return this.waterFogColor;
    }

    public int getSkyColor() {
        return this.skyColor;
    }

    public Optional<Integer> getFoliageColorOverride() {
        return this.foliageColorOverride;
    }

    public Optional<Integer> getGrassColorOverride() {
        return this.grassColorOverride;
    }

    public BiomeSpecialEffects.GrassColorModifier getGrassColorModifier() {
        return this.grassColorModifier;
    }

    public Optional<AmbientParticleSettings> getAmbientParticleSettings() {
        return this.ambientParticleSettings;
    }

    public Optional<SoundEvent> getAmbientLoopSoundEvent() {
        return this.ambientLoopSoundEvent;
    }

    public Optional<AmbientMoodSettings> getAmbientMoodSettings() {
        return this.ambientMoodSettings;
    }

    public Optional<AmbientAdditionsSettings> getAmbientAdditionsSettings() {
        return this.ambientAdditionsSettings;
    }

    public Optional<Music> getBackgroundMusic() {
        return this.backgroundMusic;
    }

    public static class Builder {
        private OptionalInt fogColor = OptionalInt.empty();
        private OptionalInt waterColor = OptionalInt.empty();
        private OptionalInt waterFogColor = OptionalInt.empty();
        private OptionalInt skyColor = OptionalInt.empty();
        private Optional<Integer> foliageColorOverride = Optional.empty();
        private Optional<Integer> grassColorOverride = Optional.empty();
        private BiomeSpecialEffects.GrassColorModifier grassColorModifier = BiomeSpecialEffects.GrassColorModifier.NONE;
        private Optional<AmbientParticleSettings> ambientParticle = Optional.empty();
        private Optional<SoundEvent> ambientLoopSoundEvent = Optional.empty();
        private Optional<AmbientMoodSettings> ambientMoodSettings = Optional.empty();
        private Optional<AmbientAdditionsSettings> ambientAdditionsSettings = Optional.empty();
        private Optional<Music> backgroundMusic = Optional.empty();

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

        public BiomeSpecialEffects.Builder skyColor(int param0) {
            this.skyColor = OptionalInt.of(param0);
            return this;
        }

        public BiomeSpecialEffects.Builder foliageColorOverride(int param0) {
            this.foliageColorOverride = Optional.of(param0);
            return this;
        }

        public BiomeSpecialEffects.Builder grassColorOverride(int param0) {
            this.grassColorOverride = Optional.of(param0);
            return this;
        }

        public BiomeSpecialEffects.Builder grassColorModifier(BiomeSpecialEffects.GrassColorModifier param0) {
            this.grassColorModifier = param0;
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

        public BiomeSpecialEffects.Builder backgroundMusic(Music param0) {
            this.backgroundMusic = Optional.of(param0);
            return this;
        }

        public BiomeSpecialEffects build() {
            return new BiomeSpecialEffects(
                this.fogColor.orElseThrow(() -> new IllegalStateException("Missing 'fog' color.")),
                this.waterColor.orElseThrow(() -> new IllegalStateException("Missing 'water' color.")),
                this.waterFogColor.orElseThrow(() -> new IllegalStateException("Missing 'water fog' color.")),
                this.skyColor.orElseThrow(() -> new IllegalStateException("Missing 'sky' color.")),
                this.foliageColorOverride,
                this.grassColorOverride,
                this.grassColorModifier,
                this.ambientParticle,
                this.ambientLoopSoundEvent,
                this.ambientMoodSettings,
                this.ambientAdditionsSettings,
                this.backgroundMusic
            );
        }
    }

    public static enum GrassColorModifier implements StringRepresentable {
        NONE("none") {
            @Override
            public int modifyColor(double param0, double param1, int param2) {
                return param2;
            }
        },
        DARK_FOREST("dark_forest") {
            @Override
            public int modifyColor(double param0, double param1, int param2) {
                return (param2 & 16711422) + 2634762 >> 1;
            }
        },
        SWAMP("swamp") {
            @Override
            public int modifyColor(double param0, double param1, int param2) {
                double var0 = Biome.BIOME_INFO_NOISE.getValue(param0 * 0.0225, param1 * 0.0225, false);
                return var0 < -0.1 ? 5011004 : 6975545;
            }
        };

        private final String name;
        public static final Codec<BiomeSpecialEffects.GrassColorModifier> CODEC = StringRepresentable.fromEnum(
            BiomeSpecialEffects.GrassColorModifier::values, BiomeSpecialEffects.GrassColorModifier::byName
        );
        private static final Map<String, BiomeSpecialEffects.GrassColorModifier> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(BiomeSpecialEffects.GrassColorModifier::getName, param0 -> param0));

        public abstract int modifyColor(double var1, double var3, int var5);

        GrassColorModifier(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static BiomeSpecialEffects.GrassColorModifier byName(String param0) {
            return BY_NAME.get(param0);
        }
    }
}
