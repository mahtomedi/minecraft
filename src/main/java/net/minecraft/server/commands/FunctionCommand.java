package net.minecraft.server.commands;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.execution.tasks.FallthroughTask;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;

public class FunctionCommand {
    private static final DynamicCommandExceptionType ERROR_ARGUMENT_NOT_COMPOUND = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("commands.function.error.argument_not_compound", param0)
    );
    static final DynamicCommandExceptionType ERROR_NO_FUNCTIONS = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("commands.function.scheduled.no_functions", param0)
    );
    @VisibleForTesting
    public static final Dynamic2CommandExceptionType ERROR_FUNCTION_INSTANTATION_FAILURE = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatableEscape("commands.function.instantiationFailure", param0, param1)
    );
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_FUNCTION = (param0, param1) -> {
        ServerFunctionManager var0 = param0.getSource().getServer().getFunctions();
        SharedSuggestionProvider.suggestResource(var0.getTagNames(), param1, "#");
        return SharedSuggestionProvider.suggestResource(var0.getFunctionNames(), param1);
    };
    static final FunctionCommand.Callbacks<CommandSourceStack> FULL_CONTEXT_CALLBACKS = new FunctionCommand.Callbacks<CommandSourceStack>() {
        public void signalResult(CommandSourceStack param0, ResourceLocation param1, int param2) {
            param0.sendSuccess(() -> Component.translatable("commands.function.result", Component.translationArg(param1), param2), true);
        }
    };

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        LiteralArgumentBuilder<CommandSourceStack> var0 = Commands.literal("with");

        for(DataCommands.DataProvider var1 : DataCommands.SOURCE_PROVIDERS) {
            var1.wrap(var0, param1 -> param1.executes(new FunctionCommand.FunctionCustomExecutor() {
                    @Override
                    protected CompoundTag arguments(CommandContext<CommandSourceStack> param0) throws CommandSyntaxException {
                        return var1.access(param0).getData();
                    }
                }).then(Commands.argument("path", NbtPathArgument.nbtPath()).executes(new FunctionCommand.FunctionCustomExecutor() {
                    @Override
                    protected CompoundTag arguments(CommandContext<CommandSourceStack> param0) throws CommandSyntaxException {
                        return FunctionCommand.getArgumentTag(NbtPathArgument.getPath(param0, "path"), var1.access(param0));
                    }
                })));
        }

        param0.register(
            Commands.literal("function")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("name", FunctionArgument.functions()).suggests(SUGGEST_FUNCTION).executes(new FunctionCommand.FunctionCustomExecutor() {
                        @Nullable
                        @Override
                        protected CompoundTag arguments(CommandContext<CommandSourceStack> param0) {
                            return null;
                        }
                    }).then(Commands.argument("arguments", CompoundTagArgument.compoundTag()).executes(new FunctionCommand.FunctionCustomExecutor() {
                        @Override
                        protected CompoundTag arguments(CommandContext<CommandSourceStack> param0) {
                            return CompoundTagArgument.getCompoundTag(param0, "arguments");
                        }
                    })).then(var0)
                )
        );
    }

    static CompoundTag getArgumentTag(NbtPathArgument.NbtPath param0, DataAccessor param1) throws CommandSyntaxException {
        Tag var0 = DataCommands.getSingleTag(param0, param1);
        if (var0 instanceof CompoundTag) {
            return (CompoundTag)var0;
        } else {
            throw ERROR_ARGUMENT_NOT_COMPOUND.create(var0.getType().getName());
        }
    }

    public static CommandSourceStack modifySenderForExecution(CommandSourceStack param0) {
        return param0.withSuppressedOutput().withMaximumPermission(2);
    }

    public static <T extends ExecutionCommandSource<T>> void queueFunctions(
        Collection<CommandFunction<T>> param0,
        @Nullable CompoundTag param1,
        T param2,
        T param3,
        ExecutionControl<T> param4,
        FunctionCommand.Callbacks<T> param5,
        ChainModifiers param6
    ) throws CommandSyntaxException {
        if (param6.isReturn()) {
            queueFunctionsAsReturn(param0, param1, param2, param3, param4, param5);
        } else {
            queueFunctionsNoReturn(param0, param1, param2, param3, param4, param5);
        }

    }

    private static <T extends ExecutionCommandSource<T>> void instantiateAndQueueFunctions(
        @Nullable CompoundTag param0,
        ExecutionControl<T> param1,
        CommandDispatcher<T> param2,
        T param3,
        CommandFunction<T> param4,
        ResourceLocation param5,
        CommandResultCallback param6,
        boolean param7
    ) throws CommandSyntaxException {
        try {
            InstantiatedFunction<T> var0 = param4.instantiate(param0, param2, param3);
            param1.queueNext(new CallFunction<>(var0, param6, param7).bind(param3));
        } catch (FunctionInstantiationException var9) {
            throw ERROR_FUNCTION_INSTANTATION_FAILURE.create(param5, var9.messageComponent());
        }
    }

    private static <T extends ExecutionCommandSource<T>> CommandResultCallback decorateOutputIfNeeded(
        T param0, FunctionCommand.Callbacks<T> param1, ResourceLocation param2, CommandResultCallback param3
    ) {
        return param0.isSilent() ? param3 : (param4, param5) -> {
            param1.signalResult(param0, param2, param5);
            param3.onSuccess(param5);
        };
    }

    private static <T extends ExecutionCommandSource<T>> void queueFunctionsAsReturn(
        Collection<CommandFunction<T>> param0,
        @Nullable CompoundTag param1,
        T param2,
        T param3,
        ExecutionControl<T> param4,
        FunctionCommand.Callbacks<T> param5
    ) throws CommandSyntaxException {
        CommandDispatcher<T> var0 = param2.dispatcher();
        T var1 = param3.clearCallbacks();
        CommandResultCallback var2 = CommandResultCallback.chain(param2.callback(), param4.currentFrame().returnValueConsumer());

        for(CommandFunction<T> var3 : param0) {
            ResourceLocation var4 = var3.id();
            CommandResultCallback var5 = decorateOutputIfNeeded(param2, param5, var4, var2);
            instantiateAndQueueFunctions(param1, param4, var0, var1, var3, var4, var5, true);
        }

        if (var2 != CommandResultCallback.EMPTY) {
            param4.queueNext(FallthroughTask.instance());
        }
    }

    private static <T extends ExecutionCommandSource<T>> void queueFunctionsNoReturn(
        Collection<CommandFunction<T>> param0,
        @Nullable CompoundTag param1,
        T param2,
        T param3,
        ExecutionControl<T> param4,
        FunctionCommand.Callbacks<T> param5
    ) throws CommandSyntaxException {
        CommandDispatcher<T> var0 = param2.dispatcher();
        T var1 = param3.clearCallbacks();
        CommandResultCallback var2 = param2.callback();
        if (!param0.isEmpty()) {
            if (param0.size() == 1) {
                CommandFunction<T> var3 = param0.iterator().next();
                ResourceLocation var4 = var3.id();
                CommandResultCallback var5 = decorateOutputIfNeeded(param2, param5, var4, var2);
                instantiateAndQueueFunctions(param1, param4, var0, var1, var3, var4, var5, false);
            } else if (var2 == CommandResultCallback.EMPTY) {
                for(CommandFunction<T> var6 : param0) {
                    ResourceLocation var7 = var6.id();
                    CommandResultCallback var8 = decorateOutputIfNeeded(param2, param5, var7, var2);
                    instantiateAndQueueFunctions(param1, param4, var0, var1, var6, var7, var8, false);
                }
            } else {
                class Accumulator {
                    boolean anyResult;
                    int sum;

                    public void add(int param0) {
                        this.anyResult = true;
                        this.sum += param0;
                    }
                }

                Accumulator var9 = new Accumulator();
                CommandResultCallback var10 = (param1x, param2x) -> var9.add(param2x);

                for(CommandFunction<T> var11 : param0) {
                    ResourceLocation var12 = var11.id();
                    CommandResultCallback var13 = decorateOutputIfNeeded(param2, param5, var12, var10);
                    instantiateAndQueueFunctions(param1, param4, var0, var1, var11, var12, var13, false);
                }

                param4.queueNext((param2x, param3x) -> {
                    if (var9.anyResult) {
                        var2.onSuccess(var9.sum);
                    }

                });
            }

        }
    }

    public interface Callbacks<T> {
        void signalResult(T var1, ResourceLocation var2, int var3);
    }

    abstract static class FunctionCustomExecutor
        extends CustomCommandExecutor.WithErrorHandling<CommandSourceStack>
        implements CustomCommandExecutor.CommandAdapter<CommandSourceStack> {
        @Nullable
        protected abstract CompoundTag arguments(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

        public void runGuarded(
            CommandSourceStack param0, ContextChain<CommandSourceStack> param1, ChainModifiers param2, ExecutionControl<CommandSourceStack> param3
        ) throws CommandSyntaxException {
            CommandContext<CommandSourceStack> var0 = param1.getTopContext().copyFor(param0);
            Pair<ResourceLocation, Collection<CommandFunction<CommandSourceStack>>> var1 = FunctionArgument.getFunctionOrTag(var0, "name")
                .mapSecond(param0x -> param0x.map(Collections::singleton, Function.identity()));
            Collection<CommandFunction<CommandSourceStack>> var2 = var1.getSecond();
            if (var2.isEmpty()) {
                throw FunctionCommand.ERROR_NO_FUNCTIONS.create(Component.translationArg(var1.getFirst()));
            } else {
                CompoundTag var3 = this.arguments(var0);
                CommandSourceStack var4 = FunctionCommand.modifySenderForExecution(param0);
                if (var2.size() == 1) {
                    param0.sendSuccess(
                        () -> Component.translatable("commands.function.scheduled.single", Component.translationArg(var2.iterator().next().id())), true
                    );
                } else {
                    param0.sendSuccess(
                        () -> Component.translatable(
                                "commands.function.scheduled.multiple",
                                ComponentUtils.formatList(var2.stream().map(CommandFunction::id).toList(), Component::translationArg)
                            ),
                        true
                    );
                }

                FunctionCommand.queueFunctions(var2, var3, param0, var4, param3, FunctionCommand.FULL_CONTEXT_CALLBACKS, param2);
            }
        }
    }
}
