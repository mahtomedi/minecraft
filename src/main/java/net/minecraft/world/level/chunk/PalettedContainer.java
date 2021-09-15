package net.minecraft.world.level.chunk;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.util.DebugBuffer;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.util.ThreadingDetector;
import net.minecraft.util.ZeroBitStorage;

public class PalettedContainer<T> implements PaletteResize<T> {
    private static final int MIN_PALETTE_BITS = 0;
    private final PaletteResize<T> dummyPaletteResize = (param0x, param1x) -> 0;
    private final IdMap<T> registry;
    private volatile PalettedContainer.Data<T> data;
    private final PalettedContainer.Strategy strategy;
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

    public static <T> Codec<PalettedContainer<T>> codec(IdMap<T> param0, Codec<T> param1, PalettedContainer.Strategy param2) {
        return RecordCodecBuilder.<PalettedContainer.DiscData>create(
                param1x -> param1x.group(
                            param1.listOf().fieldOf("palette").forGetter(PalettedContainer.DiscData::paletteEntries),
                            Codec.LONG_STREAM.optionalFieldOf("data").forGetter(PalettedContainer.DiscData::storage)
                        )
                        .apply(param1x, PalettedContainer.DiscData::new)
            )
            .comapFlatMap(param2x -> read(param0, param2, param2x), param2x -> param2x.write(param0, param2));
    }

    public PalettedContainer(IdMap<T> param0, PalettedContainer.Strategy param1, PalettedContainer.Configuration<T> param2, BitStorage param3, List<T> param4) {
        this.registry = param0;
        this.strategy = param1;
        Palette<T> var0 = param2.factory().create(param2.bits(), param0, this);
        param4.forEach(var0::idFor);
        this.data = new PalettedContainer.Data<>(param2, param3, var0);
    }

    public PalettedContainer(IdMap<T> param0, T param1, PalettedContainer.Strategy param2) {
        this.strategy = param2;
        this.registry = param0;
        this.data = this.createOrReuseData(null, 0);
        this.data.palette.idFor(param1);
    }

    private PalettedContainer.Data<T> createOrReuseData(@Nullable PalettedContainer.Data<T> param0, int param1) {
        PalettedContainer.Configuration<T> var0 = this.strategy.getConfiguration(this.registry, param1);
        return param0 != null && var0.equals(param0.configuration()) ? param0 : var0.createData(this.registry, this, this.strategy.size(), null);
    }

    @Override
    public int onResize(int param0, T param1) {
        PalettedContainer.Data<T> var0 = this.data;
        PalettedContainer.Data<T> var1 = this.createOrReuseData(var0, param0);
        var1.copyFrom(var0.palette, var0.storage);
        this.data = var1;
        return var1.palette.idFor(param1);
    }

    public T getAndSet(int param0, int param1, int param2, T param3) {
        this.acquire();

        Object var5;
        try {
            var5 = this.getAndSet(this.strategy.getIndex(param0, param1, param2), param3);
        } finally {
            this.release();
        }

        return (T)var5;
    }

    public T getAndSetUnchecked(int param0, int param1, int param2, T param3) {
        return this.getAndSet(this.strategy.getIndex(param0, param1, param2), param3);
    }

    private T getAndSet(int param0, T param1) {
        int var0 = this.data.palette.idFor(param1);
        int var1 = this.data.storage.getAndSet(param0, var0);
        return this.data.palette.valueFor(var1);
    }

    public void set(int param0, int param1, int param2, T param3) {
        this.acquire();

        try {
            this.set(this.strategy.getIndex(param0, param1, param2), param3);
        } finally {
            this.release();
        }

    }

    private void set(int param0, T param1) {
        this.data.set(param0, param1);
    }

    public T get(int param0, int param1, int param2) {
        return this.get(this.strategy.getIndex(param0, param1, param2));
    }

    protected T get(int param0) {
        PalettedContainer.Data<T> var0 = this.data;
        return var0.palette.valueFor(var0.storage.get(param0));
    }

