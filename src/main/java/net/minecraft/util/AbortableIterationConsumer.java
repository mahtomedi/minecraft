package net.minecraft.util;

import java.util.function.Consumer;

@FunctionalInterface
public interface AbortableIterationConsumer<T> {
    AbortableIterationConsumer.Continuation accept(T var1);

    static <T> AbortableIterationConsumer<T> forConsumer(Consumer<T> param0) {
        return param1 -> {
            param0.accept(param1);
            return AbortableIterationConsumer.Continuation.CONTINUE;
        };
    }

    public static enum Continuation {
        CONTINUE,
        ABORT;

        public boolean shouldAbort() {
            return this == ABORT;
        }
    }
}
