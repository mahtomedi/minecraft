package net.minecraft.world.level.levelgen.structure;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class StructureFeatureIndexSavedData extends SavedData {
    private final LongSet all;
    private final LongSet remaining;

    private StructureFeatureIndexSavedData(LongSet param0, LongSet param1) {
        this.all = param0;
        this.remaining = param1;
    }

    public StructureFeatureIndexSavedData() {
        this(new LongOpenHashSet(), new LongOpenHashSet());
    }

    public static StructureFeatureIndexSavedData load(CompoundTag param0) {
        return new StructureFeatureIndexSavedData(new LongOpenHashSet(param0.getLongArray("All")), new LongOpenHashSet(param0.getLongArray("Remaining")));
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        param0.putLongArray("All", this.all.toLongArray());
        param0.putLongArray("Remaining", this.remaining.toLongArray());
        return param0;
    }

    public void addIndex(long param0) {
        this.all.add(param0);
        this.remaining.add(param0);
    }

    public boolean hasStartIndex(long param0) {
        return this.all.contains(param0);
    }

    public boolean hasUnhandledIndex(long param0) {
        return this.remaining.contains(param0);
    }

    public void removeIndex(long param0) {
        this.remaining.remove(param0);
    }

    public LongSet getAll() {
        return this.all;
    }
}
