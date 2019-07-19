package net.minecraft.commands.synchronization;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;

public class EmptyArgumentSerializer<T extends ArgumentType<?>> implements ArgumentSerializer<T> {
    private final Supplier<T> constructor;

    public EmptyArgumentSerializer(Supplier<T> param0) {
        this.constructor = param0;
    }

    @Override
    public void serializeToNetwork(T param0, FriendlyByteBuf param1) {
    }

    @Override
    public T deserializeFromNetwork(FriendlyByteBuf param0) {
        return this.constructor.get();
    }

    @Override
    public void serializeToJson(T param0, JsonObject param1) {
    }
}
