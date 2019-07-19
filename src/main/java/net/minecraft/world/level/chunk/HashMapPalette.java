package net.minecraft.world.level.chunk;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HashMapPalette<T> implements Palette<T> {
    private final IdMapper<T> registry;
    private final CrudeIncrementalIntIdentityHashBiMap<T> values;
    private final PaletteResize<T> resizeHandler;
    private final Function<CompoundTag, T> reader;
    private final Function<T, CompoundTag> writer;
    private final int bits;

    public HashMapPalette(IdMapper<T> param0, int param1, PaletteResize<T> param2, Function<CompoundTag, T> param3, Function<T, CompoundTag> param4) {
        this.registry = param0;
        this.bits = param1;
        this.resizeHandler = param2;
        this.reader = param3;
        this.writer = param4;
        this.values = new CrudeIncrementalIntIdentityHashBiMap<>(1 << param1);
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
    public boolean maybeHas(T param0) {
        return this.values.getId(param0) != -1;
    }

    @Nullable
    @Override
    public T valueFor(int param0) {
        return this.values.byId(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void read(FriendlyByteBuf param0) {
        this.values.clear();
        int var0 = param0.readVarInt();

        for(int var1 = 0; var1 < var0; ++var1) {
            this.values.add(this.registry.byId(param0.readVarInt()));
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

    public int getSize() {
        return this.values.size();
    }

    @Override
    public void read(ListTag param0) {
        this.values.clear();

        for(int var0 = 0; var0 < param0.size(); ++var0) {
            this.values.add(this.reader.apply(param0.getCompound(var0)));
        }

    }

    public void write(ListTag param0) {
        for(int var0 = 0; var0 < this.getSize(); ++var0) {
            param0.add(this.writer.apply(this.values.byId(var0)));
        }

    }
}