    public void read(FriendlyByteBuf param0) {
        this.acquire();

        try {
            int var0 = param0.readByte();
            PalettedContainer.Data<T> var1 = this.createOrReuseData(this.data, var0);
            var1.palette.read(param0);
            param0.readLongArray(var1.storage.getRaw());
            this.data = var1;
        } finally {
            this.release();
        }

    }

    public void write(FriendlyByteBuf param0) {
        this.acquire();

        try {
            this.data.write(param0);
        } finally {
            this.release();
        }

    }

    private static <T> DataResult<PalettedContainer<T>> read(IdMap<T> param0, PalettedContainer.Strategy param1, PalettedContainer.DiscData<T> param2) {
        List<T> var0 = param2.paletteEntries();
        int var1 = param1.size();
        int var2 = param1.calculateBitsForSerialization(param0, var0.size());
        PalettedContainer.Configuration<T> var3 = param1.getConfiguration(param0, var2);
        BitStorage var4;
        if (var2 == 0) {
            var4 = new ZeroBitStorage(var1);
        } else {
            Optional<LongStream> var5 = param2.storage();
            if (var5.isEmpty()) {
                return DataResult.error("Missing values for non-zero storage");
            }

            long[] var6 = var5.get().toArray();
            if (var3.factory() == PalettedContainer.Strategy.GLOBAL_PALETTE_FACTORY) {
                Palette<T> var7 = new HashMapPalette<>(param0, var2, (param0x, param1x) -> 0, var0);
                SimpleBitStorage var8 = new SimpleBitStorage(var2, param1.size(), var6);
                IntStream var9 = IntStream.range(0, var8.getSize()).map(param3 -> param0.getId(var7.valueFor(var8.get(param3))));
                var4 = new SimpleBitStorage(var3.bits(), var1, var9);
            } else {
                var4 = new SimpleBitStorage(var3.bits(), var1, var6);
            }
        }

        return DataResult.success(new PalettedContainer<>(param0, param1, var3, var4, var0));
    }

    private PalettedContainer.DiscData<T> write(IdMap<T> param0, PalettedContainer.Strategy param1) {
        this.acquire();

        PalettedContainer.DiscData var17;
        try {
            HashMapPalette<T> var0 = new HashMapPalette<>(param0, this.data.storage.getBits(), this.dummyPaletteResize);
            T var1 = null;
            int var2 = -1;
            int var3 = param1.size();
            int[] var4 = new int[var3];

            for(int var5 = 0; var5 < var3; ++var5) {
                T var6 = this.get(var5);
                if (var6 != var1) {
                    var1 = var6;
                    var2 = var0.idFor(var6);
                }

                var4[var5] = var2;
            }

            int var7 = param1.calculateBitsForSerialization(param0, var0.getSize());
            Optional<LongStream> var12;
            if (var7 == 0) {
                var12 = Optional.empty();
            } else {
                BitStorage var8 = new SimpleBitStorage(var7, var3);

                for(int var9 = 0; var9 < var4.length; ++var9) {
                    var8.set(var9, var4[var9]);
                }

                long[] var10 = var8.getRaw();
                var12 = Optional.of(Arrays.stream(var10));
            }

            var17 = new PalettedContainer.DiscData<>(var0.getEntries(), var12);
        } finally {
            this.release();
        }

        return var17;
    }

    public int getSerializedSize() {
        return this.data.getSerializedSize();
    }

    public boolean maybeHas(Predicate<T> param0) {
        return this.data.palette.maybeHas(param0);
    }

    public void count(PalettedContainer.CountConsumer<T> param0) {
        Int2IntMap var0 = new Int2IntOpenHashMap();
        this.data.storage.getAll(param1 -> var0.put(param1, var0.get(param1) + 1));
        var0.int2IntEntrySet().forEach(param1 -> param0.accept(this.data.palette.valueFor(param1.getIntKey()), param1.getIntValue()));
    }

