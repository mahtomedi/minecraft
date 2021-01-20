package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DustColorTransitionOptions extends DustParticleOptionsBase {
    public static final Vector3f SCULK_PARTICLE_COLOR = new Vector3f(Vec3.fromRGB24(3790560));
    public static final DustColorTransitionOptions SCULK_TO_REDSTONE = new DustColorTransitionOptions(
        SCULK_PARTICLE_COLOR, DustParticleOptions.REDSTONE_PARTICLE_COLOR, 1.0F
    );
    public static final Codec<DustColorTransitionOptions> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Vector3f.CODEC.fieldOf("fromColor").forGetter(param0x -> param0x.color),
                    Vector3f.CODEC.fieldOf("toColor").forGetter(param0x -> param0x.toColor),
                    Codec.FLOAT.fieldOf("scale").forGetter(param0x -> param0x.scale)
                )
                .apply(param0, DustColorTransitionOptions::new)
    );
    public static final ParticleOptions.Deserializer<DustColorTransitionOptions> DESERIALIZER = new ParticleOptions.Deserializer<DustColorTransitionOptions>() {
        public DustColorTransitionOptions fromCommand(ParticleType<DustColorTransitionOptions> param0, StringReader param1) throws CommandSyntaxException {
            Vector3f var0 = DustParticleOptionsBase.readVector3f(param1);
            param1.expect(' ');
            float var1 = param1.readFloat();
            Vector3f var2 = DustParticleOptionsBase.readVector3f(param1);
            return new DustColorTransitionOptions(var0, var2, var1);
        }

        public DustColorTransitionOptions fromNetwork(ParticleType<DustColorTransitionOptions> param0, FriendlyByteBuf param1) {
            Vector3f var0 = DustParticleOptionsBase.readVector3f(param1);
            float var1 = param1.readFloat();
            Vector3f var2 = DustParticleOptionsBase.readVector3f(param1);
            return new DustColorTransitionOptions(var0, var2, var1);
        }
    };
    private final Vector3f toColor;

    public DustColorTransitionOptions(Vector3f param0, Vector3f param1, float param2) {
        super(param0, param2);
        this.toColor = param1;
    }

    @OnlyIn(Dist.CLIENT)
    public Vector3f getFromColor() {
        return this.color;
    }

    @OnlyIn(Dist.CLIENT)
    public Vector3f getToColor() {
        return this.toColor;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf param0) {
        super.writeToNetwork(param0);
        param0.writeFloat(this.toColor.x());
        param0.writeFloat(this.toColor.y());
        param0.writeFloat(this.toColor.z());
    }

    @Override
    public String writeToString() {
        return String.format(
            Locale.ROOT,
            "%s %.2f %.2f %.2f %.2f %.2f %.2f %.2f",
            Registry.PARTICLE_TYPE.getKey(this.getType()),
            this.color.x(),
            this.color.y(),
            this.color.z(),
            this.scale,
            this.toColor.x(),
            this.toColor.y(),
            this.toColor.z()
        );
    }

    @Override
    public ParticleType<DustColorTransitionOptions> getType() {
        return ParticleTypes.DUST_COLOR_TRANSITION;
    }
}
