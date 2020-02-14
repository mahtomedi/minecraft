package net.minecraft.core;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.UUID;
import net.minecraft.util.Serializable;

public final class SerializableUUID implements Serializable {
    private final UUID value;

    public SerializableUUID(UUID param0) {
        this.value = param0;
    }

    public UUID value() {
        return this.value;
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        return param0.createString(this.value.toString());
    }

    public static SerializableUUID of(Dynamic<?> param0) {
        String var0 = param0.asString().orElseThrow(() -> new IllegalArgumentException("Could not parse UUID"));
        return new SerializableUUID(UUID.fromString(var0));
    }

    @Override
    public String toString() {
        return this.value.toString();
    }
}
