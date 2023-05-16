package net.minecraft.server.commands;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.chase.ChaseClient;
import net.minecraft.server.chase.ChaseServer;
import net.minecraft.world.level.Level;

public class ChaseCommand {
    private static final String DEFAULT_CONNECT_HOST = "localhost";
    private static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";
    private static final int DEFAULT_PORT = 10000;
    private static final int BROADCAST_INTERVAL_MS = 100;
    public static BiMap<String, ResourceKey<Level>> DIMENSION_NAMES = ImmutableBiMap.of("o", Level.OVERWORLD, "n", Level.NETHER, "e", Level.END);
    @Nullable
    private static ChaseServer chaseServer;
    @Nullable
    private static ChaseClient chaseClient;

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("chase")
                .then(
                    Commands.literal("follow")
                        .then(
                            Commands.argument("host", StringArgumentType.string())
                                .executes(param0x -> follow(param0x.getSource(), StringArgumentType.getString(param0x, "host"), 10000))
                                .then(
                                    Commands.argument("port", IntegerArgumentType.integer(1, 65535))
                                        .executes(
                                            param0x -> follow(
                                                    param0x.getSource(),
                                                    StringArgumentType.getString(param0x, "host"),
                                                    IntegerArgumentType.getInteger(param0x, "port")
                                                )
                                        )
                                )
                        )
                        .executes(param0x -> follow(param0x.getSource(), "localhost", 10000))
                )
                .then(
                    Commands.literal("lead")
                        .then(
                            Commands.argument("bind_address", StringArgumentType.string())
                                .executes(param0x -> lead(param0x.getSource(), StringArgumentType.getString(param0x, "bind_address"), 10000))
                                .then(
                                    Commands.argument("port", IntegerArgumentType.integer(1024, 65535))
                                        .executes(
                                            param0x -> lead(
                                                    param0x.getSource(),
                                                    StringArgumentType.getString(param0x, "bind_address"),
                                                    IntegerArgumentType.getInteger(param0x, "port")
                                                )
                                        )
                                )
                        )
                        .executes(param0x -> lead(param0x.getSource(), "0.0.0.0", 10000))
                )
                .then(Commands.literal("stop").executes(param0x -> stop(param0x.getSource())))
        );
    }

    private static int stop(CommandSourceStack param0) {
        if (chaseClient != null) {
            chaseClient.stop();
            param0.sendSuccess(() -> Component.literal("You have now stopped chasing"), false);
            chaseClient = null;
        }

        if (chaseServer != null) {
            chaseServer.stop();
            param0.sendSuccess(() -> Component.literal("You are no longer being chased"), false);
            chaseServer = null;
        }

        return 0;
    }

    private static boolean alreadyRunning(CommandSourceStack param0) {
        if (chaseServer != null) {
            param0.sendFailure(Component.literal("Chase server is already running. Stop it using /chase stop"));
            return true;
        } else if (chaseClient != null) {
            param0.sendFailure(Component.literal("You are already chasing someone. Stop it using /chase stop"));
            return true;
        } else {
            return false;
        }
    }

    private static int lead(CommandSourceStack param0, String param1, int param2) {
        if (alreadyRunning(param0)) {
            return 0;
        } else {
            chaseServer = new ChaseServer(param1, param2, param0.getServer().getPlayerList(), 100);

            try {
                chaseServer.start();
                param0.sendSuccess(
                    () -> Component.literal("Chase server is now running on port " + param2 + ". Clients can follow you using /chase follow <ip> <port>"),
                    false
                );
            } catch (IOException var4) {
                var4.printStackTrace();
                param0.sendFailure(Component.literal("Failed to start chase server on port " + param2));
                chaseServer = null;
            }

            return 0;
        }
    }

    private static int follow(CommandSourceStack param0, String param1, int param2) {
        if (alreadyRunning(param0)) {
            return 0;
        } else {
            chaseClient = new ChaseClient(param1, param2, param0.getServer());
            chaseClient.start();
            param0.sendSuccess(
                () -> Component.literal(
                        "You are now chasing "
                            + param1
                            + ":"
                            + param2
                            + ". If that server does '/chase lead' then you will automatically go to the same position. Use '/chase stop' to stop chasing."
                    ),
                false
            );
            return 0;
        }
    }
}
