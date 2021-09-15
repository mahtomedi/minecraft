package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;

public class GlobalPalette<T> implements Palette<T> {
    private final IdMap<T> registry;

    public GlobalPalette(IdMap<T> param0) {
        this.registry = param0;
    }

    public static <A> Palette<A> create(int param0, IdMap<A> param1, PaletteResize<A> param2) {
        return new GlobalPalette<>(param1);
    }

    @Override
    public int idFor(T param0) {
        int var0 = this.registry.getId(param0);
        return var0 == -1 ? 0 : var0;
    }

    @Override
    public boolean maybeHas(Predicate<T> param0) {
        return true;
    }

    @Override
    public T valueFor(int param0) {
        T var0 = this.registry.byId(param0);
        if (var0 == null) {
            throw new MissingPaletteEntryException(param0);
        } else {
            return var0;
        }
    }

    @Override
    public void read(FriendlyByteBuf param0) {
    }

    @Override
    public void write(FriendlyByteBuf param0) {
    }

    @Override
    public int getSerializedSize() {
        return FriendlyByteBuf.getVarIntSize(0);
    }

    @Override
    public int getSize() {
        return this.registry.size();
    }
}
