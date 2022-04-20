package net.minecraft.network.chat.contents;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

public class KeybindResolver {
    static Function<String, Supplier<Component>> keyResolver = param0 -> () -> Component.literal(param0);

    public static void setKeyResolver(Function<String, Supplier<Component>> param0) {
        keyResolver = param0;
    }
}
