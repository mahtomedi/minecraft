package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VibrationPath {
    public static final Codec<VibrationPath> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockPos.CODEC.fieldOf("origin").forGetter(param0x -> param0x.origin),
                    PositionSource.CODEC.fieldOf("destination").forGetter(param0x -> param0x.destination),
                    Codec.INT.fieldOf("arrival_in_ticks").forGetter(param0x -> param0x.arrivalInTicks)
                )
                .apply(param0, VibrationPath::new)
    );
    private final BlockPos origin;
    private final PositionSource destination;
    private final int arrivalInTicks;

    public VibrationPath(BlockPos param0, PositionSource param1, int param2) {
        this.origin = param0;
        this.destination = param1;
        this.arrivalInTicks = param2;
    }

    public int getArrivalInTicks() {
        return this.arrivalInTicks;
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    @OnlyIn(Dist.CLIENT)
    public PositionSource getDestination() {
        return this.destination;
    }

    public static VibrationPath read(FriendlyByteBuf param0) {
        BlockPos var0 = param0.readBlockPos();
        PositionSource var1 = PositionSourceType.fromNetwork(param0);
        int var2 = param0.readVarInt();
        return new VibrationPath(var0, var1, var2);
    }

    public static void write(FriendlyByteBuf param0, VibrationPath param1) {
        param0.writeBlockPos(param1.origin);
        PositionSourceType.toNetwork(param1.destination, param0);
        param0.writeVarInt(param1.arrivalInTicks);
    }
}
