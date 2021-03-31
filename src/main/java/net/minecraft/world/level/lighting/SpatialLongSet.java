package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.util.NoSuchElementException;
import net.minecraft.util.Mth;

public class SpatialLongSet extends LongLinkedOpenHashSet {
    private final SpatialLongSet.InternalMap map;

    public SpatialLongSet(int param0, float param1) {
        super(param0, param1);
        this.map = new SpatialLongSet.InternalMap(param0 / 64, param1);
    }

    @Override
    public boolean add(long param0) {
        return this.map.addBit(param0);
    }

    @Override
    public boolean rem(long param0) {
        return this.map.removeBit(param0);
    }

    @Override
    public long removeFirstLong() {
        return this.map.removeFirstBit();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public static class InternalMap extends Long2LongLinkedOpenHashMap {
        private static final int X_BITS = Mth.log2(60000000);
        private static final int Z_BITS = Mth.log2(60000000);
        private static final int Y_BITS = 64 - X_BITS - Z_BITS;
        private static final int Y_OFFSET = 0;
        private static final int Z_OFFSET = Y_BITS;
        private static final int X_OFFSET = Y_BITS + Z_BITS;
        private static final long OUTER_MASK = 3L << X_OFFSET | 3L | 3L << Z_OFFSET;
        private int lastPos = -1;
        private long lastOuterKey;
        private final int minSize;

        public InternalMap(int param0, float param1) {
            super(param0, param1);
            this.minSize = param0;
        }

        static long getOuterKey(long param0) {
            return param0 & ~OUTER_MASK;
        }

        static int getInnerKey(long param0) {
            int var0 = (int)(param0 >>> X_OFFSET & 3L);
            int var1 = (int)(param0 >>> 0 & 3L);
            int var2 = (int)(param0 >>> Z_OFFSET & 3L);
            return var0 << 4 | var2 << 2 | var1;
        }

        static long getFullKey(long param0, int param1) {
            param0 |= (long)(param1 >>> 4 & 3) << X_OFFSET;
            param0 |= (long)(param1 >>> 2 & 3) << Z_OFFSET;
            return param0 | (long)(param1 >>> 0 & 3) << 0;
        }

        public boolean addBit(long param0) {
            long var0 = getOuterKey(param0);
            int var1 = getInnerKey(param0);
            long var2 = 1L << var1;
            int var3;
            if (var0 == 0L) {
                if (this.containsNullKey) {
                    return this.replaceBit(this.n, var2);
                }

                this.containsNullKey = true;
                var3 = this.n;
            } else {
                if (this.lastPos != -1 && var0 == this.lastOuterKey) {
                    return this.replaceBit(this.lastPos, var2);
                }

                long[] var4 = this.key;
                var3 = (int)HashCommon.mix(var0) & this.mask;

                for(long var6 = var4[var3]; var6 != 0L; var6 = var4[var3]) {
                    if (var6 == var0) {
                        this.lastPos = var3;
                        this.lastOuterKey = var0;
                        return this.replaceBit(var3, var2);
                    }

                    var3 = var3 + 1 & this.mask;
                }
            }

            this.key[var3] = var0;
            this.value[var3] = var2;
            if (this.size == 0) {
                this.first = this.last = var3;
                this.link[var3] = -1L;
            } else {
                this.link[this.last] ^= (this.link[this.last] ^ (long)var3 & 4294967295L) & 4294967295L;
                this.link[var3] = ((long)this.last & 4294967295L) << 32 | 4294967295L;
                this.last = var3;
            }

            if (this.size++ >= this.maxFill) {
                this.rehash(HashCommon.arraySize(this.size + 1, this.f));
            }

            return false;
        }

        private boolean replaceBit(int param0, long param1) {
            boolean var0 = (this.value[param0] & param1) != 0L;
            this.value[param0] |= param1;
            return var0;
        }

        public boolean removeBit(long param0) {
            long var0 = getOuterKey(param0);
            int var1 = getInnerKey(param0);
            long var2 = 1L << var1;
            if (var0 == 0L) {
                return this.containsNullKey ? this.removeFromNullEntry(var2) : false;
            } else if (this.lastPos != -1 && var0 == this.lastOuterKey) {
                return this.removeFromEntry(this.lastPos, var2);
            } else {
                long[] var3 = this.key;
                int var4 = (int)HashCommon.mix(var0) & this.mask;

                for(long var5 = var3[var4]; var5 != 0L; var5 = var3[var4]) {
                    if (var0 == var5) {
                        this.lastPos = var4;
                        this.lastOuterKey = var0;
                        return this.removeFromEntry(var4, var2);
                    }

                    var4 = var4 + 1 & this.mask;
                }

                return false;
            }
        }

        private boolean removeFromNullEntry(long param0) {
            if ((this.value[this.n] & param0) == 0L) {
                return false;
            } else {
                this.value[this.n] &= ~param0;
                if (this.value[this.n] != 0L) {
                    return true;
                } else {
                    this.containsNullKey = false;
                    --this.size;
                    this.fixPointers(this.n);
                    if (this.size < this.maxFill / 4 && this.n > 16) {
                        this.rehash(this.n / 2);
                    }

                    return true;
                }
            }
        }

        private boolean removeFromEntry(int param0, long param1) {
            if ((this.value[param0] & param1) == 0L) {
                return false;
            } else {
                this.value[param0] &= ~param1;
                if (this.value[param0] != 0L) {
                    return true;
                } else {
                    this.lastPos = -1;
                    --this.size;
                    this.fixPointers(param0);
                    this.shiftKeys(param0);
                    if (this.size < this.maxFill / 4 && this.n > 16) {
                        this.rehash(this.n / 2);
                    }

                    return true;
                }
            }
        }

        public long removeFirstBit() {
            if (this.size == 0) {
                throw new NoSuchElementException();
            } else {
                int var0 = this.first;
                long var1 = this.key[var0];
                int var2 = Long.numberOfTrailingZeros(this.value[var0]);
                this.value[var0] &= ~(1L << var2);
                if (this.value[var0] == 0L) {
                    this.removeFirstLong();
                    this.lastPos = -1;
                }

                return getFullKey(var1, var2);
            }
        }

        @Override
        protected void rehash(int param0) {
            if (param0 > this.minSize) {
                super.rehash(param0);
            }

        }
    }
}
