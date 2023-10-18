package net.minecraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.IntConsumer;
import javax.annotation.Nullable;
import net.minecraft.commands.execution.TraceCallbacks;

public interface ExecutionCommandSource<T extends ExecutionCommandSource<T>> {
    boolean hasPermission(int var1);

    void storeResults(boolean var1, int var2);

    void storeReturnValue(int var1);

    T withReturnValueConsumer(IntConsumer var1);

    T withCallback(CommandResultConsumer<T> var1);

    T clearCallbacks();

    CommandDispatcher<T> dispatcher();

    void handleError(CommandExceptionType var1, Message var2, boolean var3, @Nullable TraceCallbacks var4);

    default void handleError(CommandSyntaxException param0, boolean param1, @Nullable TraceCallbacks param2) {
        this.handleError(param0.getType(), param0.getRawMessage(), param1, param2);
    }

    static <T extends ExecutionCommandSource<T>> ResultConsumer<T> resultConsumer() {
        return (param0, param1, param2) -> param0.getSource().storeResults(param1, param2);
    }
}
