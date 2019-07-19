package net.minecraft.server.commands;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class HelpCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.help.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("help")
                .executes(param1 -> {
                    Map<CommandNode<CommandSourceStack>, String> var0x = param0.getSmartUsage(param0.getRoot(), param1.getSource());
        
                    for(String var1 : var0x.values()) {
                        param1.getSource().sendSuccess(new TextComponent("/" + var1), false);
                    }
        
                    return var0x.size();
                })
                .then(
                    Commands.argument("command", StringArgumentType.greedyString())
                        .executes(
                            param1 -> {
                                ParseResults<CommandSourceStack> var0x = param0.parse(StringArgumentType.getString(param1, "command"), param1.getSource());
                                if (var0x.getContext().getNodes().isEmpty()) {
                                    throw ERROR_FAILED.create();
                                } else {
                                    Map<CommandNode<CommandSourceStack>, String> var1 = param0.getSmartUsage(
                                        Iterables.getLast(var0x.getContext().getNodes()).getNode(), param1.getSource()
                                    );
                    
                                    for(String var2 : var1.values()) {
                                        param1.getSource().sendSuccess(new TextComponent("/" + var0x.getReader().getString() + " " + var2), false);
                                    }
                    
                                    return var1.size();
                                }
                            }
                        )
                )
        );
    }
}
