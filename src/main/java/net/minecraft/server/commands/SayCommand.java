package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;

public class SayCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("say")
                .requires(param0x -> param0x.hasPermission(2))
                .then(Commands.argument("message", MessageArgument.message()).executes(param0x -> {
                    Component var0x = MessageArgument.getMessage(param0x, "message");
                    Component var1 = new TranslatableComponent("chat.type.announcement", param0x.getSource().getDisplayName(), var0x);
                    Entity var2 = param0x.getSource().getEntity();
                    if (var2 != null) {
                        param0x.getSource().getServer().getPlayerList().broadcastMessage(var1, ChatType.CHAT, var2.getUUID());
                    } else {
                        param0x.getSource().getServer().getPlayerList().broadcastMessage(var1, ChatType.SYSTEM, Util.NIL_UUID);
                    }
        
                    return 1;
                }))
        );
    }
}
