package net.minecraft.server.chase;

import com.google.common.base.Charsets;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ChaseCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class ChaseClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int RECONNECT_INTERVAL_SECONDS = 5;
    private final String serverHost;
    private final int serverPort;
    private final MinecraftServer server;
    private volatile boolean wantsToRun;
    @Nullable
    private Socket socket;
    @Nullable
    private Thread thread;

    public ChaseClient(String param0, int param1, MinecraftServer param2) {
        this.serverHost = param0;
        this.serverPort = param1;
        this.server = param2;
    }

    public void start() {
        if (this.thread != null && this.thread.isAlive()) {
            LOGGER.warn("Remote control client was asked to start, but it is already running. Will ignore.");
        }

        this.wantsToRun = true;
        this.thread = new Thread(this::run, "chase-client");
        this.thread.setDaemon(true);
        this.thread.start();
    }

    public void stop() {
        this.wantsToRun = false;
        IOUtils.closeQuietly(this.socket);
        this.socket = null;
        this.thread = null;
    }

    public void run() {
        String var0 = this.serverHost + ":" + this.serverPort;

        while(this.wantsToRun) {
            try {
                LOGGER.info("Connecting to remote control server {}", var0);
                this.socket = new Socket(this.serverHost, this.serverPort);
                LOGGER.info("Connected to remote control server! Will continuously execute the command broadcasted by that server.");

                try (BufferedReader var1 = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), Charsets.US_ASCII))) {
                    while(this.wantsToRun) {
                        String var2 = var1.readLine();
                        if (var2 == null) {
                            LOGGER.warn("Lost connection to remote control server {}. Will retry in {}s.", var0, 5);
                            break;
                        }

                        this.handleMessage(var2);
                    }
                } catch (IOException var8) {
                    LOGGER.warn("Lost connection to remote control server {}. Will retry in {}s.", var0, 5);
                }
            } catch (IOException var9) {
                LOGGER.warn("Failed to connect to remote control server {}. Will retry in {}s.", var0, 5);
            }

            if (this.wantsToRun) {
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException var5) {
                }
            }
        }

    }

    private void handleMessage(String param0) {
        try (Scanner var0 = new Scanner(new StringReader(param0))) {
            var0.useLocale(Locale.ROOT);
            String var1 = var0.next();
            if ("t".equals(var1)) {
                this.handleTeleport(var0);
            } else {
                LOGGER.warn("Unknown message type '{}'", var1);
            }
        } catch (NoSuchElementException var7) {
            LOGGER.warn("Could not parse message '{}', ignoring", param0);
        }

    }

    private void handleTeleport(Scanner param0) {
        this.parseTarget(param0)
            .ifPresent(
                param0x -> this.executeCommand(
                        String.format(
                            Locale.ROOT,
                            "/execute in %s run tp @s %.3f %.3f %.3f %.3f %.3f",
                            param0x.level.location(),
                            param0x.pos.x,
                            param0x.pos.y,
                            param0x.pos.z,
                            param0x.rot.y,
                            param0x.rot.x
                        )
                    )
            );
    }

    private Optional<ChaseClient.TeleportTarget> parseTarget(Scanner param0) {
        ResourceKey<Level> var0 = ChaseCommand.DIMENSION_NAMES.get(param0.next());
        if (var0 == null) {
            return Optional.empty();
        } else {
            float var1 = param0.nextFloat();
            float var2 = param0.nextFloat();
            float var3 = param0.nextFloat();
            float var4 = param0.nextFloat();
            float var5 = param0.nextFloat();
            return Optional.of(new ChaseClient.TeleportTarget(var0, new Vec3((double)var1, (double)var2, (double)var3), new Vec2(var5, var4)));
        }
    }

    private void executeCommand(String param0) {
        this.server
            .execute(
                () -> {
                    List<ServerPlayer> var0 = this.server.getPlayerList().getPlayers();
                    if (!var0.isEmpty()) {
                        ServerPlayer var1x = var0.get(0);
                        ServerLevel var2 = this.server.overworld();
                        CommandSourceStack var3 = new CommandSourceStack(
                            var1x, Vec3.atLowerCornerOf(var2.getSharedSpawnPos()), Vec2.ZERO, var2, 4, "", CommonComponents.EMPTY, this.server, var1x
                        );
                        Commands var4 = this.server.getCommands();
                        var4.performCommand(var3, param0);
                    }
                }
            );
    }

    static record TeleportTarget(ResourceKey<Level> level, Vec3 pos, Vec2 rot) {
    }
}
