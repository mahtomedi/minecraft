package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.BitSet;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.ChunkStatus;

public final class BelowZeroRetrogen {
    private static final BitSet EMPTY = new BitSet(0);
    private static final Codec<BitSet> BITSET_CODEC = Codec.LONG_STREAM
        .xmap(param0 -> BitSet.valueOf(param0.toArray()), param0 -> LongStream.of(param0.toLongArray()));
    private static final Codec<ChunkStatus> NON_EMPTY_CHUNK_STATUS = Registry.CHUNK_STATUS
        .comapFlatMap(
            param0 -> param0 == ChunkStatus.EMPTY ? DataResult.error("target_status cannot be empty") : DataResult.success(param0), Function.identity()
        );
    public static final Codec<BelowZeroRetrogen> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    NON_EMPTY_CHUNK_STATUS.fieldOf("target_status").forGetter(BelowZeroRetrogen::targetStatus),
                    BITSET_CODEC.optionalFieldOf("missing_bedrock")
                        .forGetter(param0x -> param0x.missingBedrock.isEmpty() ? Optional.empty() : Optional.of(param0x.missingBedrock))
                )
                .apply(param0, BelowZeroRetrogen::new)
    );
    private final ChunkStatus targetStatus;
    private final BitSet missingBedrock;

    private BelowZeroRetrogen(ChunkStatus param0, Optional<BitSet> param1) {
        this.targetStatus = param0;
        this.missingBedrock = param1.orElse(EMPTY);
    }

    @Nullable
    public static BelowZeroRetrogen read(CompoundTag param0) {
        ChunkStatus var0 = ChunkStatus.byName(param0.getString("target_status"));
        return var0 == ChunkStatus.EMPTY ? null : new BelowZeroRetrogen(var0, Optional.of(BitSet.valueOf(param0.getLongArray("missing_bedrock"))));
    }

    public ChunkStatus targetStatus() {
        return this.targetStatus;
    }

    public boolean hasBedrockAt(int param0, int param1) {
        return !this.missingBedrock.get((param1 & 15) * 16 + (param0 & 15));
    }
}
