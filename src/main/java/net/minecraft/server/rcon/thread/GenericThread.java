package net.minecraft.server.rcon.thread;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
import net.minecraft.server.ServerInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class GenericThread implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    protected boolean running;
    protected final ServerInterface serverInterface;
    protected final String name;
    protected Thread thread;
    protected final int maxStopWait = 5;
    protected final List<DatagramSocket> datagramSockets = Lists.newArrayList();
    protected final List<ServerSocket> serverSockets = Lists.newArrayList();

    protected GenericThread(ServerInterface param0, String param1) {
        this.serverInterface = param0;
        this.name = param1;
        if (this.serverInterface.isDebugging()) {
            this.warn("Debugging is enabled, performance maybe reduced!");
        }

    }

    public synchronized void start() {
        this.thread = new Thread(this, this.name + " #" + UNIQUE_THREAD_ID.incrementAndGet());
        this.thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(LOGGER));
        this.thread.start();
        this.running = true;
    }

    public synchronized void stop() {
        this.running = false;
        if (null != this.thread) {
            int var0 = 0;

            while(this.thread.isAlive()) {
                try {
                    this.thread.join(1000L);
                    if (5 <= ++var0) {
                        this.warn("Waited " + var0 + " seconds attempting force stop!");
                        this.closeSockets(true);
                    } else if (this.thread.isAlive()) {
                        this.warn("Thread " + this + " (" + this.thread.getState() + ") failed to exit after " + var0 + " second(s)");
                        this.warn("Stack:");

                        for(StackTraceElement var1 : this.thread.getStackTrace()) {
                            this.warn(var1.toString());
                        }

                        this.thread.interrupt();
                    }
                } catch (InterruptedException var6) {
                }
            }

            this.closeSockets(true);
            this.thread = null;
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    protected void debug(String param0) {
        this.serverInterface.debug(param0);
    }

    protected void info(String param0) {
        this.serverInterface.info(param0);
    }

    protected void warn(String param0) {
        this.serverInterface.warn(param0);
    }

    protected void error(String param0) {
        this.serverInterface.error(param0);
    }

    protected int currentPlayerCount() {
        return this.serverInterface.getPlayerCount();
    }

    protected void registerSocket(DatagramSocket param0) {
        this.debug("registerSocket: " + param0);
        this.datagramSockets.add(param0);
    }

    protected boolean closeSocket(DatagramSocket param0, boolean param1) {
        this.debug("closeSocket: " + param0);
        if (null == param0) {
            return false;
        } else {
            boolean var0 = false;
            if (!param0.isClosed()) {
                param0.close();
                var0 = true;
            }

            if (param1) {
                this.datagramSockets.remove(param0);
            }

            return var0;
        }
    }

    protected boolean closeSocket(ServerSocket param0) {
        return this.closeSocket(param0, true);
    }

    protected boolean closeSocket(ServerSocket param0, boolean param1) {
        this.debug("closeSocket: " + param0);
        if (null == param0) {
            return false;
        } else {
            boolean var0 = false;

            try {
                if (!param0.isClosed()) {
                    param0.close();
                    var0 = true;
                }
            } catch (IOException var5) {
                this.warn("IO: " + var5.getMessage());
            }

            if (param1) {
                this.serverSockets.remove(param0);
            }

            return var0;
        }
    }

    protected void closeSockets() {
        this.closeSockets(false);
    }

    protected void closeSockets(boolean param0) {
        int var0 = 0;

        for(DatagramSocket var1 : this.datagramSockets) {
            if (this.closeSocket(var1, false)) {
                ++var0;
            }
        }

        this.datagramSockets.clear();

        for(ServerSocket var2 : this.serverSockets) {
            if (this.closeSocket(var2, false)) {
                ++var0;
            }
        }

        this.serverSockets.clear();
        if (param0 && 0 < var0) {
            this.warn("Force closed " + var0 + " sockets");
        }

    }
}
