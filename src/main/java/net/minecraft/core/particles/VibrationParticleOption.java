package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VibrationParticleOption implements ParticleOptions {
    public static final Codec<VibrationParticleOption> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(VibrationPath.CODEC.fieldOf("vibration").forGetter(param0x -> param0x.vibrationPath))
                .apply(param0, VibrationParticleOption::new)
    );
    public static final ParticleOptions.Deserializer<VibrationParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<VibrationParticleOption>() {
        public VibrationParticleOption fromCommand(ParticleType<VibrationParticleOption> param0, StringReader param1) throws CommandSyntaxException {
            param1.expect(' ');
            float var0 = (float)param1.readDouble();
            param1.expect(' ');
            float var1 = (float)param1.readDouble();
            param1.expect(' ');
            float var2 = (float)param1.readDouble();
            param1.expect(' ');
            float var3 = (float)param1.readDouble();
            param1.expect(' ');
            float var4 = (float)param1.readDouble();
            param1.expect(' ');
            float var5 = (float)param1.readDouble();
            param1.expect(' ');
            int var6 = param1.readInt();
            BlockPos var7 = new BlockPos((double)var0, (double)var1, (double)var2);
            BlockPos var8 = new BlockPos((double)var3, (double)var4, (double)var5);
            return new VibrationParticleOption(new VibrationPath(var7, new BlockPositionSource(var8), var6));
        }

        public VibrationParticleOption fromNetwork(ParticleType<VibrationParticleOption> param0, FriendlyByteBuf param1) {
            VibrationPath var0 = VibrationPath.read(param1);
            return new VibrationParticleOption(var0);
        }
    };
    private final VibrationPath vibrationPath;

    public VibrationParticleOption(VibrationPath param0) {
        this.vibrationPath = param0;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf param0) {
        VibrationPath.write(param0, this.vibrationPath);
    }

    @Override
    public String writeToString() {
        BlockPos var0 = this.vibrationPath.getOrigin();
        double var1 = (double)var0.getX();
        double var2 = (double)var0.getY();
        double var3 = (double)var0.getZ();
        return String.format(
            Locale.ROOT,
            "%s %.2f %.2f %.2f %.2f %.2f %.2f %d",
            Registry.PARTICLE_TYPE.getKey(this.getType()),
            var1,
            var2,
            var3,
            var1,
            var2,
            var3,
            this.vibrationPath.getArrivalInTicks()
        );
    }

    @Override
    public ParticleType<VibrationParticleOption> getType() {
        return ParticleTypes.VIBRATION;
    }

    @OnlyIn(Dist.CLIENT)
    public VibrationPath getVibrationPath() {
        return this.vibrationPath;
    }
}
