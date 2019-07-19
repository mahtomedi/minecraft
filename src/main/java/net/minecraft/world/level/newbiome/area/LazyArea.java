package net.minecraft.world.level.newbiome.area;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.newbiome.layer.traits.PixelTransformer;

public final class LazyArea implements Area {
    private final PixelTransformer transformer;
    private final Long2IntLinkedOpenHashMap cache;
    private final int maxCache;

    public LazyArea(Long2IntLinkedOpenHashMap param0, int param1, PixelTransformer param2) {
        this.cache = param0;
        this.maxCache = param1;
        this.transformer = param2;
    }

    @Override
    public int get(int param0, int param1) {
        long var0 = ChunkPos.asLong(param0, param1);
        synchronized(this.cache) {
            int var1 = this.cache.get(var0);
            if (var1 != Integer.MIN_VALUE) {
                return var1;
            } else {
                int var2 = this.transformer.apply(param0, param1);
                this.cache.put(var0, var2);
                if (this.cache.size() > this.maxCache) {
                    for(int var3 = 0; var3 < this.maxCache / 16; ++var3) {
                        this.cache.removeFirstInt();
                    }
                }

                return var2;
            }
        }
    }

    public int getMaxCache() {
        return this.maxCache;
    }
}
