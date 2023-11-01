package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.ContextChain;
import java.util.List;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.execution.tasks.FallthroughTask;

public class ReturnCommand {
    public static <T extends ExecutionCommandSource<T>> void register(CommandDispatcher<T> param0) {
        param0.register(
            LiteralArgumentBuilder.<ExecutionCommandSource>literal("return")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    RequiredArgumentBuilder.<T, Integer>argument("value", IntegerArgumentType.integer())
                        .executes(new ReturnCommand.ReturnValueCustomExecutor<>())
                )
                .then(LiteralArgumentBuilder.<T>literal("fail").executes(new ReturnCommand.ReturnFailCustomExecutor<>()))
                .then(LiteralArgumentBuilder.<T>literal("run").forward(param0.getRoot(), new ReturnCommand.ReturnFromCommandCustomModifier<>(), false))
        );
    }

    static class ReturnFailCustomExecutor<T extends ExecutionCommandSource<T>> implements CustomCommandExecutor.CommandAdapter<T> {
        public void run(T param0, ContextChain<T> param1, ChainModifiers param2, ExecutionControl<T> param3) {
            param0.callback().onFailure();
            Frame var0 = param3.currentFrame();
            var0.returnFailure();
            var0.discard();
        }
    }

    static class ReturnFromCommandCustomModifier<T extends ExecutionCommandSource<T>> implements CustomModifierExecutor.ModifierAdapter<T> {
        public void apply(T param0, List<T> param1, ContextChain<T> param2, ChainModifiers param3, ExecutionControl<T> param4) {
            if (param1.isEmpty()) {
                if (param3.isReturn()) {
                    param4.queueNext(FallthroughTask.instance());
                }

            } else {
                param4.currentFrame().discard();
                ContextChain<T> var0 = param2.nextStage();
                String var1 = var0.getTopContext().getInput();
                param4.queueNext(new BuildContexts.Continuation<>(var1, var0, param3.setReturn(), param0, param1));
            }
        }
    }

    static class ReturnValueCustomExecutor<T extends ExecutionCommandSource<T>> implements CustomCommandExecutor.CommandAdapter<T> {
        public void run(T param0, ContextChain<T> param1, ChainModifiers param2, ExecutionControl<T> param3) {
            int var0 = IntegerArgumentType.getInteger(param1.getTopContext(), "value");
            param0.callback().onSuccess(var0);
            Frame var1 = param3.currentFrame();
            var1.returnSuccess(var0);
            var1.discard();
        }
    }
}
