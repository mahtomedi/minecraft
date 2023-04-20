package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.world.level.chunk.DataLayer;

public abstract class DataLayerStorageMap<M extends DataLayerStorageMap<M>> {
    private static final int CACHE_SIZE = 2;
    private final long[] lastSectionKeys = new long[2];
    private final DataLayer[] lastSections = new DataLayer[2];
    private boolean cacheEnabled;
    protected final Long2ObjectOpenHashMap<DataLayer> map;

    protected DataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> param0) {
        this.map = param0;
        this.clearCache();
        this.cacheEnabled = true;
    }

    public abstract M copy();

    public DataLayer copyDataLayer(long param0) {
        DataLayer var0 = this.map.get(param0).copy();
        this.map.put(param0, var0);
        this.clearCache();
        return var0;
    }

    public boolean hasLayer(long param0) {
        return this.map.containsKey(param0);
    }

    @Nullable
    public DataLayer getLayer(long param0) {
        if (this.cacheEnabled) {
            for(int var0 = 0; var0 < 2; ++var0) {
                if (param0 == this.lastSectionKeys[var0]) {
                    return this.lastSections[var0];
                }
            }
        }

        DataLayer var1 = this.map.get(param0);
        if (var1 == null) {
            return null;
        } else {
            if (this.cacheEnabled) {
                for(int var2 = 1; var2 > 0; --var2) {
                    this.lastSectionKeys[var2] = this.lastSectionKeys[var2 - 1];
                    this.lastSections[var2] = this.lastSections[var2 - 1];
                }

                this.lastSectionKeys[0] = param0;
                this.lastSections[0] = var1;
            }

            return var1;
        }
    }

    @Nullable
    public DataLayer removeLayer(long param0) {
        return this.map.remove(param0);
    }

    public void setLayer(long param0, DataLayer param1) {
        this.map.put(param0, param1);
    }

    public void clearCache() {
        for(int var0 = 0; var0 < 2; ++var0) {
            this.lastSectionKeys[var0] = Long.MAX_VALUE;
            this.lastSections[var0] = null;
        }

    }

    public void disableCache() {
        this.cacheEnabled = false;
    }
}
