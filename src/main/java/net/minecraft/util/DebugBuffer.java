package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class DebugBuffer<T> {
    private final AtomicReferenceArray<T> data;
    private final AtomicInteger index;

    public DebugBuffer(int param0) {
        this.data = new AtomicReferenceArray<>(param0);
        this.index = new AtomicInteger(0);
    }

    public void push(T param0) {
        int var0 = this.data.length();

        int var1;
        int var2;
        do {
            var1 = this.index.get();
            var2 = (var1 + 1) % var0;
        } while(!this.index.compareAndSet(var1, var2));

        this.data.set(var2, param0);
    }

    public List<T> dump() {
        int var0 = this.index.get();
        Builder<T> var1 = ImmutableList.builder();

        for(int var2 = 0; var2 < this.data.length(); ++var2) {
            int var3 = Math.floorMod(var0 - var2, this.data.length());
            T var4 = this.data.get(var3);
            if (var4 != null) {
                var1.add(var4);
            }
        }

        return var1.build();
    }
}
