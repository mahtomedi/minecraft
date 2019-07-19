package net.minecraft.world.level;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class ForcedChunksSavedData extends SavedData {
    private LongSet chunks = new LongOpenHashSet();

    public ForcedChunksSavedData() {
        super("chunks");
    }

    @Override
    public void load(CompoundTag param0) {
        this.chunks = new LongOpenHashSet(param0.getLongArray("Forced"));
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
