package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class EmoteCommands {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("me")
                .then(
                    Commands.argument("action", StringArgumentType.greedyString())
                        .executes(
                            param0x -> {
                                String var0x = StringArgumentType.getString(param0x, "action");
                                Entity var1 = param0x.getSource().getEntity();
                                MinecraftServer var2 = param0x.getSource().getServer();
                                if (var1 != null) {
                                    if (var1 instanceof ServerPlayer var3) {
                                        var3.getTextFilter()
                                            .processStreamMessage(var0x)
                                            .thenAcceptAsync(
                                                param4 -> {
                                                    String var0xx = param4.getFiltered();
                                                    Component var1x = var0xx.isEmpty() ? null : createMessage(param0x, var0xx);
                                                    Component var2x = createMessage(param0x, param4.getRaw());
                                                    var2.getPlayerList()
                                                        .broadcastMessage(
                                                            var2x,
                                                            param3x -> var3.shouldFilterMessageTo(param3x) ? var1x : var2x,
                                                            ChatType.CHAT,
                                                            var1.getUUID()
                                                        );
                                                },
                                                var2
                                            );
                                        return 1;
                                    }
                    
                                    var2.getPlayerList().broadcastMessage(createMessage(param0x, var0x), ChatType.CHAT, var1.getUUID());
                                } else {
                                    var2.getPlayerList().broadcastMessage(createMessage(param0x, var0x), ChatType.SYSTEM, Util.NIL_UUID);
                                }
                    
                                return 1;
                            }
                        )
                )
        );
    }

    private static Component createMessage(CommandContext<CommandSourceStack> param0, String param1) {
        return new TranslatableComponent("chat.type.emote", param0.getSource().getDisplayName(), param1);
    }
}
