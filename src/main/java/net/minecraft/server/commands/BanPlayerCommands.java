package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;

public class BanPlayerCommands {
    private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.ban.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("ban")
                .requires(param0x -> param0x.hasPermission(3))
                .then(
                    Commands.argument("targets", GameProfileArgument.gameProfile())
                        .executes(param0x -> banPlayers(param0x.getSource(), GameProfileArgument.getGameProfiles(param0x, "targets"), null))
                        .then(
                            Commands.argument("reason", MessageArgument.message())
                                .executes(
                                    param0x -> banPlayers(
                                            param0x.getSource(),
                                            GameProfileArgument.getGameProfiles(param0x, "targets"),
                                            MessageArgument.getMessage(param0x, "reason")
                                        )
                                )
                        )
                )
        );
    }

    private static int banPlayers(CommandSourceStack param0, Collection<GameProfile> param1, @Nullable Component param2) throws CommandSyntaxException {
        UserBanList var0 = param0.getServer().getPlayerList().getBans();
        int var1 = 0;

        for(GameProfile var2 : param1) {
            if (!var0.isBanned(var2)) {
                UserBanListEntry var3 = new UserBanListEntry(var2, null, param0.getTextName(), null, param2 == null ? null : param2.getString());
                var0.add(var3);
                ++var1;
                param0.sendSuccess(() -> Component.translatable("commands.ban.success", ComponentUtils.getDisplayName(var2), var3.getReason()), true);
                ServerPlayer var4 = param0.getServer().getPlayerList().getPlayer(var2.getId());
                if (var4 != null) {
                    var4.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
                }
            }
        }

        if (var1 == 0) {
            throw ERROR_ALREADY_BANNED.create();
        } else {
            return var1;
        }
    }
}
