package net.minecraft.world.scores;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;

public interface ScoreAccess {
    int get();

    void set(int var1);

    default int add(int param0) {
        int var0 = this.get() + param0;
        this.set(var0);
        return var0;
    }

    default int increment() {
        return this.add(1);
    }

    default void reset() {
        this.set(0);
    }

    boolean locked();

    void unlock();

    void lock();

    @Nullable
    Component display();

    void display(@Nullable Component var1);

    void numberFormatOverride(@Nullable NumberFormat var1);
}
