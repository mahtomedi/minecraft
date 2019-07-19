package com.mojang.realmsclient.dto;

import java.util.Locale;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RegionPingResult extends ValueObject {
    private final String regionName;
    private final int ping;

    public RegionPingResult(String param0, int param1) {
        this.regionName = param0;
        this.ping = param1;
    }

    public int ping() {
        return this.ping;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s --> %.2f ms", this.regionName, (float)this.ping);
    }
}
