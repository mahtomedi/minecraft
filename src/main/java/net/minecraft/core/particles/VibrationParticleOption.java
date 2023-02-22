package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.phys.Vec3;

public class VibrationParticleOption implements ParticleOptions {
    public static final Codec<VibrationParticleOption> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    PositionSource.CODEC.fieldOf("destination").forGetter(param0x -> param0x.destination),
                    Codec.INT.fieldOf("arrival_in_ticks").forGetter(param0x -> param0x.arrivalInTicks)
                )
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
            int var3 = param1.readInt();
            BlockPos var4 = BlockPos.containing((double)var0, (double)var1, (double)var2);
            return new VibrationParticleOption(new BlockPositionSource(var4), var3);
        }

        public VibrationParticleOption fromNetwork(ParticleType<VibrationParticleOption> param0, FriendlyByteBuf param1) {
            PositionSource var0 = PositionSourceType.fromNetwork(param1);
            int var1 = param1.readVarInt();
            return new VibrationParticleOption(var0, var1);
        }
    };
    private final PositionSource destination;
    private final int arrivalInTicks;

    public VibrationParticleOption(PositionSource param0, int param1) {
        this.destination = param0;
        this.arrivalInTicks = param1;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf param0) {
        PositionSourceType.toNetwork(this.destination, param0);
        param0.writeVarInt(this.arrivalInTicks);
    }

    @Override
    public String writeToString() {
        Vec3 var0 = this.destination.getPosition(null).get();
        double var1 = var0.x();
        double var2 = var0.y();
        double var3 = var0.z();
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %d", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), var1, var2, var3, this.arrivalInTicks);
    }

    @Override
    public ParticleType<VibrationParticleOption> getType() {
        return ParticleTypes.VIBRATION;
    }

    public PositionSource getDestination() {
        return this.destination;
    }

    public int getArrivalInTicks() {
        return this.arrivalInTicks;
    }
}
