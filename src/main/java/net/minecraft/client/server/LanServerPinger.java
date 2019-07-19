package net.minecraft.client.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class LanServerPinger extends Thread {
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    private static final Logger LOGGER = LogManager.getLogger();
    private final String motd;
    private final DatagramSocket socket;
    private boolean isRunning = true;
    private final String serverAddress;

    public LanServerPinger(String param0, String param1) throws IOException {
        super("LanServerPinger #" + UNIQUE_THREAD_ID.incrementAndGet());
        this.motd = param0;
        this.serverAddress = param1;
        this.setDaemon(true);
        this.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        this.socket = new DatagramSocket();
    }

    @Override
    public void run() {
        String var0 = createPingString(this.motd, this.serverAddress);
        byte[] var1 = var0.getBytes(StandardCharsets.UTF_8);

        while(!this.isInterrupted() && this.isRunning) {
            try {
                InetAddress var2 = InetAddress.getByName("224.0.2.60");
                DatagramPacket var3 = new DatagramPacket(var1, var1.length, var2, 4445);
                this.socket.send(var3);
            } catch (IOException var6) {
                LOGGER.warn("LanServerPinger: {}", var6.getMessage());
                break;
            }

            try {
                sleep(1500L);
            } catch (InterruptedException var5) {
            }
        }

    }

    @Override
    public void interrupt() {
        super.interrupt();
        this.isRunning = false;
    }

    public static String createPingString(String param0, String param1) {
        return "[MOTD]" + param0 + "[/MOTD][AD]" + param1 + "[/AD]";
    }

    public static String parseMotd(String param0) {
        int var0 = param0.indexOf("[MOTD]");
        if (var0 < 0) {
            return "missing no";
        } else {
            int var1 = param0.indexOf("[/MOTD]", var0 + "[MOTD]".length());
            return var1 < var0 ? "missing no" : param0.substring(var0 + "[MOTD]".length(), var1);
        }
    }

    public static String parseAddress(String param0) {
        int var0 = param0.indexOf("[/MOTD]");
        if (var0 < 0) {
            return null;
        } else {
            int var1 = param0.indexOf("[/MOTD]", var0 + "[/MOTD]".length());
            if (var1 >= 0) {
                return null;
            } else {
                int var2 = param0.indexOf("[AD]", var0 + "[/MOTD]".length());
                if (var2 < 0) {
                    return null;
                } else {
                    int var3 = param0.indexOf("[/AD]", var2 + "[AD]".length());
                    return var3 < var2 ? null : param0.substring(var2 + "[AD]".length(), var3);
                }
            }
        }
    }
}
