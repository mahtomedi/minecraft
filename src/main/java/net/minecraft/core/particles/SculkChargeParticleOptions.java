package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public record SculkChargeParticleOptions(float roll) implements ParticleOptions {
    public static final Codec<SculkChargeParticleOptions> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(Codec.FLOAT.fieldOf("roll").forGetter(param0x -> param0x.roll)).apply(param0, SculkChargeParticleOptions::new)
    );
    public static final ParticleOptions.Deserializer<SculkChargeParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<SculkChargeParticleOptions>() {
        public SculkChargeParticleOptions fromCommand(ParticleType<SculkChargeParticleOptions> param0, StringReader param1) throws CommandSyntaxException {
            param1.expect(' ');
            float var0 = param1.readFloat();
            return new SculkChargeParticleOptions(var0);
        }

        public SculkChargeParticleOptions fromNetwork(ParticleType<SculkChargeParticleOptions> param0, FriendlyByteBuf param1) {
            return new SculkChargeParticleOptions(param1.readFloat());
        }
    };

    @Override
    public ParticleType<SculkChargeParticleOptions> getType() {
        return ParticleTypes.SCULK_CHARGE;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf param0) {
        param0.writeFloat(this.roll);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.roll);
    }
}
