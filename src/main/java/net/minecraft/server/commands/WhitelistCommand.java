package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;

public class WhitelistCommand {
    private static final SimpleCommandExceptionType ERROR_ALREADY_ENABLED = new SimpleCommandExceptionType(
        Component.translatable("commands.whitelist.alreadyOn")
    );
    private static final SimpleCommandExceptionType ERROR_ALREADY_DISABLED = new SimpleCommandExceptionType(
        Component.translatable("commands.whitelist.alreadyOff")
    );
    private static final SimpleCommandExceptionType ERROR_ALREADY_WHITELISTED = new SimpleCommandExceptionType(
        Component.translatable("commands.whitelist.add.failed")
    );
    private static final SimpleCommandExceptionType ERROR_NOT_WHITELISTED = new SimpleCommandExceptionType(
        Component.translatable("commands.whitelist.remove.failed")
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("whitelist")
                .requires(param0x -> param0x.hasPermission(3))
                .then(Commands.literal("on").executes(param0x -> enableWhitelist(param0x.getSource())))
                .then(Commands.literal("off").executes(param0x -> disableWhitelist(param0x.getSource())))
                .then(Commands.literal("list").executes(param0x -> showList(param0x.getSource())))
                .then(
                    Commands.literal("add")
                        .then(
                            Commands.argument("targets", GameProfileArgument.gameProfile())
                                .suggests(
                                    (param0x, param1) -> {
                                        PlayerList var0x = param0x.getSource().getServer().getPlayerList();
                                        return SharedSuggestionProvider.suggest(
                                            var0x.getPlayers()
                                                .stream()
                                                .filter(param1x -> !var0x.getWhiteList().isWhiteListed(param1x.getGameProfile()))
                                                .map(param0xx -> param0xx.getGameProfile().getName()),
                                            param1
                                        );
                                    }
                                )
                                .executes(param0x -> addPlayers(param0x.getSource(), GameProfileArgument.getGameProfiles(param0x, "targets")))
                        )
                )
                .then(
                    Commands.literal("remove")
                        .then(
                            Commands.argument("targets", GameProfileArgument.gameProfile())
                                .suggests(
                                    (param0x, param1) -> SharedSuggestionProvider.suggest(
                                            param0x.getSource().getServer().getPlayerList().getWhiteListNames(), param1
                                        )
                                )
                                .executes(param0x -> removePlayers(param0x.getSource(), GameProfileArgument.getGameProfiles(param0x, "targets")))
                        )
                )
                .then(Commands.literal("reload").executes(param0x -> reload(param0x.getSource())))
        );
    }

    private static int reload(CommandSourceStack param0) {
        param0.getServer().getPlayerList().reloadWhiteList();
        param0.sendSuccess(() -> Component.translatable("commands.whitelist.reloaded"), true);
        param0.getServer().kickUnlistedPlayers(param0);
        return 1;
    }

    private static int addPlayers(CommandSourceStack param0, Collection<GameProfile> param1) throws CommandSyntaxException {
        UserWhiteList var0 = param0.getServer().getPlayerList().getWhiteList();
        int var1 = 0;

        for(GameProfile var2 : param1) {
            if (!var0.isWhiteListed(var2)) {
                UserWhiteListEntry var3 = new UserWhiteListEntry(var2);
                var0.add(var3);
                param0.sendSuccess(() -> Component.translatable("commands.whitelist.add.success", ComponentUtils.getDisplayName(var2)), true);
                ++var1;
            }
        }

        if (var1 == 0) {
            throw ERROR_ALREADY_WHITELISTED.create();
        } else {
            return var1;
        }
    }

    private static int removePlayers(CommandSourceStack param0, Collection<GameProfile> param1) throws CommandSyntaxException {
        UserWhiteList var0 = param0.getServer().getPlayerList().getWhiteList();
        int var1 = 0;

        for(GameProfile var2 : param1) {
            if (var0.isWhiteListed(var2)) {
                UserWhiteListEntry var3 = new UserWhiteListEntry(var2);
                var0.remove(var3);
                param0.sendSuccess(() -> Component.translatable("commands.whitelist.remove.success", ComponentUtils.getDisplayName(var2)), true);
                ++var1;
            }
        }

        if (var1 == 0) {
            throw ERROR_NOT_WHITELISTED.create();
        } else {
            param0.getServer().kickUnlistedPlayers(param0);
            return var1;
        }
    }

    private static int enableWhitelist(CommandSourceStack param0) throws CommandSyntaxException {
        PlayerList var0 = param0.getServer().getPlayerList();
        if (var0.isUsingWhitelist()) {
            throw ERROR_ALREADY_ENABLED.create();
        } else {
            var0.setUsingWhiteList(true);
            param0.sendSuccess(() -> Component.translatable("commands.whitelist.enabled"), true);
            param0.getServer().kickUnlistedPlayers(param0);
            return 1;
        }
    }

    private static int disableWhitelist(CommandSourceStack param0) throws CommandSyntaxException {
        PlayerList var0 = param0.getServer().getPlayerList();
        if (!var0.isUsingWhitelist()) {
            throw ERROR_ALREADY_DISABLED.create();
        } else {
            var0.setUsingWhiteList(false);
            param0.sendSuccess(() -> Component.translatable("commands.whitelist.disabled"), true);
            return 1;
        }
    }

    private static int showList(CommandSourceStack param0) {
        String[] var0 = param0.getServer().getPlayerList().getWhiteListNames();
        if (var0.length == 0) {
            param0.sendSuccess(() -> Component.translatable("commands.whitelist.none"), false);
        } else {
            param0.sendSuccess(() -> Component.translatable("commands.whitelist.list", var0.length, String.join(", ", var0)), false);
        }

        return var0.length;
    }
}
