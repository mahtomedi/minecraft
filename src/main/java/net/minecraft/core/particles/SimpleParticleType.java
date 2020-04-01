package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;

public class SimpleParticleType extends ParticleType<SimpleParticleType> implements ParticleOptions {
    private static final ParticleOptions.Deserializer<SimpleParticleType> DESERIALIZER = new ParticleOptions.Deserializer<SimpleParticleType>() {
        public SimpleParticleType fromCommand(ParticleType<SimpleParticleType> param0, StringReader param1) throws CommandSyntaxException {
            return (SimpleParticleType)param0;
        }

        public SimpleParticleType fromNetwork(ParticleType<SimpleParticleType> param0, FriendlyByteBuf param1) {
            return (SimpleParticleType)param0;
        }
    };

    protected SimpleParticleType(boolean param0) {
        super(param0, DESERIALIZER, (param0x, param1) -> (SimpleParticleType)param1);
    }

    @Override
    public ParticleType<SimpleParticleType> getType() {
        return this;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf param0) {
    }

    @Override
    public String writeToString() {
        return Registry.PARTICLE_TYPE.getKey(this).toString();
    }
}
