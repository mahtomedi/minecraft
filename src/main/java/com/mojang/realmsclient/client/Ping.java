package com.mojang.realmsclient.client;

import com.mojang.realmsclient.dto.RegionPingResult;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Ping {
    public static List<RegionPingResult> ping(Ping.Region... param0) {
        for(Ping.Region var0 : param0) {
            ping(var0.endpoint);
        }

        List<RegionPingResult> var1 = new ArrayList<>();

        for(Ping.Region var2 : param0) {
            var1.add(new RegionPingResult(var2.name, ping(var2.endpoint)));
        }

        Collections.sort(var1, new Comparator<RegionPingResult>() {
            public int compare(RegionPingResult param0, RegionPingResult param1) {
                return param0.ping() - param1.ping();
            }
        });
        return var1;
    }

    private static int ping(String param0) {
        int var0 = 700;
        long var1 = 0L;
        Socket var2 = null;

        for(int var3 = 0; var3 < 5; ++var3) {
            try {
                SocketAddress var4 = new InetSocketAddress(param0, 80);
                var2 = new Socket();
                long var5 = now();
                var2.connect(var4, 700);
                var1 += now() - var5;
            } catch (Exception var12) {
                var1 += 700L;
            } finally {
                close(var2);
            }
        }

        return (int)((double)var1 / 5.0);
    }

    private static void close(Socket param0) {
        try {
            if (param0 != null) {
                param0.close();
            }
        } catch (Throwable var2) {
        }

    }

    private static long now() {
        return System.currentTimeMillis();
    }

    public static List<RegionPingResult> pingAllRegions() {
        return ping(Ping.Region.values());
    }

    @OnlyIn(Dist.CLIENT)
    static enum Region {
        US_EAST_1("us-east-1", "ec2.us-east-1.amazonaws.com"),
        US_WEST_2("us-west-2", "ec2.us-west-2.amazonaws.com"),
        US_WEST_1("us-west-1", "ec2.us-west-1.amazonaws.com"),
        EU_WEST_1("eu-west-1", "ec2.eu-west-1.amazonaws.com"),
        AP_SOUTHEAST_1("ap-southeast-1", "ec2.ap-southeast-1.amazonaws.com"),
        AP_SOUTHEAST_2("ap-southeast-2", "ec2.ap-southeast-2.amazonaws.com"),
        AP_NORTHEAST_1("ap-northeast-1", "ec2.ap-northeast-1.amazonaws.com"),
        SA_EAST_1("sa-east-1", "ec2.sa-east-1.amazonaws.com");

        private final String name;
        private final String endpoint;

        private Region(String param0, String param1) {
            this.name = param0;
            this.endpoint = param1;
        }
    }
}
