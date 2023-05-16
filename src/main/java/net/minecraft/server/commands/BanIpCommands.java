package net.minecraft.server.commands;

import com.google.common.net.InetAddresses;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;

public class BanIpCommands {
    private static final SimpleCommandExceptionType ERROR_INVALID_IP = new SimpleCommandExceptionType(Component.translatable("commands.banip.invalid"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.banip.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("ban-ip")
                .requires(param0x -> param0x.hasPermission(3))
                .then(
                    Commands.argument("target", StringArgumentType.word())
                        .executes(param0x -> banIpOrName(param0x.getSource(), StringArgumentType.getString(param0x, "target"), null))
                        .then(
                            Commands.argument("reason", MessageArgument.message())
                                .executes(
                                    param0x -> banIpOrName(
                                            param0x.getSource(), StringArgumentType.getString(param0x, "target"), MessageArgument.getMessage(param0x, "reason")
                                        )
                                )
                        )
                )
        );
    }

    private static int banIpOrName(CommandSourceStack param0, String param1, @Nullable Component param2) throws CommandSyntaxException {
        if (InetAddresses.isInetAddress(param1)) {
            return banIp(param0, param1, param2);
        } else {
            ServerPlayer var0 = param0.getServer().getPlayerList().getPlayerByName(param1);
            if (var0 != null) {
                return banIp(param0, var0.getIpAddress(), param2);
            } else {
                throw ERROR_INVALID_IP.create();
            }
        }
    }

    private static int banIp(CommandSourceStack param0, String param1, @Nullable Component param2) throws CommandSyntaxException {
        IpBanList var0 = param0.getServer().getPlayerList().getIpBans();
        if (var0.isBanned(param1)) {
            throw ERROR_ALREADY_BANNED.create();
        } else {
            List<ServerPlayer> var1 = param0.getServer().getPlayerList().getPlayersWithAddress(param1);
            IpBanListEntry var2 = new IpBanListEntry(param1, null, param0.getTextName(), null, param2 == null ? null : param2.getString());
            var0.add(var2);
            param0.sendSuccess(() -> Component.translatable("commands.banip.success", param1, var2.getReason()), true);
            if (!var1.isEmpty()) {
                param0.sendSuccess(() -> Component.translatable("commands.banip.info", var1.size(), EntitySelector.joinNames(var1)), true);
            }

            for(ServerPlayer var3 : var1) {
                var3.connection.disconnect(Component.translatable("multiplayer.disconnect.ip_banned"));
            }

            return var1.size();
        }
    }
}
