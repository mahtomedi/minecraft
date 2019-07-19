package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.ServerFunctionManager;

public class FunctionCommand {
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_FUNCTION = (param0, param1) -> {
        ServerFunctionManager var0 = param0.getSource().getServer().getFunctions();
        SharedSuggestionProvider.suggestResource(var0.getTags().getAvailableTags(), param1, "#");
        return SharedSuggestionProvider.suggestResource(var0.getFunctions().keySet(), param1);
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

        for(CommandFunction var1 : param1) {
            var0 += param0.getServer().getFunctions().execute(var1, param0.withSuppressedOutput().withMaximumPermission(2));
        }

        if (param1.size() == 1) {
            param0.sendSuccess(new TranslatableComponent("commands.function.success.single", var0, param1.iterator().next().getId()), true);
        } else {
            param0.sendSuccess(new TranslatableComponent("commands.function.success.multiple", var0, param1.size()), true);
        }

        return var0;
    }
}
