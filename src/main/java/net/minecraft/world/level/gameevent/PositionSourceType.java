package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface PositionSourceType<T extends PositionSource> {
    PositionSourceType<BlockPositionSource> BLOCK = register("block", new BlockPositionSource.Type());
    PositionSourceType<EntityPositionSource> ENTITY = register("entity", new EntityPositionSource.Type());

    T read(FriendlyByteBuf var1);

    void write(FriendlyByteBuf var1, T var2);

    Codec<T> codec();

    static <S extends PositionSourceType<T>, T extends PositionSource> S register(String param0, S param1) {
        return Registry.register(Registry.POSITION_SOURCE_TYPE, param0, param1);
    }

    static PositionSource fromNetwork(FriendlyByteBuf param0) {
        ResourceLocation var0 = param0.readResourceLocation();
        return Registry.POSITION_SOURCE_TYPE
            .getOptional(var0)
            .orElseThrow(() -> new IllegalArgumentException("Unknown position source type " + var0))
            .read(param0);
    }

    static <T extends PositionSource> void toNetwork(T param0, FriendlyByteBuf param1) {
        param1.writeResourceLocation(Registry.POSITION_SOURCE_TYPE.getKey(param0.getType()));
        param0.getType().write(param1, param0);
    }
}
