package net.minecraft.util;

import com.mojang.datafixers.types.DynamicOps;

public interface Serializer<O> {
    <T> T serialize(O var1, DynamicOps<T> var2);
}
