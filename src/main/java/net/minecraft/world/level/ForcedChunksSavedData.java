package net.minecraft.world.level;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class ForcedChunksSavedData extends SavedData {
    public static final String FILE_ID = "chunks";
    private static final String TAG_FORCED = "Forced";
    private final LongSet chunks;

    public static SavedData.Factory<ForcedChunksSavedData> factory() {
        return new SavedData.Factory<>(ForcedChunksSavedData::new, ForcedChunksSavedData::load, DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
    }

    private ForcedChunksSavedData(LongSet param0) {
        this.chunks = param0;
    }

    public ForcedChunksSavedData() {
        this(new LongOpenHashSet());
    }

    public static ForcedChunksSavedData load(CompoundTag param0) {
        return new ForcedChunksSavedData(new LongOpenHashSet(param0.getLongArray("Forced")));
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        param0.putLongArray("Forced", this.chunks.toLongArray());
        return param0;
    }

    public LongSet getChunks() {
        return this.chunks;
    }
}
