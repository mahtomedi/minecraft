package net.minecraft.world.level.chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;

public class HashMapPalette<T> implements Palette<T> {
    private final IdMap<T> registry;
    private final CrudeIncrementalIntIdentityHashBiMap<T> values;
    private final PaletteResize<T> resizeHandler;
    private final int bits;

    public HashMapPalette(IdMap<T> param0, int param1, PaletteResize<T> param2, List<T> param3) {
        this(param0, param1, param2);
        param3.forEach(this.values::add);
    }

    public HashMapPalette(IdMap<T> param0, int param1, PaletteResize<T> param2) {
        this(param0, param1, param2, CrudeIncrementalIntIdentityHashBiMap.create(1 << param1));
    }

    private HashMapPalette(IdMap<T> param0, int param1, PaletteResize<T> param2, CrudeIncrementalIntIdentityHashBiMap<T> param3) {
        this.registry = param0;
        this.bits = param1;
        this.resizeHandler = param2;
        this.values = param3;
    }

    public static <A> Palette<A> create(int param0, IdMap<A> param1, PaletteResize<A> param2, List<A> param3) {
        return new HashMapPalette<>(param1, param0, param2, param3);
    }

    @Override
    public int idFor(T param0) {
        int var0 = this.values.getId(param0);
        if (var0 == -1) {
            var0 = this.values.add(param0);
            if (var0 >= 1 << this.bits) {
                var0 = this.resizeHandler.onResize(this.bits + 1, param0);
            }
        }

        return var0;
    }

    @Override
    public boolean maybeHas(Predicate<T> param0) {
        for(int var0 = 0; var0 < this.getSize(); ++var0) {
            if (param0.test(this.values.byId(var0))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public T valueFor(int param0) {
        T var0 = this.values.byId(param0);
        if (var0 == null) {
            throw new MissingPaletteEntryException(param0);
        } else {
            return var0;
        }
    }

    @Override
    public void read(FriendlyByteBuf param0) {
        this.values.clear();
        int var0 = param0.readVarInt();

        for(int var1 = 0; var1 < var0; ++var1) {
            this.values.add(this.registry.byIdOrThrow(param0.readVarInt()));
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) {
        int var0 = this.getSize();
        param0.writeVarInt(var0);

        for(int var1 = 0; var1 < var0; ++var1) {
            param0.writeVarInt(this.registry.getId(this.values.byId(var1)));
        }

    }

    @Override
    public int getSerializedSize() {
        int var0 = FriendlyByteBuf.getVarIntSize(this.getSize());

        for(int var1 = 0; var1 < this.getSize(); ++var1) {
            var0 += FriendlyByteBuf.getVarIntSize(this.registry.getId(this.values.byId(var1)));
        }

        return var0;
    }

    public List<T> getEntries() {
        ArrayList<T> var0 = new ArrayList<>();
        this.values.iterator().forEachRemaining(var0::add);
        return var0;
    }

    @Override
    public int getSize() {
        return this.values.size();
    }

    @Override
    public Palette<T> copy() {
        return new HashMapPalette<>(this.registry, this.bits, this.resizeHandler, this.values.copy());
    }
}
