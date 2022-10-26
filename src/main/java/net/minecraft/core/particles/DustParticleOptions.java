package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class DustParticleOptions extends DustParticleOptionsBase {
    public static final Vector3f REDSTONE_PARTICLE_COLOR = Vec3.fromRGB24(16711680).toVector3f();
    public static final DustParticleOptions REDSTONE = new DustParticleOptions(REDSTONE_PARTICLE_COLOR, 1.0F);
    public static final Codec<DustParticleOptions> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(param0x -> param0x.color), Codec.FLOAT.fieldOf("scale").forGetter(param0x -> param0x.scale)
                )
                .apply(param0, DustParticleOptions::new)
    );
    public static final ParticleOptions.Deserializer<DustParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<DustParticleOptions>() {
        public DustParticleOptions fromCommand(ParticleType<DustParticleOptions> param0, StringReader param1) throws CommandSyntaxException {
            Vector3f var0 = DustParticleOptionsBase.readVector3f(param1);
            param1.expect(' ');
            float var1 = param1.readFloat();
            return new DustParticleOptions(var0, var1);
        }

        public DustParticleOptions fromNetwork(ParticleType<DustParticleOptions> param0, FriendlyByteBuf param1) {
            return new DustParticleOptions(DustParticleOptionsBase.readVector3f(param1), param1.readFloat());
        }
    };

    public DustParticleOptions(Vector3f param0, float param1) {
        super(param0, param1);
    }

    @Override
    public ParticleType<DustParticleOptions> getType() {
        return ParticleTypes.DUST;
    }
}
