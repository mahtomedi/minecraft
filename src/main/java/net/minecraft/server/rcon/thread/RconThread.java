package net.minecraft.server.rcon.thread;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.dedicated.DedicatedServerProperties;

public class RconThread extends GenericThread {
    private final int port;
    private String serverIp;
    private ServerSocket socket;
    private final String rconPassword;
    private Map<SocketAddress, RconClient> clients;

    public RconThread(ServerInterface param0) {
        super(param0, "RCON Listener");
        DedicatedServerProperties var0 = param0.getProperties();
        this.port = var0.rconPort;
        this.rconPassword = var0.rconPassword;
        this.serverIp = param0.getServerIp();
        if (this.serverIp.isEmpty()) {
            this.serverIp = "0.0.0.0";
        }

        this.initClients();
        this.socket = null;
    }

    private void initClients() {
        this.clients = Maps.newHashMap();
    }

    private void clearClients() {
        Iterator<Entry<SocketAddress, RconClient>> var0 = this.clients.entrySet().iterator();

        while(var0.hasNext()) {
            Entry<SocketAddress, RconClient> var1 = var0.next();
            if (!var1.getValue().isRunning()) {
                var0.remove();
            }
        }

    }

    @Override
    public void run() {
        this.info("RCON running on " + this.serverIp + ":" + this.port);

        try {
            while(this.running) {
                try {
                    Socket var0 = this.socket.accept();
                    var0.setSoTimeout(500);
                    RconClient var1 = new RconClient(this.serverInterface, this.rconPassword, var0);
                    var1.start();
                    this.clients.put(var0.getRemoteSocketAddress(), var1);
                    this.clearClients();
                } catch (SocketTimeoutException var7) {
                    this.clearClients();
                } catch (IOException var8) {
                    if (this.running) {
                        this.info("IO: " + var8.getMessage());
                    }
                }
            }
        } finally {
            this.closeSocket(this.socket);
        }

    }

    @Override
    public void start() {
        if (this.rconPassword.isEmpty()) {
            this.warn("No rcon password set in server.properties, rcon disabled!");
        } else if (0 >= this.port || 65535 < this.port) {
            this.warn("Invalid rcon port " + this.port + " found in server.properties, rcon disabled!");
        } else if (!this.running) {
            try {
                this.socket = new ServerSocket(this.port, 0, InetAddress.getByName(this.serverIp));
                this.socket.setSoTimeout(500);
                super.start();
            } catch (IOException var2) {
                this.warn("Unable to initialise rcon on " + this.serverIp + ":" + this.port + " : " + var2.getMessage());
            }

        }
    }

    @Override
    public void stop() {
        super.stop();

        for(Entry<SocketAddress, RconClient> var1 : this.clients.entrySet()) {
            var1.getValue().stop();
        }

        this.closeSocket(this.socket);
        this.initClients();
    }
}
