package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import java.util.List;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;

public class ListPlayersCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("list")
                .executes(param0x -> listPlayers(param0x.getSource()))
                .then(Commands.literal("uuids").executes(param0x -> listPlayersWithUuids(param0x.getSource())))
        );
    }

    private static int listPlayers(CommandSourceStack param0) {
        return format(param0, Player::getDisplayName);
    }

    private static int listPlayersWithUuids(CommandSourceStack param0) {
        return format(param0, param0x -> Component.translatable("commands.list.nameAndId", param0x.getName(), param0x.getGameProfile().getId()));
    }

    private static int format(CommandSourceStack param0, Function<ServerPlayer, Component> param1) {
        PlayerList var0 = param0.getServer().getPlayerList();
        List<ServerPlayer> var1 = var0.getPlayers();
        Component var2 = ComponentUtils.formatList(var1, param1);
        param0.sendSuccess(Component.translatable("commands.list.players", var1.size(), var0.getMaxPlayers(), var2), false);
        return var1.size();
    }
}
