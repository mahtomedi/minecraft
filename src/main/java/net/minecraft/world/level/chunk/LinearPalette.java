package net.minecraft.world.level.chunk;

import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

public class LinearPalette<T> implements Palette<T> {
    private final IdMapper<T> registry;
    private final T[] values;
    private final PaletteResize<T> resizeHandler;
    private final Function<CompoundTag, T> reader;
    private final int bits;
    private int size;

    public LinearPalette(IdMapper<T> param0, int param1, PaletteResize<T> param2, Function<CompoundTag, T> param3) {
        this.registry = param0;
        this.values = (T[])(new Object[1 << param1]);
        this.bits = param1;
        this.resizeHandler = param2;
        this.reader = param3;
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

    @Nullable
    @Override
    public T valueFor(int param0) {
        return param0 >= 0 && param0 < this.size ? this.values[param0] : null;
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

    @Override
    public void read(ListTag param0) {
        for(int var0 = 0; var0 < param0.size(); ++var0) {
            this.values[var0] = this.reader.apply(param0.getCompound(var0));
        }

        this.size = param0.size();
    }
}
