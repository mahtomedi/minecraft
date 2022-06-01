package net.minecraft.world.level.chunk;

import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;

public interface PalettedContainerRO<T> {
    T get(int var1, int var2, int var3);

    void getAll(Consumer<T> var1);

    void write(FriendlyByteBuf var1);

    int getSerializedSize();

    boolean maybeHas(Predicate<T> var1);

    void count(PalettedContainer.CountConsumer<T> var1);

    PalettedContainer<T> recreate();

    PalettedContainerRO.PackedData<T> pack(IdMap<T> var1, PalettedContainer.Strategy var2);

    public static record PackedData<T>(List<T> paletteEntries, Optional<LongStream> storage) {
    }

    public interface Unpacker<T, C extends PalettedContainerRO<T>> {
        DataResult<C> read(IdMap<T> var1, PalettedContainer.Strategy var2, PalettedContainerRO.PackedData<T> var3);
    }
}