    static record Configuration<T>(Palette.Factory factory, int bits) {
        public PalettedContainer.Data<T> createData(IdMap<T> param0, PaletteResize<T> param1, int param2, @Nullable long[] param3) {
            BitStorage var0 = (BitStorage)(this.bits == 0 ? new ZeroBitStorage(param2) : new SimpleBitStorage(this.bits, param2, param3));
            Palette<T> var1 = this.factory.create(this.bits, param0, param1);
            return new PalettedContainer.Data<>(this, var0, var1);
        }
    }

    @FunctionalInterface
    public interface CountConsumer<T> {
        void accept(T var1, int var2);
    }

    static record Data<T>(PalettedContainer.Configuration<T> configuration, BitStorage storage, Palette<T> palette) {
        public void copyFrom(Palette<T> param0, BitStorage param1) {
            for(int var0 = 0; var0 < param1.getSize(); ++var0) {
                this.set(var0, param0.valueFor(param1.get(var0)));
            }

        }

        public void set(int param0, T param1) {
            this.storage.set(param0, this.palette.idFor(param1));
        }

        public int getSerializedSize() {
            return 1 + this.palette.getSerializedSize() + FriendlyByteBuf.getVarIntSize(this.storage.getSize()) + this.storage.getRaw().length * 8;
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeByte(this.storage.getBits());
            this.palette.write(param0);
            param0.writeLongArray(this.storage.getRaw());
        }
    }

    static record DiscData<T>(List<T> paletteEntries, Optional<LongStream> storage) {
    }

    public abstract static class Strategy {
        public static final Palette.Factory SINGLE_VALUE_PALETTE_FACTORY = SingleValuePalette::create;
        public static final Palette.Factory LINEAR_PALETTE_FACTORY = LinearPalette::create;
        public static final Palette.Factory HASHMAP_PALETTE_FACTORY = HashMapPalette::create;
        static final Palette.Factory GLOBAL_PALETTE_FACTORY = GlobalPalette::create;
        public static final PalettedContainer.Strategy SECTION_STATES = new PalettedContainer.Strategy(4) {
            @Override
            public <A> PalettedContainer.Configuration<A> getConfiguration(IdMap<A> param0, int param1) {
                return switch(param1) {
                    case 0 -> new PalettedContainer.Configuration(SINGLE_VALUE_PALETTE_FACTORY, param1);
                    case 1, 2, 3, 4 -> new PalettedContainer.Configuration(LINEAR_PALETTE_FACTORY, 4);
                    case 5, 6, 7, 8 -> new PalettedContainer.Configuration(HASHMAP_PALETTE_FACTORY, param1);
                    default -> new PalettedContainer.Configuration(PalettedContainer.Strategy.GLOBAL_PALETTE_FACTORY, Mth.ceillog2(param0.size()));
                };
            }
        };
        public static final PalettedContainer.Strategy SECTION_BIOMES = new PalettedContainer.Strategy(2) {
            @Override
            public <A> PalettedContainer.Configuration<A> getConfiguration(IdMap<A> param0, int param1) {
                return switch(param1) {
                    case 0 -> new PalettedContainer.Configuration(SINGLE_VALUE_PALETTE_FACTORY, param1);
                    case 1, 2 -> new PalettedContainer.Configuration(LINEAR_PALETTE_FACTORY, param1);
                    default -> new PalettedContainer.Configuration(PalettedContainer.Strategy.GLOBAL_PALETTE_FACTORY, Mth.ceillog2(param0.size()));
                };
            }
        };
        private final int sizeBits;

        Strategy(int param0) {
            this.sizeBits = param0;
        }

        public int size() {
            return 1 << this.sizeBits * 3;
        }

        public int getIndex(int param0, int param1, int param2) {
            return (param1 << this.sizeBits | param2) << this.sizeBits | param0;
        }

        public abstract <A> PalettedContainer.Configuration<A> getConfiguration(IdMap<A> var1, int var2);

        <A> int calculateBitsForSerialization(IdMap<A> param0, int param1) {
            int var0 = Mth.ceillog2(param1);
            PalettedContainer.Configuration<A> var1 = this.getConfiguration(param0, var0);
            return var1.factory() == GLOBAL_PALETTE_FACTORY ? var0 : var1.bits();
        }
    }
}
