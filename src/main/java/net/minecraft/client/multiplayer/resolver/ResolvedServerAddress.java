package net.minecraft.client.multiplayer.resolver;

import java.net.InetSocketAddress;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ResolvedServerAddress {
    String getHostName();

    String getHostIp();

    int getPort();

    InetSocketAddress asInetSocketAddress();

    static ResolvedServerAddress from(final InetSocketAddress param0) {
        return new ResolvedServerAddress() {
            @Override
            public String getHostName() {
                return param0.getAddress().getHostName();
            }

            @Override
            public String getHostIp() {
                return param0.getAddress().getHostAddress();
            }

            @Override
            public int getPort() {
                return param0.getPort();
            }

            @Override
            public InetSocketAddress asInetSocketAddress() {
                return param0;
            }
        };
    }
}
