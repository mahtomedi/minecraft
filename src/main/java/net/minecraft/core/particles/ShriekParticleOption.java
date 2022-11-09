package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public class ShriekParticleOption implements ParticleOptions {
    public static final Codec<ShriekParticleOption> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(Codec.INT.fieldOf("delay").forGetter(param0x -> param0x.delay)).apply(param0, ShriekParticleOption::new)
    );
    public static final ParticleOptions.Deserializer<ShriekParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<ShriekParticleOption>() {
        public ShriekParticleOption fromCommand(ParticleType<ShriekParticleOption> param0, StringReader param1) throws CommandSyntaxException {
            param1.expect(' ');
            int var0 = param1.readInt();
            return new ShriekParticleOption(var0);
        }

        public ShriekParticleOption fromNetwork(ParticleType<ShriekParticleOption> param0, FriendlyByteBuf param1) {
            return new ShriekParticleOption(param1.readVarInt());
        }
    };
    private final int delay;

    public ShriekParticleOption(int param0) {
        this.delay = param0;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf param0) {
        param0.writeVarInt(this.delay);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %d", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.delay);
    }

    @Override
    public ParticleType<ShriekParticleOption> getType() {
        return ParticleTypes.SHRIEK;
    }

    public int getDelay() {
        return this.delay;
    }
}
