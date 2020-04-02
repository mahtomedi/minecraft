package net.minecraft.util;

import javax.annotation.Nullable;

public class ExceptionCollector<T extends Throwable> {
    @Nullable
    private T result;

    public void add(T param0) {
        if (this.result == null) {
            this.result = param0;
        } else {
            this.result.addSuppressed(param0);
        }

    }

    public void throwIfPresent() throws T {
        if (this.result != null) {
            throw this.result;
        }
    }
}
