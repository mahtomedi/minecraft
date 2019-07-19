package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface Palette<T> {
    int idFor(T var1);

    boolean maybeHas(T var1);

    @Nullable
    T valueFor(int var1);

    @OnlyIn(Dist.CLIENT)
    void read(FriendlyByteBuf var1);

    void write(FriendlyByteBuf var1);

    int getSerializedSize();

    void read(ListTag var1);
}
