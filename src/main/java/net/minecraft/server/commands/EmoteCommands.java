package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;

public class EmoteCommands {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("me")
                .then(
                    Commands.argument("action", StringArgumentType.greedyString())
                        .executes(
                            param0x -> {
                                TranslatableComponent var0x = new TranslatableComponent(
                                    "chat.type.emote", param0x.getSource().getDisplayName(), StringArgumentType.getString(param0x, "action")
                                );
                                Entity var1 = param0x.getSource().getEntity();
                                if (var1 != null) {
                                    param0x.getSource().getServer().getPlayerList().broadcastMessage(var0x, ChatType.CHAT, var1.getUUID());
                                } else {
                                    param0x.getSource().getServer().getPlayerList().broadcastMessage(var0x, ChatType.SYSTEM, Util.NIL_UUID);
                                }
                    
                                return 1;
                            }
                        )
                )
        );
    }
}
