package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;

public class SingleValuePalette<T> implements Palette<T> {
    private final IdMap<T> registry;
    @Nullable
    private T value;
    private final PaletteResize<T> resizeHandler;

    public SingleValuePalette(IdMap<T> param0, PaletteResize<T> param1) {
        this.registry = param0;
        this.resizeHandler = param1;
    }

    public static <A> Palette<A> create(int param0, IdMap<A> param1, PaletteResize<A> param2) {
        return new SingleValuePalette<>(param1, param2);
    }

    @Override
    public int idFor(T param0) {
        if (this.value != null && this.value != param0) {
            return this.resizeHandler.onResize(1, param0);
        } else {
            this.value = param0;
            return 0;
        }
    }

    @Override
    public boolean maybeHas(Predicate<T> param0) {
        if (this.value == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        } else {
            return param0.test(this.value);
        }
    }

    @Override
    public T valueFor(int param0) {
        if (this.value != null && param0 == 0) {
            return this.value;
        } else {
            throw new IllegalStateException("Missing Palette entry for id " + param0 + ".");
        }
    }

    @Override
    public void read(FriendlyByteBuf param0) {
        this.value = this.registry.byId(param0.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        if (this.value == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        } else {
            param0.writeVarInt(this.registry.getId(this.value));
        }
    }

    @Override
    public int getSerializedSize() {
        if (this.value == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        } else {
            return FriendlyByteBuf.getVarIntSize(this.registry.getId(this.value));
        }
    }

    @Override
    public int getSize() {
        return 1;
    }
}
