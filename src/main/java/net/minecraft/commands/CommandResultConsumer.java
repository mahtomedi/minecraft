package net.minecraft.commands;

public interface CommandResultConsumer<T> {
    void storeResult(T var1, boolean var2, int var3);
}
