package net.minecraft.world.level.chunk.storage;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.BitSet;

public class RegionBitmap {
    private final BitSet used = new BitSet();

    public void force(int param0, int param1) {
        this.used.set(param0, param0 + param1);
    }

    public void free(int param0, int param1) {
        this.used.clear(param0, param0 + param1);
    }

    public int allocate(int param0) {
        int var0 = 0;

        while(true) {
            int var1 = this.used.nextClearBit(var0);
            int var2 = this.used.nextSetBit(var1);
            if (var2 == -1 || var2 - var1 >= param0) {
                this.force(var1, param0);
                return var1;
            }

            var0 = var2;
        }
    }

    @VisibleForTesting
    public IntSet getUsed() {
        return this.used.stream().collect(IntArraySet::new, IntCollection::add, IntCollection::addAll);
    }
}
