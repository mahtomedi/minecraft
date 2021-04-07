package net.minecraft.client.multiplayer;

import com.google.common.net.HostAndPort;
import java.net.IDN;
import java.util.Hashtable;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ServerAddress {
    private static final Logger LOGGER = LogManager.getLogger();
    private final HostAndPort hostAndPort;
    private static final ServerAddress INVALID = new ServerAddress(HostAndPort.fromParts("server.invalid", 25565));

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
            HostAndPort var0;
            try {
                var0 = HostAndPort.fromString(param0).withDefaultPort(25565);
                if (var0.getHost().isEmpty()) {
                    return INVALID;
                }
            } catch (IllegalArgumentException var3) {
                LOGGER.info("Failed to parse URL {}", param0, var3);
                return INVALID;
            }

            return new ServerAddress(lookupSrv(var0));
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

    private static HostAndPort lookupSrv(HostAndPort param0) {
        if (param0.getPort() != 25565) {
            return param0;
        } else {
            try {
                String var0 = "com.sun.jndi.dns.DnsContextFactory";
                Class.forName("com.sun.jndi.dns.DnsContextFactory");
                Hashtable<String, String> var1 = new Hashtable<>();
                var1.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
                var1.put("java.naming.provider.url", "dns:");
                var1.put("com.sun.jndi.dns.timeout.retries", "1");
                DirContext var2 = new InitialDirContext(var1);
                Attributes var3 = var2.getAttributes("_minecraft._tcp." + param0.getHost(), new String[]{"SRV"});
                Attribute var4 = var3.get("srv");
                if (var4 != null) {
                    String[] var5 = var4.get().toString().split(" ", 4);
                    return HostAndPort.fromParts(var5[3], parseInt(var5[2], 25565));
                }
            } catch (Throwable var7) {
            }

            return param0;
        }
    }

    private static int parseInt(String param0, int param1) {
        try {
            return Integer.parseInt(param0.trim());
        } catch (Exception var3) {
            return param1;
        }
    }
}
