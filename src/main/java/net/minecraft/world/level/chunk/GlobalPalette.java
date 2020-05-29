package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GlobalPalette<T> implements Palette<T> {
    private final IdMapper<T> registry;
    private final T defaultValue;

    public GlobalPalette(IdMapper<T> param0, T param1) {
        this.registry = param0;
        this.defaultValue = param1;
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
        return (T)(var0 == null ? this.defaultValue : var0);
    }

    @OnlyIn(Dist.CLIENT)
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
    public void read(ListTag param0) {
    }
}
