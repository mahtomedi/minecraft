package net.minecraft.world;

import javax.annotation.Nullable;

public interface Clearable {
    void clearContent();

    static void tryClear(@Nullable Object param0) {
        if (param0 instanceof Clearable) {
            ((Clearable)param0).clearContent();
        }

    }
}
