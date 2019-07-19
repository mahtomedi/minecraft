package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;

public class EmoteCommands {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("me")
                .then(
                    Commands.argument("action", StringArgumentType.greedyString())
                        .executes(
                            param0x -> {
                                param0x.getSource()
                                    .getServer()
                                    .getPlayerList()
                                    .broadcastMessage(
                                        new TranslatableComponent(
                                            "chat.type.emote", param0x.getSource().getDisplayName(), StringArgumentType.getString(param0x, "action")
                                        )
                                    );
                                return 1;
                            }
                        )
                )
        );
    }
}
