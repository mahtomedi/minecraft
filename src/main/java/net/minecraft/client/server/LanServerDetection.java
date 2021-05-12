package net.minecraft.client.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class LanServerDetection {
    static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    static final Logger LOGGER = LogManager.getLogger();

    @OnlyIn(Dist.CLIENT)
    public static class LanServerDetector extends Thread {
        private final LanServerDetection.LanServerList serverList;
        private final InetAddress pingGroup;
        private final MulticastSocket socket;

        public LanServerDetector(LanServerDetection.LanServerList param0) throws IOException {
            super("LanServerDetector #" + LanServerDetection.UNIQUE_THREAD_ID.incrementAndGet());
            this.serverList = param0;
            this.setDaemon(true);
            this.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LanServerDetection.LOGGER));
            this.socket = new MulticastSocket(4445);
            this.pingGroup = InetAddress.getByName("224.0.2.60");
            this.socket.setSoTimeout(5000);
            this.socket.joinGroup(this.pingGroup);
        }

        @Override
        public void run() {
            byte[] var0 = new byte[1024];

            while(!this.isInterrupted()) {
                DatagramPacket var1 = new DatagramPacket(var0, var0.length);

                try {
                    this.socket.receive(var1);
                } catch (SocketTimeoutException var5) {
                    continue;
                } catch (IOException var6) {
                    LanServerDetection.LOGGER.error("Couldn't ping server", (Throwable)var6);
                    break;
                }

                String var4 = new String(var1.getData(), var1.getOffset(), var1.getLength(), StandardCharsets.UTF_8);
                LanServerDetection.LOGGER.debug("{}: {}", var1.getAddress(), var4);
                this.serverList.addServer(var4, var1.getAddress());
            }

            try {
                this.socket.leaveGroup(this.pingGroup);
            } catch (IOException var41) {
            }

            this.socket.close();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class LanServerList {
        private final List<LanServer> servers = Lists.newArrayList();
        private boolean isDirty;

        public synchronized boolean isDirty() {
            return this.isDirty;
        }

        public synchronized void markClean() {
            this.isDirty = false;
        }

        public synchronized List<LanServer> getServers() {
            return Collections.unmodifiableList(this.servers);
        }

        public synchronized void addServer(String param0, InetAddress param1) {
            String var0 = LanServerPinger.parseMotd(param0);
            String var1 = LanServerPinger.parseAddress(param0);
            if (var1 != null) {
                var1 = param1.getHostAddress() + ":" + var1;
                boolean var2 = false;

                for(LanServer var3 : this.servers) {
                    if (var3.getAddress().equals(var1)) {
                        var3.updatePingTime();
                        var2 = true;
                        break;
                    }
                }

                if (!var2) {
                    this.servers.add(new LanServer(var0, var1));
                    this.isDirty = true;
                }

            }
        }
    }
}
