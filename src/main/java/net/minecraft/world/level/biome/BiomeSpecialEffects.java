package net.minecraft.world.level.biome;

import java.util.Optional;
import java.util.OptionalInt;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BiomeSpecialEffects {
    private final int fogColor;
    private final int waterColor;
    private final int waterFogColor;
    private final Optional<AmbientParticleSettings> ambientParticleSettings;

    private BiomeSpecialEffects(int param0, int param1, int param2, Optional<AmbientParticleSettings> param3) {
        this.fogColor = param0;
        this.waterColor = param1;
        this.waterFogColor = param2;
        this.ambientParticleSettings = param3;
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

    public static class Builder {
        private OptionalInt fogColor = OptionalInt.empty();
        private OptionalInt waterColor = OptionalInt.empty();
        private OptionalInt waterFogColor = OptionalInt.empty();
        private Optional<AmbientParticleSettings> ambientParticle = Optional.empty();

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

        public BiomeSpecialEffects build() {
            return new BiomeSpecialEffects(
                this.fogColor.orElseThrow(() -> new IllegalStateException("Missing 'fog' color.")),
                this.waterColor.orElseThrow(() -> new IllegalStateException("Missing 'water' color.")),
                this.waterFogColor.orElseThrow(() -> new IllegalStateException("Missing 'water fog' color.")),
                this.ambientParticle
            );
        }
    }
}
