package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public interface PositionSourceType<T extends PositionSource> {
    PositionSourceType<BlockPositionSource> BLOCK = register("block", new BlockPositionSource.Type());
    PositionSourceType<EntityPositionSource> ENTITY = register("entity", new EntityPositionSource.Type());

    T read(FriendlyByteBuf var1);

    void write(FriendlyByteBuf var1, T var2);

    Codec<T> codec();

    static <S extends PositionSourceType<T>, T extends PositionSource> S register(String param0, S param1) {
        return Registry.register(BuiltInRegistries.POSITION_SOURCE_TYPE, param0, param1);
    }

    static PositionSource fromNetwork(FriendlyByteBuf param0) {
        PositionSourceType<?> var0 = param0.readById(BuiltInRegistries.POSITION_SOURCE_TYPE);
        if (var0 == null) {
            throw new IllegalArgumentException("Unknown position source type");
        } else {
            return var0.read(param0);
        }
    }

    static <T extends PositionSource> void toNetwork(T param0, FriendlyByteBuf param1) {
        param1.writeId(BuiltInRegistries.POSITION_SOURCE_TYPE, param0.getType());
        param0.getType().write(param1, param0);
    }
}
