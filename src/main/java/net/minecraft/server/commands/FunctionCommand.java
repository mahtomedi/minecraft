package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import org.apache.commons.lang3.mutable.MutableObject;

public class FunctionCommand {
    private static final DynamicCommandExceptionType ERROR_ARGUMENT_NOT_COMPOUND = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.function.error.argument_not_compound", param0)
    );
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_FUNCTION = (param0, param1) -> {
        ServerFunctionManager var0 = param0.getSource().getServer().getFunctions();
        SharedSuggestionProvider.suggestResource(var0.getTagNames(), param1, "#");
        return SharedSuggestionProvider.suggestResource(var0.getFunctionNames(), param1);
    };

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        LiteralArgumentBuilder<CommandSourceStack> var0 = Commands.literal("with");

        for(DataCommands.DataProvider var1 : DataCommands.SOURCE_PROVIDERS) {
            var1.wrap(
                var0,
                param1 -> param1.executes(
                            param1x -> runFunction(param1x.getSource(), FunctionArgument.getFunctions(param1x, "name"), var1.access(param1x).getData())
                        )
                        .then(
                            Commands.argument("path", NbtPathArgument.nbtPath())
                                .executes(
                                    param1x -> runFunction(
                                            param1x.getSource(),
                                            FunctionArgument.getFunctions(param1x, "name"),
                                            getArgumentTag(NbtPathArgument.getPath(param1x, "path"), var1.access(param1x))
                                        )
                                )
                        )
            );
        }

        param0.register(
            Commands.literal("function")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("name", FunctionArgument.functions())
                        .suggests(SUGGEST_FUNCTION)
                        .executes(param0x -> runFunction(param0x.getSource(), FunctionArgument.getFunctions(param0x, "name"), null))
                        .then(
                            Commands.argument("arguments", CompoundTagArgument.compoundTag())
                                .executes(
                                    param0x -> runFunction(
                                            param0x.getSource(),
                                            FunctionArgument.getFunctions(param0x, "name"),
                                            CompoundTagArgument.getCompoundTag(param0x, "arguments")
                                        )
                                )
                        )
                        .then(var0)
                )
        );
    }

    private static CompoundTag getArgumentTag(NbtPathArgument.NbtPath param0, DataAccessor param1) throws CommandSyntaxException {
        Tag var0 = DataCommands.getSingleTag(param0, param1);
        if (var0 instanceof CompoundTag) {
            return (CompoundTag)var0;
        } else {
            throw ERROR_ARGUMENT_NOT_COMPOUND.create(var0.getType().getName());
        }
    }

    private static int runFunction(CommandSourceStack param0, Collection<CommandFunction> param1, @Nullable CompoundTag param2) {
        int var0 = 0;
        boolean var1 = false;
        boolean var2 = false;

        for(CommandFunction var3 : param1) {
            try {
                FunctionCommand.FunctionResult var4 = runFunction(param0, var3, param2);
                var0 += var4.value();
                var1 |= var4.isReturn();
                var2 = true;
            } catch (FunctionInstantiationException var9) {
                param0.sendFailure(var9.messageComponent());
            }
        }

        if (var2) {
            int var6 = var0;
            if (param1.size() == 1) {
                if (var1) {
                    param0.sendSuccess(() -> Component.translatable("commands.function.success.single.result", var6, param1.iterator().next().getId()), true);
                } else {
                    param0.sendSuccess(() -> Component.translatable("commands.function.success.single", var6, param1.iterator().next().getId()), true);
                }
            } else if (var1) {
                param0.sendSuccess(() -> Component.translatable("commands.function.success.multiple.result", param1.size()), true);
            } else {
                param0.sendSuccess(() -> Component.translatable("commands.function.success.multiple", var6, param1.size()), true);
            }
        }

        return var0;
    }

    public static FunctionCommand.FunctionResult runFunction(CommandSourceStack param0, CommandFunction param1, @Nullable CompoundTag param2) throws FunctionInstantiationException {
        MutableObject<FunctionCommand.FunctionResult> var0 = new MutableObject<>();
        int var1 = param0.getServer()
            .getFunctions()
            .execute(
                param1,
                param0.withSuppressedOutput()
                    .withMaximumPermission(2)
                    .withReturnValueConsumer(param1x -> var0.setValue(new FunctionCommand.FunctionResult(param1x, true))),
                null,
                param2
            );
        FunctionCommand.FunctionResult var2 = var0.getValue();
        return var2 != null ? var2 : new FunctionCommand.FunctionResult(var1, false);
    }

    public static record FunctionResult(int value, boolean isReturn) {
    }
}
