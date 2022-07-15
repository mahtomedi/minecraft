package net.minecraft.server.rcon.thread;

import com.mojang.logging.LogUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.rcon.PktUtils;
import org.slf4j.Logger;

public class RconClient extends GenericThread {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SERVERDATA_AUTH = 3;
    private static final int SERVERDATA_EXECCOMMAND = 2;
    private static final int SERVERDATA_RESPONSE_VALUE = 0;
    private static final int SERVERDATA_AUTH_RESPONSE = 2;
    private static final int SERVERDATA_AUTH_FAILURE = -1;
    private boolean authed;
    private final Socket client;
    private final byte[] buf = new byte[1460];
    private final String rconPassword;
    private final ServerInterface serverInterface;

    RconClient(ServerInterface param0, String param1, Socket param2) {
        super("RCON Client " + param2.getInetAddress());
        this.serverInterface = param0;
        this.client = param2;

        try {
            this.client.setSoTimeout(0);
        } catch (Exception var5) {
            this.running = false;
        }

        this.rconPassword = param1;
    }

    @Override
    public void run() {
        while(true) {
            try {
                if (!this.running) {
                    return;
                }

                BufferedInputStream var0 = new BufferedInputStream(this.client.getInputStream());
                int var1 = var0.read(this.buf, 0, 1460);
                if (10 <= var1) {
                    int var2 = 0;
                    int var3 = PktUtils.intFromByteArray(this.buf, 0, var1);
                    if (var3 != var1 - 4) {
                        return;
                    }

                    var2 += 4;
                    int var4 = PktUtils.intFromByteArray(this.buf, var2, var1);
                    var2 += 4;
                    int var5 = PktUtils.intFromByteArray(this.buf, var2);
                    var2 += 4;
                    switch(var5) {
                        case 2:
                            if (this.authed) {
                                String var7 = PktUtils.stringFromByteArray(this.buf, var2, var1);

                                try {
                                    this.sendCmdResponse(var4, this.serverInterface.runCommand(var7));
                                } catch (Exception var15) {
                                    this.sendCmdResponse(var4, "Error executing: " + var7 + " (" + var15.getMessage() + ")");
                                }
                                continue;
                            }

                            this.sendAuthFailure();
                            continue;
                        case 3:
                            String var6 = PktUtils.stringFromByteArray(this.buf, var2, var1);
                            var2 += var6.length();
                            if (!var6.isEmpty() && var6.equals(this.rconPassword)) {
                                this.authed = true;
                                this.send(var4, 2, "");
                                continue;
                            }

                            this.authed = false;
                            this.sendAuthFailure();
                            continue;
                        default:
                            this.sendCmdResponse(var4, String.format(Locale.ROOT, "Unknown request %s", Integer.toHexString(var5)));
                            continue;
                    }
                }
            } catch (IOException var16) {
                return;
            } catch (Exception var17) {
                LOGGER.error("Exception whilst parsing RCON input", (Throwable)var17);
                return;
            } finally {
                this.closeSocket();
                LOGGER.info("Thread {} shutting down", this.name);
                this.running = false;
            }

            return;
        }
    }

    private void send(int param0, int param1, String param2) throws IOException {
        ByteArrayOutputStream var0 = new ByteArrayOutputStream(1248);
        DataOutputStream var1 = new DataOutputStream(var0);
        byte[] var2 = param2.getBytes(StandardCharsets.UTF_8);
        var1.writeInt(Integer.reverseBytes(var2.length + 10));
        var1.writeInt(Integer.reverseBytes(param0));
        var1.writeInt(Integer.reverseBytes(param1));
        var1.write(var2);
        var1.write(0);
        var1.write(0);
        this.client.getOutputStream().write(var0.toByteArray());
    }

    private void sendAuthFailure() throws IOException {
        this.send(-1, 2, "");
    }

    private void sendCmdResponse(int param0, String param1) throws IOException {
        int var0 = param1.length();

        do {
            int var1 = 4096 <= var0 ? 4096 : var0;
            this.send(param0, 0, param1.substring(0, var1));
            param1 = param1.substring(var1);
            var0 = param1.length();
        } while(0 != var0);

    }

    @Override
    public void stop() {
        this.running = false;
        this.closeSocket();
        super.stop();
    }

    private void closeSocket() {
        try {
            this.client.close();
        } catch (IOException var2) {
            LOGGER.warn("Failed to close socket", (Throwable)var2);
        }

    }
}
