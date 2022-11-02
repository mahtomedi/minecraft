package net.minecraft.client.multiplayer.resolver;

import com.google.common.net.HostAndPort;
import com.mojang.logging.LogUtils;
import java.net.IDN;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public final class ServerAddress {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final HostAndPort hostAndPort;
    private static final ServerAddress INVALID = new ServerAddress(HostAndPort.fromParts("server.invalid", 25565));

    public ServerAddress(String param0, int param1) {
        this(HostAndPort.fromParts(param0, param1));
    }

    private ServerAddress(HostAndPort param0) {
        this.hostAndPort = param0;
    }

    public String getHost() {
        try {
            return IDN.toASCII(this.hostAndPort.getHost());
        } catch (IllegalArgumentException var2) {
            return "";
        }
    }

    public int getPort() {
        return this.hostAndPort.getPort();
    }

    public static ServerAddress parseString(String param0) {
        if (param0 == null) {
            return INVALID;
        } else {
            try {
                HostAndPort var0 = HostAndPort.fromString(param0).withDefaultPort(25565);
                return var0.getHost().isEmpty() ? INVALID : new ServerAddress(var0);
            } catch (IllegalArgumentException var2) {
                LOGGER.info("Failed to parse URL {}", param0, var2);
                return INVALID;
            }
        }
    }

    public static boolean isValidAddress(String param0) {
        try {
            HostAndPort var0 = HostAndPort.fromString(param0);
            String var1 = var0.getHost();
            if (!var1.isEmpty()) {
                IDN.toASCII(var1);
                return true;
            }
        } catch (IllegalArgumentException var3) {
        }

        return false;
    }

    static int parsePort(String param0) {
        try {
            return Integer.parseInt(param0.trim());
        } catch (Exception var2) {
            return 25565;
        }
    }

    @Override
    public String toString() {
        return this.hostAndPort.toString();
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 instanceof ServerAddress ? this.hostAndPort.equals(((ServerAddress)param0).hostAndPort) : false;
        }
    }

    @Override
    public int hashCode() {
        return this.hostAndPort.hashCode();
    }
}
