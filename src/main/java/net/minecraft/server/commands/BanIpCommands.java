package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;

public class BanIpCommands {
    public static final Pattern IP_ADDRESS_PATTERN = Pattern.compile(
        "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
    );
    private static final SimpleCommandExceptionType ERROR_INVALID_IP = new SimpleCommandExceptionType(new TranslatableComponent("commands.banip.invalid"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(new TranslatableComponent("commands.banip.failed"));

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
        Matcher var0 = IP_ADDRESS_PATTERN.matcher(param1);
        if (var0.matches()) {
            return banIp(param0, param1, param2);
        } else {
            ServerPlayer var1 = param0.getServer().getPlayerList().getPlayerByName(param1);
            if (var1 != null) {
                return banIp(param0, var1.getIpAddress(), param2);
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
            param0.sendSuccess(new TranslatableComponent("commands.banip.success", param1, var2.getReason()), true);
            if (!var1.isEmpty()) {
                param0.sendSuccess(new TranslatableComponent("commands.banip.info", var1.size(), EntitySelector.joinNames(var1)), true);
            }

            for(ServerPlayer var3 : var1) {
                var3.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.ip_banned"));
            }

            return var1.size();
        }
    }
}
