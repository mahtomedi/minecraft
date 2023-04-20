package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.OptionalInt;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerFunctionManager;
import org.apache.commons.lang3.mutable.MutableObject;

public class FunctionCommand {
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_FUNCTION = (param0, param1) -> {
        ServerFunctionManager var0 = param0.getSource().getServer().getFunctions();
        SharedSuggestionProvider.suggestResource(var0.getTagNames(), param1, "#");
        return SharedSuggestionProvider.suggestResource(var0.getFunctionNames(), param1);
    };

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("function")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("name", FunctionArgument.functions())
                        .suggests(SUGGEST_FUNCTION)
                        .executes(param0x -> runFunction(param0x.getSource(), FunctionArgument.getFunctions(param0x, "name")))
                )
        );
    }

    private static int runFunction(CommandSourceStack param0, Collection<CommandFunction> param1) {
        int var0 = 0;
        boolean var1 = false;

        for(CommandFunction var2 : param1) {
            MutableObject<OptionalInt> var3 = new MutableObject<>(OptionalInt.empty());
            int var4 = param0.getServer()
                .getFunctions()
                .execute(
                    var2, param0.withSuppressedOutput().withMaximumPermission(2).withReturnValueConsumer(param1x -> var3.setValue(OptionalInt.of(param1x)))
                );
            OptionalInt var5 = var3.getValue();
            var0 += var5.orElse(var4);
            var1 |= var5.isPresent();
        }

        if (param1.size() == 1) {
            if (var1) {
                param0.sendSuccess(Component.translatable("commands.function.success.single.result", var0, param1.iterator().next().getId()), true);
            } else {
                param0.sendSuccess(Component.translatable("commands.function.success.single", var0, param1.iterator().next().getId()), true);
            }
        } else if (var1) {
            param0.sendSuccess(Component.translatable("commands.function.success.multiple.result", param1.size()), true);
        } else {
            param0.sendSuccess(Component.translatable("commands.function.success.multiple", var0, param1.size()), true);
        }

        return var0;
    }
}
