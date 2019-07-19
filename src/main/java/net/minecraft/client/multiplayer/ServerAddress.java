package net.minecraft.client.multiplayer;

import java.net.IDN;
import java.util.Hashtable;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ServerAddress {
    private final String host;
    private final int port;

    private ServerAddress(String param0, int param1) {
        this.host = param0;
        this.port = param1;
    }

    public String getHost() {
        try {
            return IDN.toASCII(this.host);
        } catch (IllegalArgumentException var2) {
            return "";
        }
    }

    public int getPort() {
        return this.port;
    }

    public static ServerAddress parseString(String param0) {
        if (param0 == null) {
            return null;
        } else {
            String[] var0 = param0.split(":");
            if (param0.startsWith("[")) {
                int var1 = param0.indexOf("]");
                if (var1 > 0) {
                    String var2 = param0.substring(1, var1);
                    String var3 = param0.substring(var1 + 1).trim();
                    if (var3.startsWith(":") && !var3.isEmpty()) {
                        var3 = var3.substring(1);
                        var0 = new String[]{var2, var3};
                    } else {
                        var0 = new String[]{var2};
                    }
                }
            }

            if (var0.length > 2) {
                var0 = new String[]{param0};
            }

            String var4 = var0[0];
            int var5 = var0.length > 1 ? parseInt(var0[1], 25565) : 25565;
            if (var5 == 25565) {
                String[] var6 = lookupSrv(var4);
                var4 = var6[0];
                var5 = parseInt(var6[1], 25565);
            }

            return new ServerAddress(var4, var5);
        }
    }

    private static String[] lookupSrv(String param0) {
        try {
            String var0 = "com.sun.jndi.dns.DnsContextFactory";
            Class.forName("com.sun.jndi.dns.DnsContextFactory");
            Hashtable<String, String> var1 = new Hashtable<>();
            var1.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            var1.put("java.naming.provider.url", "dns:");
            var1.put("com.sun.jndi.dns.timeout.retries", "1");
            DirContext var2 = new InitialDirContext(var1);
            Attributes var3 = var2.getAttributes("_minecraft._tcp." + param0, new String[]{"SRV"});
            String[] var4 = var3.get("srv").get().toString().split(" ", 4);
            return new String[]{var4[3], var4[2]};
        } catch (Throwable var6) {
            return new String[]{param0, Integer.toString(25565)};
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
