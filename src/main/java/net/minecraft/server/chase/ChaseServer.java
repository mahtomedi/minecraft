package net.minecraft.server.chase;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.server.commands.ChaseCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChaseServer {
    private static final Logger LOGGER = LogManager.getLogger();
    private final String serverBindAddress;
    private final int serverPort;
    private final PlayerList playerList;
    private final int broadcastIntervalMs;
    private volatile boolean wantsToRun;
    @Nullable
    private ServerSocket serverSocket;
    private final CopyOnWriteArrayList<Socket> clientSockets = new CopyOnWriteArrayList<>();

    public ChaseServer(String param0, int param1, PlayerList param2, int param3) {
        this.serverBindAddress = param0;
        this.serverPort = param1;
        this.playerList = param2;
        this.broadcastIntervalMs = param3;
    }

    public void start() throws IOException {
        if (this.serverSocket != null && !this.serverSocket.isClosed()) {
            LOGGER.warn("Remote control server was asked to start, but it is already running. Will ignore.");
        } else {
            this.wantsToRun = true;
            this.serverSocket = new ServerSocket(this.serverPort, 50, InetAddress.getByName(this.serverBindAddress));
            Thread var0 = new Thread(this::runAcceptor, "chase-server-acceptor");
            var0.setDaemon(true);
            var0.start();
            Thread var1 = new Thread(this::runSender, "chase-server-sender");
            var1.setDaemon(true);
            var1.start();
        }
    }

    private void runSender() {
        ChaseServer.PlayerPosition var0x = null;

        while(this.wantsToRun) {
            if (!this.clientSockets.isEmpty()) {
                ChaseServer.PlayerPosition var1x = this.getPlayerPosition();
                if (var1x != null && !var1x.equals(var0x)) {
                    var0x = var1x;
                    byte[] var2 = var1x.format().getBytes(StandardCharsets.US_ASCII);

                    for(Socket var3 : this.clientSockets) {
                        if (!var3.isClosed()) {
                            Util.ioPool().submit(() -> {
                                try {
                                    OutputStream var1xx = var3.getOutputStream();
                                    var1xx.write(var2);
                                    var1xx.flush();
                                } catch (IOException var3x) {
                                    LOGGER.info("Remote control client socket got an IO exception and will be closed", (Throwable)var3x);
                                    IOUtils.closeQuietly(var3);
                                }

                            });
                        }
                    }
                }

                List<Socket> var4 = this.clientSockets.stream().filter(Socket::isClosed).collect(Collectors.toList());
                this.clientSockets.removeAll(var4);
            }

            if (this.wantsToRun) {
                try {
                    Thread.sleep((long)this.broadcastIntervalMs);
                } catch (InterruptedException var6) {
                }
            }
        }

    }

    public void stop() {
        this.wantsToRun = false;
        IOUtils.closeQuietly(this.serverSocket);
        this.serverSocket = null;
    }

    private void runAcceptor() {
        while(true) {
            try {
                if (this.wantsToRun) {
                    if (this.serverSocket != null) {
                        LOGGER.info("Remote control server is listening for connections on port {}", this.serverPort);
                        Socket var0x = this.serverSocket.accept();
                        LOGGER.info("Remote control server received client connection on port {}", var0x.getPort());
                        this.clientSockets.add(var0x);
                    }
                    continue;
                }
            } catch (ClosedByInterruptException var6) {
                if (this.wantsToRun) {
                    LOGGER.info("Remote control server closed by interrupt");
                }
            } catch (IOException var7) {
                if (this.wantsToRun) {
                    LOGGER.error("Remote control server closed because of an IO exception", (Throwable)var7);
                }
            } finally {
                IOUtils.closeQuietly(this.serverSocket);
            }

            LOGGER.info("Remote control server is now stopped");
            this.wantsToRun = false;
            return;
        }
    }

    @Nullable
    private ChaseServer.PlayerPosition getPlayerPosition() {
        List<ServerPlayer> var0 = this.playerList.getPlayers();
        if (var0.isEmpty()) {
            return null;
        } else {
            ServerPlayer var1 = var0.get(0);
            String var2 = ChaseCommand.DIMENSION_NAMES.inverse().get(var1.getLevel().dimension());
            return var2 == null ? null : new ChaseServer.PlayerPosition(var2, var1.getX(), var1.getY(), var1.getZ(), var1.getYRot(), var1.getXRot());
        }
    }

    static record PlayerPosition(String dimensionName, double x, double y, double z, float yRot, float xRot) {
        String format() {
            return String.format(Locale.ROOT, "t %s %.2f %.2f %.2f %.2f %.2f\n", this.dimensionName, this.x, this.y, this.z, this.yRot, this.xRot);
        }
    }
}
