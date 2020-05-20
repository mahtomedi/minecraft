package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;

public class TellRawCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("tellraw")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("targets", EntityArgument.players())
                        .then(
                            Commands.argument("message", ComponentArgument.textComponent())
                                .executes(
                                    param0x -> {
                                        int var0x = 0;
                            
                                        for(ServerPlayer var1 : EntityArgument.getPlayers(param0x, "targets")) {
                                            var1.sendMessage(
                                                ComponentUtils.updateForEntity(param0x.getSource(), ComponentArgument.getComponent(param0x, "message"), var1, 0),
                                                Util.NIL_UUID
                                            );
                                            ++var0x;
                                        }
                            
                                        return var0x;
                                    }
                                )
                        )
                )
        );
    }
}
