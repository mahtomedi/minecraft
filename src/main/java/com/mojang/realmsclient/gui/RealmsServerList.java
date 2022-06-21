package com.mojang.realmsclient.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.realmsclient.dto.RealmsServer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsServerList {
    private final Minecraft minecraft;
    private final Set<RealmsServer> removedServers = Sets.newHashSet();
    private List<RealmsServer> servers = Lists.newArrayList();

    public RealmsServerList(Minecraft param0) {
        this.minecraft = param0;
    }

    public List<RealmsServer> updateServersList(List<RealmsServer> param0) {
        List<RealmsServer> var0 = new ArrayList<>(param0);
        var0.sort(new RealmsServer.McoServerComparator(this.minecraft.getUser().getName()));
        boolean var1 = var0.removeAll(this.removedServers);
        if (!var1) {
            this.removedServers.clear();
        }

        this.servers = var0;
        return List.copyOf(this.servers);
    }

    public synchronized List<RealmsServer> removeItem(RealmsServer param0) {
        this.servers.remove(param0);
        this.removedServers.add(param0);
        return List.copyOf(this.servers);
    }
}
