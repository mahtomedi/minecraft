package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class DustParticleOptions extends DustParticleOptionsBase {
    public static final Vec3 REDSTONE_PARTICLE_COLOR = Vec3.fromRGB24(16711680);
    public static final DustParticleOptions REDSTONE = new DustParticleOptions(REDSTONE_PARTICLE_COLOR, 1.0F);
    public static final Codec<DustParticleOptions> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Vec3.CODEC.fieldOf("color").forGetter(param0x -> param0x.color), Codec.FLOAT.fieldOf("scale").forGetter(param0x -> param0x.scale)
                )
                .apply(param0, DustParticleOptions::new)
    );
    public static final ParticleOptions.Deserializer<DustParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<DustParticleOptions>() {
        public DustParticleOptions fromCommand(ParticleType<DustParticleOptions> param0, StringReader param1) throws CommandSyntaxException {
            Vec3 var0 = DustParticleOptionsBase.readVec3(param1);
            param1.expect(' ');
            float var1 = (float)param1.readDouble();
            return new DustParticleOptions(var0, var1);
        }

        public DustParticleOptions fromNetwork(ParticleType<DustParticleOptions> param0, FriendlyByteBuf param1) {
            return new DustParticleOptions(new Vec3((double)param1.readFloat(), (double)param1.readFloat(), (double)param1.readFloat()), param1.readFloat());
        }
    };

    public DustParticleOptions(Vec3 param0, float param1) {
        super(param0, param1);
    }

    @Override
    public ParticleType<DustParticleOptions> getType() {
        return ParticleTypes.DUST;
    }
}
