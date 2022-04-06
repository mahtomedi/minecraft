package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BlockPositionSource implements PositionSource {
    public static final Codec<BlockPositionSource> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(BlockPos.CODEC.fieldOf("pos").forGetter(param0x -> param0x.pos)).apply(param0, BlockPositionSource::new)
    );
    final BlockPos pos;

    public BlockPositionSource(BlockPos param0) {
        this.pos = param0;
    }

    @Override
    public Optional<Vec3> getPosition(Level param0) {
        return Optional.of(Vec3.atCenterOf(this.pos));
    }

    @Override
    public PositionSourceType<?> getType() {
        return PositionSourceType.BLOCK;
    }

    public static class Type implements PositionSourceType<BlockPositionSource> {
        public BlockPositionSource read(FriendlyByteBuf param0) {
            return new BlockPositionSource(param0.readBlockPos());
        }

        public void write(FriendlyByteBuf param0, BlockPositionSource param1) {
            param0.writeBlockPos(param1.pos);
        }

        @Override
        public Codec<BlockPositionSource> codec() {
            return BlockPositionSource.CODEC;
        }
    }
}
