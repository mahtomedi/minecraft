package net.minecraft.commands.execution;

import net.minecraft.resources.ResourceLocation;

public interface TraceCallbacks extends AutoCloseable {
    void onCommand(int var1, String var2);

    void onReturn(int var1, String var2, int var3);

    void onError(int var1, String var2);

    void onCall(int var1, ResourceLocation var2, int var3);

    @Override
    void close();
}
