package net.minecraft.world.level.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.util.ThreadingDetector;
import net.minecraft.util.ZeroBitStorage;

public class PalettedContainer<T> implements PaletteResize<T>, PalettedContainerRO<T> {
    private static final int MIN_PALETTE_BITS = 0;
    private final PaletteResize<T> dummyPaletteResize = (param0x, param1x) -> 0;
    private final IdMap<T> registry;
    private volatile PalettedContainer.Data<T> data;
    private final PalettedContainer.Strategy strategy;
    private final ThreadingDetector threadingDetector = new ThreadingDetector("PalettedContainer");

    public void acquire() {
        this.threadingDetector.checkAndLock();
    }

    public void release() {
        this.threadingDetector.checkAndUnlock();
    }

    public static <T> Codec<PalettedContainer<T>> codecRW(IdMap<T> param0, Codec<T> param1, PalettedContainer.Strategy param2, T param3) {
        PalettedContainerRO.Unpacker<T, PalettedContainer<T>> var0 = PalettedContainer::unpack;
        return codec(param0, param1, param2, param3, var0);
    }

    public static <T> Codec<PalettedContainerRO<T>> codecRO(IdMap<T> param0, Codec<T> param1, PalettedContainer.Strategy param2, T param3) {
        PalettedContainerRO.Unpacker<T, PalettedContainerRO<T>> var0 = (param0x, param1x, param2x) -> unpack(param0x, param1x, param2x)
                .map((Function<? super PalettedContainer<T>, ? extends PalettedContainer<T>>)(param0xx -> param0xx));
        return codec(param0, param1, param2, param3, var0);
    }

    private static <T, C extends PalettedContainerRO<T>> Codec<C> codec(
        IdMap<T> param0, Codec<T> param1, PalettedContainer.Strategy param2, T param3, PalettedContainerRO.Unpacker<T, C> param4
    ) {
        return RecordCodecBuilder.create(
                param2x -> param2x.group(
                            param1.mapResult(ExtraCodecs.orElsePartial(param3))
                                .listOf()
                                .fieldOf("palette")
                                .forGetter(PalettedContainerRO.PackedData::paletteEntries),
                            Codec.LONG_STREAM.optionalFieldOf("data").forGetter(PalettedContainerRO.PackedData::storage)
                        )
                        .apply(param2x, PalettedContainerRO.PackedData::new)
            )
            .comapFlatMap(param3x -> param4.read(param0, param2, param3x), param2x -> param2x.pack(param0, param2));
    }

    public PalettedContainer(IdMap<T> param0, PalettedContainer.Strategy param1, PalettedContainer.Configuration<T> param2, BitStorage param3, List<T> param4) {
        this.registry = param0;
        this.strategy = param1;
        this.data = new PalettedContainer.Data<>(param2, param3, param2.factory().create(param2.bits(), param0, this, param4));
    }

    private PalettedContainer(IdMap<T> param0, PalettedContainer.Strategy param1, PalettedContainer.Data<T> param2) {
        this.registry = param0;
        this.strategy = param1;
        this.data = param2;
    }

    public PalettedContainer(IdMap<T> param0, T param1, PalettedContainer.Strategy param2) {
        this.strategy = param2;
        this.registry = param0;
        this.data = this.createOrReuseData(null, 0);
        this.data.palette.idFor(param1);
    }

    private PalettedContainer.Data<T> createOrReuseData(@Nullable PalettedContainer.Data<T> param0, int param1) {
        PalettedContainer.Configuration<T> var0 = this.strategy.getConfiguration(this.registry, param1);
        return param0 != null && var0.equals(param0.configuration()) ? param0 : var0.createData(this.registry, this, this.strategy.size());
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
        int var0 = this.data.palette.idFor(param1);
        this.data.storage.set(param0, var0);
    }

    @Override
    public T get(int param0, int param1, int param2) {
        return this.get(this.strategy.getIndex(param0, param1, param2));
    }

    protected T get(int param0) {
        PalettedContainer.Data<T> var0 = this.data;
        return var0.palette.valueFor(var0.storage.get(param0));
    }

