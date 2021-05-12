package net.minecraft.client.multiplayer.resolver;

import java.util.Hashtable;
import java.util.Optional;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface ServerRedirectHandler {
    Logger LOGGER = LogManager.getLogger();
    ServerRedirectHandler EMPTY = param0 -> Optional.empty();

    Optional<ServerAddress> lookupRedirect(ServerAddress var1);

    static ServerRedirectHandler createDnsSrvRedirectHandler() {
        DirContext var2;
        try {
            String var0 = "com.sun.jndi.dns.DnsContextFactory";
            Class.forName("com.sun.jndi.dns.DnsContextFactory");
            Hashtable<String, String> var1 = new Hashtable<>();
            var1.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            var1.put("java.naming.provider.url", "dns:");
            var1.put("com.sun.jndi.dns.timeout.retries", "1");
            var2 = new InitialDirContext(var1);
        } catch (Throwable var31) {
            LOGGER.error("Failed to initialize SRV redirect resolved, some servers might not work", var31);
            return EMPTY;
        }

        return param1 -> {
            if (param1.getPort() == 25565) {
                try {
                    Attributes var0x = var2.getAttributes("_minecraft._tcp." + param1.getHost(), new String[]{"SRV"});
                    Attribute var1x = var0x.get("srv");
                    if (var1x != null) {
                        String[] var2x = var1x.get().toString().split(" ", 4);
                        return Optional.of(new ServerAddress(var2x[3], ServerAddress.parsePort(var2x[2])));
                    }
                } catch (Throwable var5) {
                }
            }

            return Optional.empty();
        };
    }
}
