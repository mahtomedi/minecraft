package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;

public class ProtoTickList<T> implements TickList<T> {
    protected final Predicate<T> ignore;
    private final ChunkPos chunkPos;
    private final ShortList[] toBeTicked;
    private LevelHeightAccessor levelHeightAccessor;

    public ProtoTickList(Predicate<T> param0, ChunkPos param1, LevelHeightAccessor param2) {
        this(param0, param1, new ListTag(), param2);
    }

    public ProtoTickList(Predicate<T> param0, ChunkPos param1, ListTag param2, LevelHeightAccessor param3) {
        this.ignore = param0;
        this.chunkPos = param1;
        this.levelHeightAccessor = param3;
        this.toBeTicked = new ShortList[param3.getSectionsCount()];

        for(int var0 = 0; var0 < param2.size(); ++var0) {
            ListTag var1 = param2.getList(var0);

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                ChunkAccess.getOrCreateOffsetList(this.toBeTicked, var0).add(var1.getShort(var2));
            }
        }

    }

    public ListTag save() {
        return ChunkSerializer.packOffsets(this.toBeTicked);
    }

    public void copyOut(TickList<T> param0, Function<BlockPos, T> param1) {
        for(int var0 = 0; var0 < this.toBeTicked.length; ++var0) {
            if (this.toBeTicked[var0] != null) {
                for(Short var1 : this.toBeTicked[var0]) {
                    BlockPos var2 = ProtoChunk.unpackOffsetCoordinates(var1, this.levelHeightAccessor.getSectionYFromSectionIndex(var0), this.chunkPos);
                    param0.scheduleTick(var2, param1.apply(var2), 0);
                }

                this.toBeTicked[var0].clear();
            }
        }

    }

    @Override
    public boolean hasScheduledTick(BlockPos param0, T param1) {
        return false;
    }

    @Override
    public void scheduleTick(BlockPos param0, T param1, int param2, TickPriority param3) {
        ChunkAccess.getOrCreateOffsetList(this.toBeTicked, this.levelHeightAccessor.getSectionIndex(param0.getY()))
            .add(ProtoChunk.packOffsetCoordinates(param0));
    }

    @Override
    public boolean willTickThisTick(BlockPos param0, T param1) {
        return false;
    }
}
