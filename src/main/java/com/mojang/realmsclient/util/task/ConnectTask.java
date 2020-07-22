package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.dto.RealmsServerAddress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsConnect;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConnectTask extends LongRunningTask {
    private final RealmsConnect realmsConnect;
    private final RealmsServerAddress address;

    public ConnectTask(Screen param0, RealmsServerAddress param1) {
        this.address = param1;
        this.realmsConnect = new RealmsConnect(param0);
    }

    @Override
    public void run() {
        this.setTitle(new TranslatableComponent("mco.connect.connecting"));
        net.minecraft.realms.RealmsServerAddress var0 = net.minecraft.realms.RealmsServerAddress.parseString(this.address.address);
        this.realmsConnect.connect(var0.getHost(), var0.getPort());
    }

    @Override
    public void abortTask() {
        this.realmsConnect.abort();
        Minecraft.getInstance().getClientPackSource().clearServerPack();
    }

    @Override
    public void tick() {
        this.realmsConnect.tick();
    }
}
