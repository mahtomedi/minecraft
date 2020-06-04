package net.minecraft.server.commands;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.players.BanListEntry;
import net.minecraft.server.players.PlayerList;

public class BanListCommands {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("banlist")
                .requires(param0x -> param0x.hasPermission(3))
                .executes(param0x -> {
                    PlayerList var0x = param0x.getSource().getServer().getPlayerList();
                    return showList(param0x.getSource(), Lists.newArrayList(Iterables.concat(var0x.getBans().getEntries(), var0x.getIpBans().getEntries())));
                })
                .then(
                    Commands.literal("ips")
                        .executes(param0x -> showList(param0x.getSource(), param0x.getSource().getServer().getPlayerList().getIpBans().getEntries()))
                )
                .then(
                    Commands.literal("players")
                        .executes(param0x -> showList(param0x.getSource(), param0x.getSource().getServer().getPlayerList().getBans().getEntries()))
                )
        );
    }

    private static int showList(CommandSourceStack param0, Collection<? extends BanListEntry<?>> param1) {
        if (param1.isEmpty()) {
            param0.sendSuccess(new TranslatableComponent("commands.banlist.none"), false);
        } else {
            param0.sendSuccess(new TranslatableComponent("commands.banlist.list", param1.size()), false);

            for(BanListEntry<?> var0 : param1) {
                param0.sendSuccess(new TranslatableComponent("commands.banlist.entry", var0.getDisplayName(), var0.getSource(), var0.getReason()), false);
            }
        }

        return param1.size();
    }
}
