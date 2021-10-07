package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.Validate;

public class LinearPalette<T> implements Palette<T> {
    private final IdMap<T> registry;
    private final T[] values;
    private final PaletteResize<T> resizeHandler;
    private final int bits;
    private int size;

    public LinearPalette(IdMap<T> param0, int param1, PaletteResize<T> param2, List<T> param3) {
        this.registry = param0;
        this.values = (T[])(new Object[1 << param1]);
        this.bits = param1;
        this.resizeHandler = param2;
        Validate.isTrue(param3.size() <= this.values.length, "Can't initialize LinearPalette of size %d with %d entries", this.values.length, param3.size());

        for(int var0 = 0; var0 < param3.size(); ++var0) {
            this.values[var0] = param3.get(var0);
        }

        this.size = param3.size();
    }

    public static <A> Palette<A> create(int param0, IdMap<A> param1, PaletteResize<A> param2, List<A> param3) {
        return new LinearPalette<>(param1, param0, param2, param3);
    }

    @Override
    public int idFor(T param0) {
        for(int var0 = 0; var0 < this.size; ++var0) {
            if (this.values[var0] == param0) {
                return var0;
            }
        }

        int var1 = this.size;
        if (var1 < this.values.length) {
            this.values[var1] = param0;
            ++this.size;
            return var1;
        } else {
            return this.resizeHandler.onResize(this.bits + 1, param0);
        }
    }

    @Override
    public boolean maybeHas(Predicate<T> param0) {
        for(int var0 = 0; var0 < this.size; ++var0) {
            if (param0.test(this.values[var0])) {
                return true;
            }
        }

        return false;
    }

    @Override
    public T valueFor(int param0) {
        if (param0 >= 0 && param0 < this.size) {
            return this.values[param0];
        } else {
            throw new MissingPaletteEntryException(param0);
        }
    }

    @Override
    public void read(FriendlyByteBuf param0) {
        this.size = param0.readVarInt();

        for(int var0 = 0; var0 < this.size; ++var0) {
            this.values[var0] = this.registry.byId(param0.readVarInt());
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.size);

        for(int var0 = 0; var0 < this.size; ++var0) {
            param0.writeVarInt(this.registry.getId(this.values[var0]));
        }

    }

    @Override
    public int getSerializedSize() {
        int var0 = FriendlyByteBuf.getVarIntSize(this.getSize());

        for(int var1 = 0; var1 < this.getSize(); ++var1) {
            var0 += FriendlyByteBuf.getVarIntSize(this.registry.getId(this.values[var1]));
        }

        return var0;
    }

    @Override
    public int getSize() {
        return this.size;
    }
}
