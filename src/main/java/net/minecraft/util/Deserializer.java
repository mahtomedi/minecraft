package net.minecraft.util;

import com.mojang.datafixers.Dynamic;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface Deserializer<T> {
    Logger LOGGER = LogManager.getLogger();

    T deserialize(Dynamic<?> var1);

    static <T, V, U extends Deserializer<V>> V deserialize(Dynamic<T> param0, Registry<U> param1, String param2, V param3) {
        U var0 = param1.get(new ResourceLocation(param0.get(param2).asString("")));
        V var1;
        if (var0 != null) {
            var1 = var0.deserialize(param0);
        } else {
            LOGGER.error("Unknown type {}, replacing with {}", param0.get(param2).asString(""), param3);
            var1 = param3;
        }

        return var1;
    }
}
