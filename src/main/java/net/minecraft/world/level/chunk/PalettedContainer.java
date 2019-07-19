package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PalettedContainer<T> implements PaletteResize<T> {
    private final Palette<T> globalPalette;
    private final PaletteResize<T> dummyPaletteResize = (param0x, param1x) -> 0;
    private final IdMapper<T> registry;
    private final Function<CompoundTag, T> reader;
    private final Function<T, CompoundTag> writer;
    private final T defaultValue;
    protected BitStorage storage;
    private Palette<T> palette;
    private int bits;
    private final ReentrantLock lock = new ReentrantLock();

    public void acquire() {
        if (this.lock.isLocked() && !this.lock.isHeldByCurrentThread()) {
            String var0 = Thread.getAllStackTraces()
                .keySet()
                .stream()
                .filter(Objects::nonNull)
                .map(
                    param0 -> param0.getName()
                            + ": \n\tat "
                            + (String)Arrays.stream(param0.getStackTrace()).map(Object::toString).collect(Collectors.joining("\n\tat "))
                )
                .collect(Collectors.joining("\n"));
            CrashReport var1 = new CrashReport("Writing into PalettedContainer from multiple threads", new IllegalStateException());
            CrashReportCategory var2 = var1.addCategory("Thread dumps");
            var2.setDetail("Thread dumps", var0);
            throw new ReportedException(var1);
        } else {
            this.lock.lock();
        }
    }

    public void release() {
        this.lock.unlock();
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
        this.acquire();
        BitStorage var0 = this.storage;
        Palette<T> var1 = this.palette;
        this.setBits(param0);

        for(int var2 = 0; var2 < var0.getSize(); ++var2) {
            T var3 = var1.valueFor(var0.get(var2));
            if (var3 != null) {
                this.set(var2, var3);
            }
        }

        int var4 = this.palette.idFor(param1);
        this.release();
        return var4;
    }

    public T getAndSet(int param0, int param1, int param2, T param3) {
        this.acquire();
        T var0 = this.getAndSet(getIndex(param0, param1, param2), param3);
        this.release();
        return var0;
    }

    public T getAndSetUnchecked(int param0, int param1, int param2, T param3) {
        return this.getAndSet(getIndex(param0, param1, param2), param3);
    }

    protected T getAndSet(int param0, T param1) {
        int var0 = this.palette.idFor(param1);
        int var1 = this.storage.getAndSet(param0, var0);
        T var2 = this.palette.valueFor(var1);
        return (T)(var2 == null ? this.defaultValue : var2);
    }

    protected void set(int param0, T param1) {
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

    @OnlyIn(Dist.CLIENT)
    public void read(FriendlyByteBuf param0) {
        this.acquire();
        int var0 = param0.readByte();
        if (this.bits != var0) {
            this.setBits(var0);
        }

        this.palette.read(param0);
        param0.readLongArray(this.storage.getRaw());
        this.release();
    }

    public void write(FriendlyByteBuf param0) {
        this.acquire();
        param0.writeByte(this.bits);
        this.palette.write(param0);
        param0.writeLongArray(this.storage.getRaw());
        this.release();
    }

    public void read(ListTag param0, long[] param1) {
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

        this.release();
    }

    public void write(CompoundTag param0, String param1, String param2) {
        this.acquire();
        HashMapPalette<T> var0 = new HashMapPalette<>(this.registry, this.bits, this.dummyPaletteResize, this.reader, this.writer);
        var0.idFor(this.defaultValue);
        int[] var1 = new int[4096];

        for(int var2 = 0; var2 < 4096; ++var2) {
            var1[var2] = var0.idFor(this.get(var2));
        }

        ListTag var3 = new ListTag();
        var0.write(var3);
        param0.put(param1, var3);
        int var4 = Math.max(4, Mth.ceillog2(var3.size()));
        BitStorage var5 = new BitStorage(var4, 4096);

        for(int var6 = 0; var6 < var1.length; ++var6) {
            var5.set(var6, var1[var6]);
        }

        param0.putLongArray(param2, var5.getRaw());
        this.release();
    }

    public int getSerializedSize() {
        return 1 + this.palette.getSerializedSize() + FriendlyByteBuf.getVarIntSize(this.storage.getSize()) + this.storage.getRaw().length * 8;
    }

    public boolean maybeHas(T param0) {
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
