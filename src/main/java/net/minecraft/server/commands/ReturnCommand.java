package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.ContextChain;
import java.util.List;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.tasks.BuildContexts;

public class ReturnCommand {
    public static <T extends ExecutionCommandSource<T>> void register(CommandDispatcher<T> param0) {
        param0.register(
            LiteralArgumentBuilder.<ExecutionCommandSource>literal("return")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    RequiredArgumentBuilder.<T, Integer>argument("value", IntegerArgumentType.integer())
                        .executes(new ReturnCommand.ReturnValueCustomExecutor<>())
                )
                .then(LiteralArgumentBuilder.<T>literal("run").forward(param0.getRoot(), new ReturnCommand.ReturnFromCommandCustomModifier<>(), false))
        );
    }

    static class ReturnFromCommandCustomModifier<T extends ExecutionCommandSource<T>> implements CustomModifierExecutor.ModifierAdapter<T> {
        @Override
        public void apply(List<T> param0, ContextChain<T> param1, boolean param2, ExecutionControl<T> param3) {
            if (!param0.isEmpty()) {
                ContextChain<T> var0 = param1.nextStage();
                String var1 = var0.getTopContext().getInput();
                List<T> var2 = param0.stream().map(param1x -> param1x.withCallback((param1xx, param2x, param3x) -> {
                        param3.discardCurrentDepth();
                        param1xx.storeReturnValue(param3x);
                    })).toList();
                param3.queueNext(new BuildContexts.Continuation<>(var1, var0, param2, var2));
            }
        }
    }

    static class ReturnValueCustomExecutor<T extends ExecutionCommandSource<T>> implements CustomCommandExecutor.CommandAdapter<T> {
        public void run(T param0, ContextChain<T> param1, boolean param2, ExecutionControl<T> param3) {
            param3.discardCurrentDepth();
            int var0 = IntegerArgumentType.getInteger(param1.getTopContext(), "value");
            param0.storeReturnValue(var0);
        }
    }
}
