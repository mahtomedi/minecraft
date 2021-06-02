package net.minecraft.world.level.chunk;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.util.DebugBuffer;
import net.minecraft.util.Mth;
import net.minecraft.util.ThreadingDetector;

public class PalettedContainer<T> implements PaletteResize<T> {
    private static final int SIZE = 4096;
    public static final int GLOBAL_PALETTE_BITS = 9;
    public static final int MIN_PALETTE_SIZE = 4;
    private final Palette<T> globalPalette;
    private final PaletteResize<T> dummyPaletteResize = (param0x, param1x) -> 0;
    private final IdMapper<T> registry;
    private final Function<CompoundTag, T> reader;
    private final Function<T, CompoundTag> writer;
    private final T defaultValue;
    protected BitStorage storage;
    private Palette<T> palette;
    private int bits;
    private final Semaphore lock = new Semaphore(1);
    @Nullable
    private final DebugBuffer<Pair<Thread, StackTraceElement[]>> traces = null;

    public void acquire() {
        if (this.traces != null) {
            Thread var0 = Thread.currentThread();
            this.traces.push(Pair.of(var0, var0.getStackTrace()));
        }

        ThreadingDetector.checkAndLock(this.lock, this.traces, "PalettedContainer");
    }

    public void release() {
        this.lock.release();
    }

    public PalettedContainer(Palette<T> param0, IdMapper<T> param1, Function<CompoundTag, T> param2, Function<T, CompoundTag> param3, T param4) {
        this.globalPalette = param0;
        this.registry = param1;
        this.reader = param2;
        this.writer = param3;
        this.defaultValue = param4;
        this.setBits(4);
    }

    private static int getIndex(int param0, int param1, int param2) {
        return param1 << 8 | param2 << 4 | param0;
    }

    private void setBits(int param0) {
        if (param0 != this.bits) {
            this.bits = param0;
            if (this.bits <= 4) {
                this.bits = 4;
                this.palette = new LinearPalette<>(this.registry, this.bits, this, this.reader);
            } else if (this.bits < 9) {
                this.palette = new HashMapPalette<>(this.registry, this.bits, this, this.reader, this.writer);
            } else {
                this.palette = this.globalPalette;
                this.bits = Mth.ceillog2(this.registry.size());
            }

            this.palette.idFor(this.defaultValue);
            this.storage = new BitStorage(this.bits, 4096);
        }
    }

    @Override
    public int onResize(int param0, T param1) {
        BitStorage var0 = this.storage;
        Palette<T> var1 = this.palette;
        this.setBits(param0);

        for(int var2 = 0; var2 < var0.getSize(); ++var2) {
            T var3 = var1.valueFor(var0.get(var2));
            if (var3 != null) {
                this.set(var2, var3);
            }
        }

        return this.palette.idFor(param1);
    }

    public T getAndSet(int param0, int param1, int param2, T param3) {
        Object var6;
        try {
            this.acquire();
            T var0 = this.getAndSet(getIndex(param0, param1, param2), param3);
            var6 = var0;
        } finally {
            this.release();
        }

        return (T)var6;
    }

    public T getAndSetUnchecked(int param0, int param1, int param2, T param3) {
        return this.getAndSet(getIndex(param0, param1, param2), param3);
    }

    private T getAndSet(int param0, T param1) {
        int var0 = this.palette.idFor(param1);
        int var1 = this.storage.getAndSet(param0, var0);
        T var2 = this.palette.valueFor(var1);
        return (T)(var2 == null ? this.defaultValue : var2);
    }

    public void set(int param0, int param1, int param2, T param3) {
        try {
            this.acquire();
            this.set(getIndex(param0, param1, param2), param3);
        } finally {
            this.release();
        }

    }

    private void set(int param0, T param1) {
        int var0 = this.palette.idFor(param1);
        this.storage.set(param0, var0);
    }

    public T get(int param0, int param1, int param2) {
        return this.get(getIndex(param0, param1, param2));
    }

    protected T get(int param0) {
        T var0 = this.palette.valueFor(this.storage.get(param0));
        return (T)(var0 == null ? this.defaultValue : var0);
    }

    public void read(FriendlyByteBuf param0) {
        try {
            this.acquire();
            int var0 = param0.readByte();
            if (this.bits != var0) {
                this.setBits(var0);
            }

            this.palette.read(param0);
            param0.readLongArray(this.storage.getRaw());
        } finally {
            this.release();
        }

    }

    public void write(FriendlyByteBuf param0) {
        try {
            this.acquire();
            param0.writeByte(this.bits);
            this.palette.write(param0);
            param0.writeLongArray(this.storage.getRaw());
        } finally {
            this.release();
        }

    }

    public void read(ListTag param0, long[] param1) {
        try {
            this.acquire();
            int var0 = Math.max(4, Mth.ceillog2(param0.size()));
            if (var0 != this.bits) {
                this.setBits(var0);
            }

            this.palette.read(param0);
            int var1 = param1.length * 64 / 4096;
            if (this.palette == this.globalPalette) {
                Palette<T> var2 = new HashMapPalette<>(this.registry, var0, this.dummyPaletteResize, this.reader, this.writer);
                var2.read(param0);
                BitStorage var3 = new BitStorage(var0, 4096, param1);

                for(int var4 = 0; var4 < 4096; ++var4) {
                    this.storage.set(var4, this.globalPalette.idFor(var2.valueFor(var3.get(var4))));
                }
            } else if (var1 == this.bits) {
                System.arraycopy(param1, 0, this.storage.getRaw(), 0, param1.length);
            } else {
                BitStorage var5 = new BitStorage(var1, 4096, param1);

                for(int var6 = 0; var6 < 4096; ++var6) {
                    this.storage.set(var6, var5.get(var6));
                }
            }
        } finally {
            this.release();
        }

    }

    public void write(CompoundTag param0, String param1, String param2) {
        try {
            this.acquire();
            HashMapPalette<T> var0 = new HashMapPalette<>(this.registry, this.bits, this.dummyPaletteResize, this.reader, this.writer);
            T var1 = this.defaultValue;
            int var2 = var0.idFor(this.defaultValue);
            int[] var3 = new int[4096];

            for(int var4 = 0; var4 < 4096; ++var4) {
                T var5 = this.get(var4);
                if (var5 != var1) {
                    var1 = var5;
                    var2 = var0.idFor(var5);
                }

                var3[var4] = var2;
            }

            ListTag var6 = new ListTag();
            var0.write(var6);
            param0.put(param1, var6);
            int var7 = Math.max(4, Mth.ceillog2(var6.size()));
            BitStorage var8 = new BitStorage(var7, 4096);

            for(int var9 = 0; var9 < var3.length; ++var9) {
                var8.set(var9, var3[var9]);
            }

            param0.putLongArray(param2, var8.getRaw());
        } finally {
            this.release();
        }

    }

    public int getSerializedSize() {
        return 1 + this.palette.getSerializedSize() + FriendlyByteBuf.getVarIntSize(this.storage.getSize()) + this.storage.getRaw().length * 8;
    }

    public boolean maybeHas(Predicate<T> param0) {
        return this.palette.maybeHas(param0);
    }

    public void count(PalettedContainer.CountConsumer<T> param0) {
        Int2IntMap var0 = new Int2IntOpenHashMap();
        this.storage.getAll(param1 -> var0.put(param1, var0.get(param1) + 1));
        var0.int2IntEntrySet().forEach(param1 -> param0.accept(this.palette.valueFor(param1.getIntKey()), param1.getIntValue()));
    }

    @FunctionalInterface
    public interface CountConsumer<T> {
        void accept(T var1, int var2);
    }
}
