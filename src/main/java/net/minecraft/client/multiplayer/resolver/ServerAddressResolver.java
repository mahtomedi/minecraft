package net.minecraft.client.multiplayer.resolver;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface ServerAddressResolver {
    Logger LOGGER = LogManager.getLogger();
    ServerAddressResolver SYSTEM = param0 -> {
        try {
            InetAddress var0 = InetAddress.getByName(param0.getHost());
            return Optional.of(ResolvedServerAddress.from(new InetSocketAddress(var0, param0.getPort())));
        } catch (UnknownHostException var2) {
            LOGGER.debug("Couldn't resolve server {} address", param0.getHost(), var2);
            return Optional.empty();
        }
    };

    Optional<ResolvedServerAddress> resolve(ServerAddress var1);
}
