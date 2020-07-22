package net.minecraft.server.rcon.thread;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RconThread extends GenericThread {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ServerSocket socket;
    private final String rconPassword;
    private final List<RconClient> clients = Lists.newArrayList();
    private final ServerInterface serverInterface;

    private RconThread(ServerInterface param0, ServerSocket param1, String param2) {
        super("RCON Listener");
        this.serverInterface = param0;
        this.socket = param1;
        this.rconPassword = param2;
    }

    private void clearClients() {
        this.clients.removeIf(param0 -> !param0.isRunning());
    }

    @Override
    public void run() {
        try {
            while(this.running) {
                try {
                    Socket var0 = this.socket.accept();
                    RconClient var1 = new RconClient(this.serverInterface, this.rconPassword, var0);
                    var1.start();
                    this.clients.add(var1);
                    this.clearClients();
                } catch (SocketTimeoutException var7) {
                    this.clearClients();
                } catch (IOException var8) {
                    if (this.running) {
                        LOGGER.info("IO exception: ", (Throwable)var8);
                    }
                }
            }
        } finally {
            this.closeSocket(this.socket);
        }

    }

    @Nullable
    public static RconThread create(ServerInterface param0) {
        DedicatedServerProperties var0 = param0.getProperties();
        String var1 = param0.getServerIp();
        if (var1.isEmpty()) {
            var1 = "0.0.0.0";
        }

        int var2 = var0.rconPort;
        if (0 < var2 && 65535 >= var2) {
            String var3 = var0.rconPassword;
            if (var3.isEmpty()) {
                LOGGER.warn("No rcon password set in server.properties, rcon disabled!");
                return null;
            } else {
                try {
                    ServerSocket var4 = new ServerSocket(var2, 0, InetAddress.getByName(var1));
                    var4.setSoTimeout(500);
                    RconThread var5 = new RconThread(param0, var4, var3);
                    if (!var5.start()) {
                        return null;
                    } else {
                        LOGGER.info("RCON running on {}:{}", var1, var2);
                        return var5;
                    }
                } catch (IOException var7) {
                    LOGGER.warn("Unable to initialise RCON on {}:{}", var1, var2, var7);
                    return null;
                }
            }
        } else {
            LOGGER.warn("Invalid rcon port {} found in server.properties, rcon disabled!", var2);
            return null;
        }
    }

    @Override
    public void stop() {
        this.running = false;
        this.closeSocket(this.socket);
        super.stop();

        for(RconClient var0 : this.clients) {
            if (var0.isRunning()) {
                var0.stop();
            }
        }

        this.clients.clear();
    }

    private void closeSocket(ServerSocket param0) {
        LOGGER.debug("closeSocket: {}", param0);

        try {
            param0.close();
        } catch (IOException var3) {
            LOGGER.warn("Failed to close socket", (Throwable)var3);
        }

    }
}
