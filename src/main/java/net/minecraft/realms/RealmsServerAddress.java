package net.minecraft.realms;

import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsServerAddress {
    private final String host;
    private final int port;

    protected RealmsServerAddress(String param0, int param1) {
        this.host = param0;
        this.port = param1;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public static RealmsServerAddress parseString(String param0) {
        ServerAddress var0 = ServerAddress.parseString(param0);
        return new RealmsServerAddress(var0.getHost(), var0.getPort());
    }
}
