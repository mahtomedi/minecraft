package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

public class DebugConfigCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("debugconfig")
                .requires(param0x -> param0x.hasPermission(3))
                .then(
                    Commands.literal("config")
                        .then(
                            Commands.argument("target", EntityArgument.player())
                                .executes(param0x -> config(param0x.getSource(), EntityArgument.getPlayer(param0x, "target")))
                        )
                )
                .then(
                    Commands.literal("unconfig")
                        .then(
                            Commands.argument("target", UuidArgument.uuid())
                                .suggests((param0x, param1) -> SharedSuggestionProvider.suggest(getUuidsInConfig(param0x.getSource().getServer()), param1))
                                .executes(param0x -> unconfig(param0x.getSource(), UuidArgument.getUuid(param0x, "target")))
                        )
                )
        );
    }

    private static Iterable<String> getUuidsInConfig(MinecraftServer param0) {
        Set<String> var0 = new HashSet<>();

        for(Connection var1 : param0.getConnection().getConnections()) {
            PacketListener var5 = var1.getPacketListener();
            if (var5 instanceof ServerConfigurationPacketListenerImpl var2) {
                var0.add(var2.getOwner().getId().toString());
            }
        }

        return var0;
    }

    private static int config(CommandSourceStack param0, ServerPlayer param1) {
        GameProfile var0 = param1.getGameProfile();
        param1.connection.switchToConfig();
        param0.sendSuccess(() -> Component.literal("Switched player " + var0.getName() + "(" + var0.getId() + ") to config mode"), false);
        return 1;
    }

    private static int unconfig(CommandSourceStack param0, UUID param1) {
        for(Connection var0 : param0.getServer().getConnection().getConnections()) {
            PacketListener var5 = var0.getPacketListener();
            if (var5 instanceof ServerConfigurationPacketListenerImpl var1 && var1.getOwner().getId().equals(param1)) {
                var1.returnToWorld();
            }
        }

        param0.sendFailure(Component.literal("Can't find player to unconfig"));
        return 0;
    }
}