    @Override
    public void getAll(Consumer<T> param0) {
        Palette<T> var0 = this.data.palette();
        IntSet var1 = new IntArraySet();
        this.data.storage.getAll(var1::add);
        var1.forEach(param2 -> param0.accept(var0.valueFor(param2)));
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

    @Override
    public void write(FriendlyByteBuf param0) {
        this.acquire();

        try {
            this.data.write(param0);
        } finally {
            this.release();
        }

    }

    private static <T> DataResult<PalettedContainer<T>> unpack(IdMap<T> param0x, PalettedContainer.Strategy param1x, PalettedContainerRO.PackedData<T> param2x) {
        List<T> var0x = param2x.paletteEntries();
        int var1 = param1x.size();
        int var2 = param1x.calculateBitsForSerialization(param0x, var0x.size());
        PalettedContainer.Configuration<T> var3 = param1x.getConfiguration(param0x, var2);
        BitStorage var4;
        if (var2 == 0) {
            var4 = new ZeroBitStorage(var1);
        } else {
            Optional<LongStream> var5 = param2x.storage();
            if (var5.isEmpty()) {
                return DataResult.error(() -> "Missing values for non-zero storage");
            }

            long[] var6 = var5.get().toArray();

            try {
                if (var3.factory() == PalettedContainer.Strategy.GLOBAL_PALETTE_FACTORY) {
                    Palette<T> var7 = new HashMapPalette<>(param0x, var2, (param0xx, param1xx) -> 0, var0x);
                    SimpleBitStorage var8 = new SimpleBitStorage(var2, var1, var6);
                    int[] var9 = new int[var1];
                    var8.unpack(var9);
                    swapPalette(var9, param2xx -> param0x.getId(var7.valueFor(param2xx)));
                    var4 = new SimpleBitStorage(var3.bits(), var1, var9);
                } else {
                    var4 = new SimpleBitStorage(var3.bits(), var1, var6);
                }
            } catch (SimpleBitStorage.InitializationException var131) {
                return DataResult.error(() -> "Failed to read PalettedContainer: " + var131.getMessage());
            }
        }

        return DataResult.success(new PalettedContainer<>(param0x, param1x, var3, var4, var0x));
    }

    @Override
    public PalettedContainerRO.PackedData<T> pack(IdMap<T> param0, PalettedContainer.Strategy param1) {
        this.acquire();

        PalettedContainerRO.PackedData var12;
        try {
            HashMapPalette<T> var0 = new HashMapPalette<>(param0, this.data.storage.getBits(), this.dummyPaletteResize);
            int var1 = param1.size();
            int[] var2 = new int[var1];
            this.data.storage.unpack(var2);
            swapPalette(var2, param1x -> var0.idFor(this.data.palette.valueFor(param1x)));
            int var3 = param1.calculateBitsForSerialization(param0, var0.getSize());
            Optional<LongStream> var5;
            if (var3 != 0) {
                SimpleBitStorage var4 = new SimpleBitStorage(var3, var1, var2);
                var5 = Optional.of(Arrays.stream(var4.getRaw()));
            } else {
                var5 = Optional.empty();
            }

            var12 = new PalettedContainerRO.PackedData<>(var0.getEntries(), var5);
        } finally {
            this.release();
        }

        return var12;
    }

    private static <T> void swapPalette(int[] param0, IntUnaryOperator param1) {
        int var0 = -1;
        int var1 = -1;

        for(int var2 = 0; var2 < param0.length; ++var2) {
            int var3 = param0[var2];
            if (var3 != var0) {
                var0 = var3;
                var1 = param1.applyAsInt(var3);
            }

            param0[var2] = var1;
        }

    }

    @Override
    public int getSerializedSize() {
        return this.data.getSerializedSize();
    }

    @Override
    public boolean maybeHas(Predicate<T> param0) {
        return this.data.palette.maybeHas(param0);
    }

    public PalettedContainer<T> copy() {
        return new PalettedContainer<>(this.registry, this.strategy, this.data.copy());
    }

    @Override
    public PalettedContainer<T> recreate() {
        return new PalettedContainer<>(this.registry, this.data.palette.valueFor(0), this.strategy);
    }

    @Override
    public void count(PalettedContainer.CountConsumer<T> param0) {
        if (this.data.palette.getSize() == 1) {
            param0.accept(this.data.palette.valueFor(0), this.data.storage.getSize());
        } else {
            Int2IntOpenHashMap var0 = new Int2IntOpenHashMap();
            this.data.storage.getAll(param1 -> var0.addTo(param1, 1));
            var0.int2IntEntrySet().forEach(param1 -> param0.accept(this.data.palette.valueFor(param1.getIntKey()), param1.getIntValue()));
        }
    }

    static record Configuration<T>(Palette.Factory factory, int bits) {
        public PalettedContainer.Data<T> createData(IdMap<T> param0, PaletteResize<T> param1, int param2) {
            BitStorage var0 = (BitStorage)(this.bits == 0 ? new ZeroBitStorage(param2) : new SimpleBitStorage(this.bits, param2));
            Palette<T> var1 = this.factory.create(this.bits, param0, param1, List.of());
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
                T var1 = param0.valueFor(param1.get(var0));
                this.storage.set(var0, this.palette.idFor(var1));
            }

        }

        public int getSerializedSize() {
            return 1 + this.palette.getSerializedSize() + FriendlyByteBuf.getVarIntSize(this.storage.getSize()) + this.storage.getRaw().length * 8;
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeByte(this.storage.getBits());
            this.palette.write(param0);
            param0.writeLongArray(this.storage.getRaw());
        }

        public PalettedContainer.Data<T> copy() {
            return new PalettedContainer.Data<>(this.configuration, this.storage.copy(), this.palette.copy());
        }
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
                    case 1, 2, 3 -> new PalettedContainer.Configuration(LINEAR_PALETTE_FACTORY, param1);
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
