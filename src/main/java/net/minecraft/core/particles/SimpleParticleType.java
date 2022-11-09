package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public class SimpleParticleType extends ParticleType<SimpleParticleType> implements ParticleOptions {
    private static final ParticleOptions.Deserializer<SimpleParticleType> DESERIALIZER = new ParticleOptions.Deserializer<SimpleParticleType>() {
        public SimpleParticleType fromCommand(ParticleType<SimpleParticleType> param0, StringReader param1) {
            return (SimpleParticleType)param0;
        }

        public SimpleParticleType fromNetwork(ParticleType<SimpleParticleType> param0, FriendlyByteBuf param1) {
            return (SimpleParticleType)param0;
        }
    };
    private final Codec<SimpleParticleType> codec = Codec.unit(this::getType);

    protected SimpleParticleType(boolean param0) {
        super(param0, DESERIALIZER);
    }

    public SimpleParticleType getType() {
        return this;
    }

    @Override
    public Codec<SimpleParticleType> codec() {
        return this.codec;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf param0) {
    }

    @Override
    public String writeToString() {
        return BuiltInRegistries.PARTICLE_TYPE.getKey(this).toString();
    }
}
