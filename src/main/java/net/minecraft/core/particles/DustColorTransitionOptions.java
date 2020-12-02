package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DustColorTransitionOptions extends DustParticleOptionsBase {
    public static final Vec3 SCULK_PARTICLE_COLOR = Vec3.fromRGB24(3790560);
    public static final DustColorTransitionOptions SCULK_TO_REDSTONE = new DustColorTransitionOptions(
        SCULK_PARTICLE_COLOR, DustParticleOptions.REDSTONE_PARTICLE_COLOR, 1.0F
    );
    public static final Codec<DustColorTransitionOptions> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Vec3.CODEC.fieldOf("fromColor").forGetter(param0x -> param0x.color),
                    Vec3.CODEC.fieldOf("toColor").forGetter(param0x -> param0x.toColor),
                    Codec.FLOAT.fieldOf("scale").forGetter(param0x -> param0x.scale)
                )
                .apply(param0, DustColorTransitionOptions::new)
    );
    public static final ParticleOptions.Deserializer<DustColorTransitionOptions> DESERIALIZER = new ParticleOptions.Deserializer<DustColorTransitionOptions>() {
        public DustColorTransitionOptions fromCommand(ParticleType<DustColorTransitionOptions> param0, StringReader param1) throws CommandSyntaxException {
            Vec3 var0 = DustParticleOptionsBase.readVec3(param1);
            param1.expect(' ');
            float var1 = (float)param1.readDouble();
            Vec3 var2 = DustParticleOptionsBase.readVec3(param1);
            return new DustColorTransitionOptions(var0, var2, var1);
        }

        public DustColorTransitionOptions fromNetwork(ParticleType<DustColorTransitionOptions> param0, FriendlyByteBuf param1) {
            return new DustColorTransitionOptions(
                new Vec3((double)param1.readFloat(), (double)param1.readFloat(), (double)param1.readFloat()),
                new Vec3((double)param1.readFloat(), (double)param1.readFloat(), (double)param1.readFloat()),
                param1.readFloat()
            );
        }
    };
    private final Vec3 toColor;

    public DustColorTransitionOptions(Vec3 param0, Vec3 param1, float param2) {
        super(param0, param2);
        this.toColor = param1;
    }

    @OnlyIn(Dist.CLIENT)
    public Vec3 getFromColor() {
        return this.color;
    }

    @OnlyIn(Dist.CLIENT)
    public Vec3 getToColor() {
        return this.toColor;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf param0) {
        super.writeToNetwork(param0);
        param0.writeDouble(this.color.x);
        param0.writeDouble(this.color.y);
        param0.writeDouble(this.color.z);
    }

    @Override
    public String writeToString() {
        return String.format(
            Locale.ROOT,
            "%s %.2f %.2f %.2f %.2f %.2f %.2f %.2f",
            Registry.PARTICLE_TYPE.getKey(this.getType()),
            this.color.x,
            this.color.y,
            this.color.z,
            this.scale,
            this.toColor.x,
            this.toColor.y,
            this.toColor.z
        );
    }

    @Override
    public ParticleType<DustColorTransitionOptions> getType() {
        return ParticleTypes.DUST_COLOR_TRANSITION;
    }
}
