package net.minecraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ResultConsumer;
import java.util.function.IntConsumer;

public interface ExecutionCommandSource<T extends ExecutionCommandSource<T>> {
    boolean hasPermission(int var1);

    void storeResults(boolean var1, int var2);

    void storeReturnValue(int var1);

    T withReturnValueConsumer(IntConsumer var1);

    T withCallback(CommandResultConsumer<T> var1);

    T clearCallbacks();

    CommandDispatcher<T> dispatcher();

    static <T extends ExecutionCommandSource<T>> ResultConsumer<T> resultConsumer() {
        return (param0, param1, param2) -> param0.getSource().storeResults(param1, param2);
    }
}
