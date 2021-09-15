package net.minecraft.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import java.util.Arrays;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;

public class CrudeIncrementalIntIdentityHashBiMap<K> implements IdMap<K> {
    public static final int NOT_FOUND = -1;
    private static final Object EMPTY_SLOT = null;
    private static final float LOADFACTOR = 0.8F;
    private K[] keys;
    private int[] values;
    private K[] byId;
    private int nextId;
    private int size;

    private CrudeIncrementalIntIdentityHashBiMap(int param0) {
        this.keys = (K[])(new Object[param0]);
        this.values = new int[param0];
        this.byId = (K[])(new Object[param0]);
    }

    public static <A> CrudeIncrementalIntIdentityHashBiMap<A> create(int param0) {
        return new CrudeIncrementalIntIdentityHashBiMap<>((int)((float)param0 / 0.8F));
    }

    @Override
    public int getId(@Nullable K param0) {
        return this.getValue(this.indexOf(param0, this.hash(param0)));
    }

    @Nullable
    @Override
    public K byId(int param0) {
        return param0 >= 0 && param0 < this.byId.length ? this.byId[param0] : null;
    }

    private int getValue(int param0) {
        return param0 == -1 ? -1 : this.values[param0];
    }

    public boolean contains(K param0) {
        return this.getId(param0) != -1;
    }

    public boolean contains(int param0) {
        return this.byId(param0) != null;
    }

    public int add(K param0) {
        int var0 = this.nextId();
        this.addMapping(param0, var0);
        return var0;
    }

    private int nextId() {
        while(this.nextId < this.byId.length && this.byId[this.nextId] != null) {
            ++this.nextId;
        }

        return this.nextId;
    }

    private void grow(int param0) {
        K[] var0 = this.keys;
        int[] var1 = this.values;
        CrudeIncrementalIntIdentityHashBiMap<K> var2 = new CrudeIncrementalIntIdentityHashBiMap<>(param0);

        for(int var3 = 0; var3 < var0.length; ++var3) {
            if (var0[var3] != null) {
                var2.addMapping(var0[var3], var1[var3]);
            }
        }

        this.keys = var2.keys;
        this.values = var2.values;
        this.byId = var2.byId;
        this.nextId = var2.nextId;
        this.size = var2.size;
    }

    public void addMapping(K param0, int param1) {
        int var0 = Math.max(param1, this.size + 1);
        if ((float)var0 >= (float)this.keys.length * 0.8F) {
            int var1 = this.keys.length << 1;

            while(var1 < param1) {
                var1 <<= 1;
            }

            this.grow(var1);
        }

        int var2 = this.findEmpty(this.hash(param0));
        this.keys[var2] = param0;
        this.values[var2] = param1;
        this.byId[param1] = param0;
        ++this.size;
        if (param1 == this.nextId) {
            ++this.nextId;
        }

    }

    private int hash(@Nullable K param0) {
        return (Mth.murmurHash3Mixer(System.identityHashCode(param0)) & 2147483647) % this.keys.length;
    }

    private int indexOf(@Nullable K param0, int param1) {
        for(int var0 = param1; var0 < this.keys.length; ++var0) {
            if (this.keys[var0] == param0) {
                return var0;
            }

            if (this.keys[var0] == EMPTY_SLOT) {
                return -1;
            }
        }

        for(int var1 = 0; var1 < param1; ++var1) {
            if (this.keys[var1] == param0) {
                return var1;
            }

            if (this.keys[var1] == EMPTY_SLOT) {
                return -1;
            }
        }

        return -1;
    }

    private int findEmpty(int param0) {
        for(int var0 = param0; var0 < this.keys.length; ++var0) {
            if (this.keys[var0] == EMPTY_SLOT) {
                return var0;
            }
        }

        for(int var1 = 0; var1 < param0; ++var1) {
            if (this.keys[var1] == EMPTY_SLOT) {
                return var1;
            }
        }

        throw new RuntimeException("Overflowed :(");
    }

    @Override
    public Iterator<K> iterator() {
        return Iterators.filter(Iterators.forArray(this.byId), Predicates.notNull());
    }

    public void clear() {
        Arrays.fill(this.keys, null);
        Arrays.fill(this.byId, null);
        this.nextId = 0;
        this.size = 0;
    }

    @Override
    public int size() {
        return this.size;
    }
}
