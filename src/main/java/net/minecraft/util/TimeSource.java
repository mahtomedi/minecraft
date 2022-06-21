package net.minecraft.util;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

@FunctionalInterface
public interface TimeSource {
    long get(TimeUnit var1);

    public interface NanoTimeSource extends LongSupplier, TimeSource {
        @Override
        default long get(TimeUnit param0) {
            return param0.convert(this.getAsLong(), TimeUnit.NANOSECONDS);
        }
    }
}
