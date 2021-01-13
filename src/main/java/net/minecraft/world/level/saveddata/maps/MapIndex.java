package net.minecraft.world.level.saveddata.maps;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class MapIndex extends SavedData {
    private final Object2IntMap<String> usedAuxIds = new Object2IntOpenHashMap<>();

    public MapIndex() {
        super("idcounts");
        this.usedAuxIds.defaultReturnValue(-1);
    }

    @Override
    public void load(CompoundTag param0) {
        this.usedAuxIds.clear();

        for(String var0 : param0.getAllKeys()) {
            if (param0.contains(var0, 99)) {
                this.usedAuxIds.put(var0, param0.getInt(var0));
            }
        }

    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        for(Entry<String> var0 : this.usedAuxIds.object2IntEntrySet()) {
            param0.putInt(var0.getKey(), var0.getIntValue());
        }

        return param0;
    }

    public int getFreeAuxValueForMap() {
        int var0 = this.usedAuxIds.getInt("map") + 1;
        this.usedAuxIds.put("map", var0);
        this.setDirty();
        return var0;
    }
}
